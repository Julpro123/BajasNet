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
    static final Random RND = new Random(42);

    public static void main(String[] args) throws IOException {
        List<String[]> filas = cargarCsv("DatasetTelcoChurn.csv");
        String[] header = filas.remove(0);

        Pre pre = new Pre(header, filas);
        System.out.println("Entradas: " + pre.nEntradas);

        DataSet[] split = pre.construirDataSets(filas);
        DataSet train = balancear(split[0], pre.nEntradas);
        DataSet test = split[1];

        NeuralNetwork red = entrenar(train, pre.nEntradas);
        red.save("redEntrenada.nnet");
        pre.guardar("preprocesamiento.txt");

        evaluar(red, test);
    }

    // ---- Carga ----

    static List<String[]> cargarCsv(String archivo) throws IOException {
        List<String[]> out = new ArrayList<>();
        for (String linea : Files.readAllLines(Paths.get(archivo))) {
            if (linea.trim().isEmpty()) continue;
            out.add(linea.split(",", -1));
        }
        return out;
    }

    // ---- Preprocesamiento (una sola clase con todo el estado) ----

    static class Pre {
        final String[] nombres;      // nombre de cada feature
        final boolean[] numerica;
        final List<List<String>> vocab = new ArrayList<>();
        final double[] min, max;
        final int nEntradas;
        final int nCols;
        final int idxChurn, idxId;

        Pre(String[] header, List<String[]> filas) {
            nCols = header.length;
            idxChurn = indiceDe(header, "Churn");
            idxId = indiceDe(header, "customerID");
            if (idxChurn == -1) throw new RuntimeException("Falta columna 'Churn'.");

            // columnas de entrada = todas menos id y churn
            List<Integer> cols = new ArrayList<>();
            for (int c = 0; c < nCols; c++)
                if (c != idxId && c != idxChurn) cols.add(c);

            int nFeat = cols.size();
            nombres = new String[nFeat];
            numerica = new boolean[nFeat];
            min = new double[nFeat];
            max = new double[nFeat];
            Arrays.fill(min, Double.MAX_VALUE);
            Arrays.fill(max, -Double.MAX_VALUE);

            for (int k = 0; k < nFeat; k++) {
                int col = cols.get(k);
                nombres[k] = header[col].trim();
                numerica[k] = true;
                LinkedHashSet<String> cats = new LinkedHashSet<>();

                for (String[] fila : filas) {
                    if (fila.length != nCols) continue;
                    String v = valor(fila, col);
                    if (esDouble(v)) {
                        double d = Double.parseDouble(v);
                        min[k] = Math.min(min[k], d);
                        max[k] = Math.max(max[k], d);
                    } else {
                        numerica[k] = false;
                    }
                    cats.add(v);
                }
                vocab.add(numerica[k] ? List.of() : new ArrayList<>(cats));
            }

            int n = 0;
            for (int k = 0; k < nFeat; k++)
                n += numerica[k] ? 1 : vocab.get(k).size();
            nEntradas = n;
        }

        DataSet[] construirDataSets(List<String[]> filas) {
            List<String[]> validas = new ArrayList<>();
            for (String[] f : filas) if (f.length == nCols) validas.add(f);
            Collections.shuffle(validas, new Random(42));
            int corte = (int) Math.round(validas.size() * 0.8);

            DataSet train = new DataSet(nEntradas, 1);
            DataSet test = new DataSet(nEntradas, 1);
            for (int i = 0; i < validas.size(); i++) {
                String[] f = validas.get(i);
                double target = f[idxChurn].trim().equalsIgnoreCase("Yes") ? 1 : 0;
                (i < corte ? train : test).add(vectorizar(f), new double[]{ target });
            }
            return new DataSet[]{ train, test };
        }

        double[] vectorizar(String[] fila) {
            double[] v = new double[nEntradas];
            int idx = 0, k = 0;
            for (int c = 0; c < nCols; c++) {
                if (c == idxId || c == idxChurn) continue;
                String val = valor(fila, c);
                if (numerica[k]) {
                    double d = esDouble(val) ? Double.parseDouble(val) : 0;
                    double rango = max[k] - min[k];
                    v[idx++] = rango == 0 ? 0 : (d - min[k]) / rango;
                } else {
                    List<String> cats = vocab.get(k);
                    int pos = cats.indexOf(val);
                    for (int j = 0; j < cats.size(); j++)
                        v[idx++] = (j == pos) ? 1.0 : 0.0;
                }
                k++;
            }
            return v;
        }

        void guardar(String archivo) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo))) {
                for (int k = 0; k < nombres.length; k++) {
                    bw.write(numerica[k]
                        ? "NUM|" + nombres[k] + "|" + min[k] + "|" + max[k]
                        : "CAT|" + nombres[k] + "|" + String.join(";", vocab.get(k)));
                    bw.newLine();
                }
            } catch (IOException e) { e.printStackTrace(); }
        }

        private String valor(String[] fila, int col) {
            String v = fila[col].trim();
            return v.isEmpty() ? "0" : v;
        }
    }

    // ---- Balanceo (oversampling de la clase minoritaria) ----

    static DataSet balancear(DataSet train, int nEntradas) {
        List<DataSetRow> pos = new ArrayList<>(), neg = new ArrayList<>();
        for (DataSetRow r : train.getRows())
            (r.getDesiredOutput()[0] >= 0.5 ? pos : neg).add(r);

        DataSet bal = new DataSet(nEntradas, 1);
        for (DataSetRow r : neg) bal.add(r.getInput(), r.getDesiredOutput());
        for (int i = 0; i < neg.size() && !pos.isEmpty(); i++) {
            DataSetRow r = pos.get(RND.nextInt(pos.size()));
            bal.add(r.getInput(), r.getDesiredOutput());
        }
        bal.shuffle();
        System.out.println("Train balanceado: " + bal.size()
                + " (neg=" + neg.size() + ", pos=" + pos.size() + ")");
        return bal;
    }

    // ---- Entrenamiento ----

    static NeuralNetwork entrenar(DataSet train, int nEntradas) {
        NeuralNetwork red = new MultiLayerPerceptron(nEntradas, 8, 4, 1);
        MomentumBackpropagation regla = new MomentumBackpropagation();
        regla.setMaxIterations(3000);
        regla.setLearningRate(0.05);
        regla.setMomentum(0.7);
        regla.setMaxError(0.05);
        regla.addListener(e -> {
            MomentumBackpropagation bp = (MomentumBackpropagation) e.getSource();
            if (bp.getCurrentIteration() % 200 == 0)
                System.out.println("Iter " + bp.getCurrentIteration()
                        + " | Error " + bp.getTotalNetworkError());
        });
        red.setLearningRule(regla);

        long t = System.currentTimeMillis();
        red.learn(train);
        System.out.printf("Entrenado en %.1fs | error %.4f%n",
                (System.currentTimeMillis() - t) / 1000.0, regla.getTotalNetworkError());
        return red;
    }
    
    static void evaluar(NeuralNetwork red, DataSet test) {
        int tp = 0, tn = 0, fp = 0, fn = 0;
        double sumSq = 0, sumAbs = 0, sumReal = 0, sumPred = 0,
               sumRP = 0, sumRSq = 0, sumPSq = 0;
        int n = test.size();

        for (DataSetRow fila : test.getRows()) {
            red.setInput(fila.getInput());
            red.calculate();
            double pred = red.getOutput()[0];
            double real = fila.getDesiredOutput()[0];

            double err = real - pred;
            sumSq += err * err; sumAbs += Math.abs(err);
            sumReal += real; sumPred += pred;
            sumRP += real * pred; sumRSq += real * real; sumPSq += pred * pred;

            int p = pred >= UMBRAL ? 1 : 0, rr = (int) Math.round(real);
            if (p == 1 && rr == 1) tp++;
            else if (p == 0 && rr == 0) tn++;
            else if (p == 1) fp++;
            else fn++;
        }

        double acc = 100.0 * (tp + tn) / n;
        double prec = tp + fp == 0 ? 0 : 100.0 * tp / (tp + fp);
        double rec = tp + fn == 0 ? 0 : 100.0 * tp / (tp + fn);
        double f1 = prec + rec == 0 ? 0 : 2 * prec * rec / (prec + rec);

        double mediaReal = sumReal / n;
        double ssTot = 0;
        for (DataSetRow fila : test.getRows()) {
            double d = fila.getDesiredOutput()[0] - mediaReal;
            ssTot += d * d;
        }
        double r2 = ssTot == 0 ? 0 : 1 - sumSq / ssTot;
        double denom = Math.sqrt((n * sumRSq - sumReal * sumReal) * (n * sumPSq - sumPred * sumPred));
        double r = denom == 0 ? 0 : (n * sumRP - sumReal * sumPred) / denom;

        System.out.println("\n---- TEST (umbral " + UMBRAL + ") ----");
        System.out.println("           Pred NO   Pred SI");
        System.out.println("Real NO  |  " + tn + "        " + fp);
        System.out.println("Real SI  |  " + fn + "        " + tp);
        System.out.printf("Accuracy %.2f%% | Precision %.2f%% | Recall %.2f%% | F1 %.2f%%%n",
                acc, prec, rec, f1);
        System.out.printf("MSE %.4f | RMSE %.4f | MAE %.4f | R2 %.4f | r %.4f%n",
                sumSq / n, Math.sqrt(sumSq / n), sumAbs / n, r2, r);
    }
    
    static int indiceDe(String[] header, String nombre) {
        for (int i = 0; i < header.length; i++)
            if (header[i].trim().equalsIgnoreCase(nombre)) return i;
        return -1;
    }

    static boolean esDouble(String s) {
        try { Double.parseDouble(s); return true; }
        catch (NumberFormatException e) { return false; }
    }
}