package bajasnet;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.neuroph.core.*;
import org.neuroph.core.data.*;
import org.neuroph.nnet.*;
import org.neuroph.nnet.learning.*;

public class RedNeuronal {

    static final double UMBRAL = 0.45;

    public static void main(String[] args) throws IOException {

        // ---------- 1. LEER EL CSV ----------
        List<String> lineas = Files.readAllLines(Paths.get("DatasetTelcoChurn.csv"));
        String[] header = lineas.get(0).split(",", -1);
        int nCols = header.length;

        int idxCustomerID = indiceDe(header, "customerID");
        int idxChurn = indiceDe(header, "Churn");
        if (idxChurn == -1)
            throw new RuntimeException("No se encontró la columna 'Churn' en el header.");

        List<Integer> colsFeature = new ArrayList<>();
        for (int c = 0; c < nCols; c++) {
            if (c != idxCustomerID && c != idxChurn) colsFeature.add(c);
        }
        int nFeat = colsFeature.size();

        List<String[]> featuresCrudas = new ArrayList<>();
        List<Integer> targets = new ArrayList<>();
        for (int i = 1; i < lineas.size(); i++) {
            if (lineas.get(i).trim().isEmpty()) continue;
            String[] fila = lineas.get(i).split(",", -1);
            if (fila.length != nCols) continue;   

            String[] feats = new String[nFeat];
            for (int k = 0; k < nFeat; k++) {
                String v = fila[colsFeature.get(k)].trim();
                feats[k] = v.isEmpty() ? "0" : v;  
            }
            featuresCrudas.add(feats);

            targets.add(fila[idxChurn].trim().equalsIgnoreCase("Yes") ? 1 : 0);
        }

        boolean[] esNumerica = new boolean[nFeat];
        List<List<String>> categorias = new ArrayList<>();  
        for (int k = 0; k < nFeat; k++) {
            boolean numerica = true;
            for (String[] feats : featuresCrudas) {
                if (!esDouble(feats[k])) { numerica = false; break; }
            }
            esNumerica[k] = numerica;

            List<String> vocab = new ArrayList<>();
            if (!numerica) {
                LinkedHashSet<String> set = new LinkedHashSet<>();
                for (String[] feats : featuresCrudas) set.add(feats[k]);
                vocab.addAll(set);
            }
            categorias.add(vocab);
        }

        int nEntradas = 0;
        for (int k = 0; k < nFeat; k++) {
            nEntradas += esNumerica[k] ? 1 : categorias.get(k).size();
        }

        List<Integer> orden = new ArrayList<>();
        for (int i = 0; i < featuresCrudas.size(); i++) orden.add(i);
        Collections.shuffle(orden, new Random(42));
        int corte = (int) Math.round(orden.size() * 0.8);
        List<Integer> idxTrain = new ArrayList<>(orden.subList(0, corte));
        List<Integer> idxTest  = new ArrayList<>(orden.subList(corte, orden.size()));

        double[] min = new double[nFeat];
        double[] max = new double[nFeat];
        Arrays.fill(min, Double.MAX_VALUE);
        Arrays.fill(max, -Double.MAX_VALUE);
        for (int i : idxTrain) {
            String[] feats = featuresCrudas.get(i);
            for (int k = 0; k < nFeat; k++) {
                if (!esNumerica[k]) continue;
                double val = Double.parseDouble(feats[k]);
                if (val < min[k]) min[k] = val;
                if (val > max[k]) max[k] = val;
            }
        }

        DataSet train = new DataSet(nEntradas, 1);
        DataSet test  = new DataSet(nEntradas, 1);
        for (int i : idxTrain)
            train.add(vectorizar(featuresCrudas.get(i), esNumerica, categorias, min, max, nEntradas),
                      new double[]{ targets.get(i) });
        for (int i : idxTest)
            test.add(vectorizar(featuresCrudas.get(i), esNumerica, categorias, min, max, nEntradas),
                     new double[]{ targets.get(i) });

        List<DataSetRow> pos = new ArrayList<>();
        List<DataSetRow> neg = new ArrayList<>();
        for (DataSetRow r : train.getRows()) {
            if (r.getDesiredOutput()[0] >= 0.5) pos.add(r); else neg.add(r);
        }
        DataSet trainBal = new DataSet(nEntradas, 1);
        for (DataSetRow r : neg) trainBal.add(r.getInput(), r.getDesiredOutput());
        Random rnd = new Random(42);
        for (int k = 0; k < neg.size() && !pos.isEmpty(); k++) {
            DataSetRow r = pos.get(rnd.nextInt(pos.size()));   
            trainBal.add(r.getInput(), r.getDesiredOutput());
        }
        trainBal.shuffle();
        System.out.println("Entradas: " + nEntradas
                + " | Train: " + train.size() + " -> balanceado: " + trainBal.size()
                + " (neg=" + neg.size() + ", pos original=" + pos.size() + ")");

        NeuralNetwork red = new MultiLayerPerceptron(nEntradas, 8, 4, 1);

        MomentumBackpropagation regla = new MomentumBackpropagation();
        regla.setMaxIterations(3000);
        regla.setLearningRate(0.05);
        regla.setMomentum(0.7);
        regla.setMaxError(0.05);
        regla.addListener(event -> {
            MomentumBackpropagation bp = (MomentumBackpropagation) event.getSource();
            if (bp.getCurrentIteration() % 200 == 0) {
                System.out.println("Iteración: " + bp.getCurrentIteration()
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
        guardarPreprocesamiento("preprocesamiento.txt", header, colsFeature, esNumerica, categorias, min, max);

        int tp = 0, tn = 0, fp = 0, fn = 0;
        double sumSq = 0, sumAbs = 0, sumReal = 0, sumPred = 0;
        double sumRealPred = 0, sumRealSq = 0, sumPredSq = 0;
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
            sumRealPred += real * predContinuo;
            sumRealSq   += real * real;
            sumPredSq   += predContinuo * predContinuo;

            int predicho = predContinuo >= UMBRAL ? 1 : 0;
            int realInt  = (int) Math.round(real);
            if (predicho == 1 && realInt == 1) tp++;
            else if (predicho == 0 && realInt == 0) tn++;
            else if (predicho == 1 && realInt == 0) fp++;
            else fn++;
        }

        double mse  = sumSq / n;
        double rmse = Math.sqrt(mse);
        double mae  = sumAbs / n;

        double mediaReal = sumReal / n;
        double ssTot = 0;
        for (double[] par : paresTest) {
            double d = par[1] - mediaReal;
            ssTot += d * d;
        }
        double r2 = ssTot == 0 ? 0 : 1 - (sumSq / ssTot);

        double numer = n * sumRealPred - sumReal * sumPred;
        double denom = Math.sqrt((n * sumRealSq - sumReal * sumReal) * (n * sumPredSq - sumPred * sumPred));
        double r = denom == 0 ? 0 : numer / denom;

        int total = tp + tn + fp + fn;
        double accuracy  = 100.0 * (tp + tn) / total;
        double precision = (tp + fp) == 0 ? 0 : 100.0 * tp / (tp + fp);
        double recall    = (tp + fn) == 0 ? 0 : 100.0 * tp / (tp + fn);
        double f1        = (precision + recall) == 0 ? 0 : 2 * precision * recall / (precision + recall);

        System.out.println("\n---- RESULTADOS EN TEST (umbral " + UMBRAL + ") ----");
        System.out.println("Matriz de confusión:");
        System.out.println("             Pred NO   Pred SI");
        System.out.println("Real NO   |  " + tn + "        " + fp);
        System.out.println("Real SI   |  " + fn + "        " + tp);
        System.out.printf("Accuracy : %.2f%%%n", accuracy);
        System.out.printf("Precision: %.2f%% (de los que predijo churn, cuántos lo eran)%n", precision);
        System.out.printf("Recall   : %.2f%% (de los churn reales, cuántos detectó)%n", recall);
        System.out.printf("F1-score : %.2f%%%n", f1);

        System.out.println("\n---- MÉTRICAS DE ERROR ----");
        System.out.printf("MSE : %.4f  (Exc<0.01 | Bueno<0.05 | Reg<0.15 | Malo>0.15)%n", mse);
        System.out.printf("RMSE: %.4f  (Exc<0.10 | Bueno<0.25 | Reg<0.40 | Malo>0.40)%n", rmse);
        System.out.printf("MAE : %.4f  (Exc<0.10 | Bueno<0.20 | Reg<0.30 | Malo>0.30)%n", mae);
        System.out.printf("R2  : %.4f  (1 - SSres/SStot)%n", r2);
        System.out.printf("r   : %.4f  (correlación de Pearson pred vs real)%n", r);
    }

    // ---------- Helpers ----------

    private static int indiceDe(String[] header, String nombre) {
        for (int i = 0; i < header.length; i++) {
            if (header[i].trim().equalsIgnoreCase(nombre)) return i;
        }
        return -1;
    }

    private static boolean esDouble(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Convierte una fila cruda en el vector de entrada: numéricas normalizadas + categóricas en one-hot. */
    private static double[] vectorizar(String[] feats, boolean[] esNumerica, List<List<String>> categorias,
                                       double[] min, double[] max, int nEntradas) {
        double[] v = new double[nEntradas];
        int idx = 0;
        for (int k = 0; k < feats.length; k++) {
            if (esNumerica[k]) {
                double val = Double.parseDouble(feats[k]);
                double rango = max[k] - min[k];
                v[idx++] = rango == 0 ? 0 : (val - min[k]) / rango;
            } else {
                List<String> vocab = categorias.get(k);
                int pos = vocab.indexOf(feats[k]);          // -1 si es una categoría no vista
                for (int j = 0; j < vocab.size(); j++) {
                    v[idx++] = (j == pos) ? 1.0 : 0.0;
                }
            }
        }
        return v;
    }

    /** Guarda tipos, vocabularios y min/max para reproducir el mismo preprocesamiento al predecir. */
    private static void guardarPreprocesamiento(String archivo, String[] header, List<Integer> colsFeature,
            boolean[] esNumerica, List<List<String>> categorias, double[] min, double[] max) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo))) {
            for (int k = 0; k < colsFeature.size(); k++) {
                String nombre = header[colsFeature.get(k)].trim();
                if (esNumerica[k]) {
                    bw.write("NUM|" + nombre + "|" + min[k] + "|" + max[k]);
                } else {
                    bw.write("CAT|" + nombre + "|" + String.join(";", categorias.get(k)));
                }
                bw.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
