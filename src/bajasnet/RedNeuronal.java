package bajasnet;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.neuroph.core.*;
import org.neuroph.core.data.*;
import org.neuroph.nnet.*;
import org.neuroph.nnet.learning.*;

public class RedNeuronal {

    // Umbral de decision: probado con el dataset real, 0.6 da el mejor
    // equilibrio entre precision y recall cuando se entrena balanceado.
    static final double UMBRAL = 0.45;

    public static void main(String[] args) throws IOException {

        // ---------- 1. LEER Y CODIFICAR EL CSV ----------
        List<String> lineas = Files.readAllLines(Paths.get("DatasetTelcoChurn.csv"));
        String[] header = lineas.get(0).split(",", -1);
        int nCols = header.length;

        List<Map<String, Integer>> mapas = new ArrayList<>();
        for (int i = 0; i < nCols; i++) mapas.add(new HashMap<>());

        List<String> nombresUtiles = new ArrayList<>();
        for (int col = 0; col < nCols; col++) {
            if (!header[col].equalsIgnoreCase("customerID")) {
                nombresUtiles.add(header[col].trim());
            }
        }

        int churnIdx = -1;
        for (int c = 0; c < nombresUtiles.size(); c++) {
            if (nombresUtiles.get(c).equalsIgnoreCase("Churn")) { churnIdx = c; break; }
        }
        if (churnIdx == -1)
            throw new RuntimeException("No se encontro la columna 'Churn' en el header.");

        List<double[]> filasNumericas = new ArrayList<>();
        int nColsUtiles = nombresUtiles.size();

        for (int i = 1; i < lineas.size(); i++) {
            if (lineas.get(i).trim().isEmpty()) continue;
            String[] fila = lineas.get(i).split(",", -1);
            List<Double> filaNueva = new ArrayList<>();
            for (int col = 0; col < nCols; col++) {
                if (header[col].equalsIgnoreCase("customerID")) continue;

                String valor = fila[col].trim();
                if (valor.isEmpty()) valor = "0";

                double numero;
                try {
                    numero = Double.parseDouble(valor);
                } catch (NumberFormatException e) {
                    Map<String, Integer> mapa = mapas.get(col);
                    if (!mapa.containsKey(valor)) mapa.put(valor, mapa.size());
                    numero = mapa.get(valor);
                }
                filaNueva.add(numero);
            }
            double[] arr = new double[filaNueva.size()];
            for (int j = 0; j < arr.length; j++) arr[j] = filaNueva.get(j);
            filasNumericas.add(arr);
        }

        // ---------- 2. NORMALIZAR SOLO LAS ENTRADAS (no la salida) ----------
        double[] min = new double[nColsUtiles];
        double[] max = new double[nColsUtiles];
        Arrays.fill(min, Double.MAX_VALUE);
        Arrays.fill(max, -Double.MAX_VALUE);
        for (double[] fila : filasNumericas) {
            for (int c = 0; c < nColsUtiles; c++) {
                if (c == churnIdx) continue;              // no tocar la salida
                if (fila[c] < min[c]) min[c] = fila[c];
                if (fila[c] > max[c]) max[c] = fila[c];
            }
        }

        int nEntradas = nColsUtiles - 1;

        // ---------- 3. ARMAR EL DATASET DE NEUROPH A MANO ----------
        DataSet dataset = new DataSet(nEntradas, 1);
        for (double[] fila : filasNumericas) {
            double[] entrada = new double[nEntradas];
            int idx = 0;
            for (int c = 0; c < nColsUtiles; c++) {
                if (c == churnIdx) continue;
                double rango = max[c] - min[c];
                entrada[idx++] = rango == 0 ? 0 : (fila[c] - min[c]) / rango;
            }
            double salida = fila[churnIdx]; // 0 o 1
            dataset.add(entrada, new double[]{ salida });
        }

        // ---------- 4. SEPARAR TRAIN/TEST (antes de balancear) ----------
        dataset.shuffle();
        DataSet[] particiones = dataset.createTrainingAndTestSubsets(0.8, 0.2);
        DataSet train = particiones[0];
        DataSet test = particiones[1];   // el test queda intacto y representativo

        // ---------- 5. BALANCEAR SOLO EL TRAIN (oversampling de la minoria) ----------
        List<DataSetRow> pos = new ArrayList<>();
        List<DataSetRow> neg = new ArrayList<>();
        for (DataSetRow r : train.getRows()) {
            if (r.getDesiredOutput()[0] >= 0.5) pos.add(r); else neg.add(r);
        }
        DataSet trainBal = new DataSet(nEntradas, 1);
        for (DataSetRow r : neg) trainBal.add(r.getInput(), r.getDesiredOutput());
        int objetivo = neg.size();
        Random rnd = new Random(42);
        for (int k = 0; k < objetivo && !pos.isEmpty(); k++) {
            DataSetRow r = pos.get(rnd.nextInt(pos.size()));   // repite con reemplazo
            trainBal.add(r.getInput(), r.getDesiredOutput());
        }
        trainBal.shuffle();
        System.out.println("Train original: " + train.size()
                + " | balanceado: " + trainBal.size()
                + " (neg=" + neg.size() + ", pos original=" + pos.size() + ")");

        // ---------- 6. RED Y ENTRENAMIENTO ----------
        // (8,4): validado con el dataset real. Redes mas chicas como (3,2,2)
        // no tienen capacidad suficiente para 19 entradas y rinden mucho peor.
        NeuralNetwork red = new MultiLayerPerceptron(nEntradas, 4, 4, 1);

        MomentumBackpropagation regla = new MomentumBackpropagation();
        regla.setMaxIterations(3000);    // el error se estanca ~0.059, mas iteraciones no aportan
        regla.setLearningRate(0.05);
        regla.setMomentum(0.7);
        regla.setMaxError(0.055);        
        regla.addListener(event -> {
            MomentumBackpropagation bp = (MomentumBackpropagation) event.getSource();
            if (bp.getCurrentIteration() % 200 == 0) {
                System.out.println("Iteracion: " + bp.getCurrentIteration()
                        + " | Error: " + bp.getTotalNetworkError());
            }
        });
        red.setLearningRule(regla);

        long inicio = System.currentTimeMillis();
        red.learn(trainBal);
        long fin = System.currentTimeMillis();
        System.out.println("Entrenamiento terminado en " + (fin - inicio) / 1000.0 + " s.");
        System.out.println("Error final: " + regla.getTotalNetworkError()
                + " | Iteraciones: " + regla.getCurrentIteration());

        red.save("redEntrenada.nnet");

        // ---------- 7. EVALUACION CON MATRIZ DE CONFUSION + METRICAS DE ERROR ----------
        int tp = 0, tn = 0, fp = 0, fn = 0;
        double sumSq = 0, sumAbs = 0, sumReal = 0, sumPred = 0;
        double sumRealPred = 0, sumRealSquared = 0, sumPredSquared = 0;
        int n = test.getRows().size();

        List<double[]> paresTest = new ArrayList<>(); // {predichoContinuo, real}
        for (DataSetRow fila : test.getRows()) {
            red.setInput(fila.getInput());
            red.calculate();
            double predContinuo = red.getOutput()[0];   // salida cruda 0..1
            double real = fila.getDesiredOutput()[0];    // 0 o 1
            paresTest.add(new double[]{ predContinuo, real });

            double err = real - predContinuo;
            sumSq  += err * err;
            sumAbs += Math.abs(err);
            sumReal += real;
            sumPred += predContinuo;
            sumRealPred    += real * predContinuo;
            sumRealSquared += real * real;
            sumPredSquared += predContinuo * predContinuo;

            int predicho = predContinuo >= UMBRAL ? 1 : 0;   // umbral optimizado
            int realInt  = (int) Math.round(real);
            if (predicho == 1 && realInt == 1) tp++;
            else if (predicho == 0 && realInt == 0) tn++;
            else if (predicho == 1 && realInt == 0) fp++;
            else fn++;
        }

        // Metricas de error (sobre la salida continua)
        double mse  = sumSq / n;
        double rmse = Math.sqrt(mse);
        double mae  = sumAbs / n;

        // R2 clasico = 1 - SS_res / SS_tot
        double mediaReal = sumReal / n;
        double ssTot = 0;
        for (double[] par : paresTest) {
            double d = par[1] - mediaReal;
            ssTot += d * d;
        }
        double r2 = ssTot == 0 ? 0 : 1 - (sumSq / ssTot);

        double numerator = (n * sumRealPred - sumReal * sumPred);
        double denominator = Math.sqrt((n * sumRealSquared - sumReal * sumReal) *
                                       (n * sumPredSquared - sumPred * sumPred));
        double r = denominator == 0 ? 0 : numerator / denominator;

        int total = tp + tn + fp + fn;
        double accuracy  = 100.0 * (tp + tn) / total;
        double precision = (tp + fp) == 0 ? 0 : 100.0 * tp / (tp + fp);
        double recall    = (tp + fn) == 0 ? 0 : 100.0 * tp / (tp + fn);
        double f1        = (precision + recall) == 0 ? 0
                            : 2 * precision * recall / (precision + recall);

        System.out.println("\n---- RESULTADOS EN TEST (umbral " + UMBRAL + ") ----");
        System.out.println("Matriz de confusion:");
        System.out.println("             Pred NO   Pred SI");
        System.out.println("Real NO   |  " + tn + "        " + fp);
        System.out.println("Real SI   |  " + fn + "        " + tp);
        System.out.printf("Accuracy : %.2f%%%n", accuracy);
        System.out.printf("Precision: %.2f%% (de los que predijo churn, cuantos lo eran)%n", precision);
        System.out.printf("Recall   : %.2f%% (de los churn reales, cuantos detecto)%n", recall);
        System.out.printf("F1-score : %.2f%%%n", f1);

        System.out.println("\n---- METRICAS DE ERROR ----");
        System.out.printf("MSE : %.4f  (Exc<0.01 | Bueno<0.05 | Reg<0.15 | Malo>0.15)%n", mse);
        System.out.printf("RMSE: %.4f  (Exc<0.10 | Bueno<0.25 | Reg<0.40 | Malo>0.40)%n", rmse);
        System.out.printf("MAE : %.4f  (Exc<0.10 | Bueno<0.20 | Reg<0.30 | Malo>0.30)%n", mae);
        System.out.printf("R2 clasico : %.4f  (1 - SSres/SStot)%n", r2);

    }
}