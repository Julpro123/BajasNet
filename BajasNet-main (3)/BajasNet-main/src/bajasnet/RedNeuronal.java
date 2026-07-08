package bajasnet;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.neuroph.core.*;
import org.neuroph.core.data.*;
import org.neuroph.core.transfer.*;
import org.neuroph.nnet.*;
import org.neuroph.nnet.learning.*;

public class RedNeuronal {
    public static void main(String[] args) throws IOException {
        List<String> lineas = Files.readAllLines(Paths.get("DatasetTelcoChurn.csv"));
        String[] header = lineas.get(0).split(",", -1);
        int nCols = header.length;
        List<Map<String, Integer>> mapas = new ArrayList<>();
        for (int i = 0; i < nCols; i++) mapas.add(new HashMap<>());
        List<String> idsPorFila = new ArrayList<>();

        List<double[]> filasNumericas = new ArrayList<>();
        int nColsUtiles = 0;

        for (int i = 1; i < lineas.size(); i++) {
            String[] fila = lineas.get(i).split(",", -1);
            List<Double> filaNueva = new ArrayList<>();
            for (int col = 0; col < nCols; col++) {
                if (header[col].equalsIgnoreCase("customerID")) {
                    idsPorFila.add(fila[col].trim());
                    continue;
                }
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
            nColsUtiles = filaNueva.size();
            double[] arr = new double[filaNueva.size()];
            for (int j = 0; j < arr.length; j++) arr[j] = filaNueva.get(j);
            filasNumericas.add(arr);
        }

        double[] min = new double[nColsUtiles];
        double[] max = new double[nColsUtiles];
        Arrays.fill(min, Double.MAX_VALUE);
        Arrays.fill(max, -Double.MAX_VALUE);
        for (double[] fila : filasNumericas) {
            for (int c = 0; c < nColsUtiles; c++) {
                if (fila[c] < min[c]) min[c] = fila[c];
                if (fila[c] > max[c]) max[c] = fila[c];
            }
        }
        StringBuilder salida = new StringBuilder();
        for (double[] fila : filasNumericas) {
            StringBuilder filaTxt = new StringBuilder();
            for (int c = 0; c < nColsUtiles; c++) {
                double rango = max[c] - min[c];
                double norm = rango == 0 ? 0 : (fila[c] - min[c]) / rango;
                if (filaTxt.length() > 0) filaTxt.append(",");
                filaTxt.append(norm);
            }
            salida.append(filaTxt).append("\n");
        }
        Files.write(Paths.get("DatasetLimpio.csv"), salida.toString().getBytes());
        System.out.println("DatasetLimpio.csv generado y normalizado (0-1).\n");

        int nEntradas = nColsUtiles - 1;

        NeuralNetwork red = new MultiLayerPerceptron(nEntradas, 4,2, 1);
        

        DataSet dataset = DataSet.createFromFile("DatasetLimpio.csv", nEntradas, 1, ",");
        dataset.shuffle();
        DataSet[] particiones = dataset.createTrainingAndTestSubsets(0.4, 0.6);
        DataSet train = particiones[0];
        DataSet test = particiones[1];

        BackPropagation regla = new BackPropagation();
        regla.setMaxIterations(4000);
        regla.setLearningRate(0.1);
        regla.setMaxError(0.005);
        regla.addListener(event -> {
            BackPropagation bp = (BackPropagation) event.getSource();
            if (bp.getCurrentIteration() % 100 == 0) {
                System.out.println("Iteracion: " + bp.getCurrentIteration()
                        + " | Error: " + bp.getTotalNetworkError());
            }
        });
        red.setLearningRule(regla);

        long inicio = System.currentTimeMillis();
        red.learn(train);
        long fin = System.currentTimeMillis();
        System.out.println("Entrenamiento terminado en " + (fin - inicio) / 1000.0 + " segundos.");

        red.save("redEntrenada.nnet");

        int correctos = 0;
        for (DataSetRow fila : test.getRows()) {
            red.setInput(fila.getInput());
            red.calculate();
            double predicho = red.getOutput()[0];
            double real = fila.getDesiredOutput()[0];
            if ((predicho >= 0.5 ? 1 : 0) == (int) real) correctos++;
        }
        double precision = 100.0 * correctos / test.getRows().size();
        System.out.println("Precision en test: " + precision + "%");
    }
}