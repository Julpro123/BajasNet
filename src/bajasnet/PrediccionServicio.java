package bajasnet;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.neuroph.core.NeuralNetwork;

public class PrediccionServicio {

    private static final String ARCHIVO_RED = "redEntrenada.nnet";
    private static final String ARCHIVO_PREPROC = "preprocesamiento.txt";

    private static NeuralNetwork red;
    private static List<String[]> preproc;   // una entrada por feature, en orden: {tipo, nombre, ...}

    private static void cargar() throws IOException {
        if (red != null) return;
        if (!Files.exists(Paths.get(ARCHIVO_RED)) || !Files.exists(Paths.get(ARCHIVO_PREPROC))) {
            throw new IllegalStateException(
                "Falta la red entrenada. Ejecutá RedNeuronal una vez para generar '"
                + ARCHIVO_RED + "' y '" + ARCHIVO_PREPROC + "'.");
        }
        red = NeuralNetwork.createFromFile(ARCHIVO_RED);
        preproc = new ArrayList<>();
        for (String linea : Files.readAllLines(Paths.get(ARCHIVO_PREPROC))) {
            if (!linea.trim().isEmpty()) preproc.add(linea.split("\\|", -1));
        }
    }

   
    public static double predecirChurn(Cliente cliente) throws IOException {
        cargar();
        double[] entrada = vectorizar(cliente.toArray());
        red.setInput(entrada);
        red.calculate();
        return red.getOutput()[0];
    }

    
    private static double[] vectorizar(String[] datosCliente) {
        List<Double> v = new ArrayList<>();
        for (int k = 0; k < preproc.size(); k++) {
            String[] p = preproc.get(k);
            String valor = datosCliente[k + 1] == null ? "0" : datosCliente[k + 1].trim();
            if (valor.isEmpty()) valor = "0";

            if (p[0].equals("NUM")) {
                double min = Double.parseDouble(p[2]);
                double max = Double.parseDouble(p[3]);
                double rango = max - min;
                double val;
                try { val = Double.parseDouble(valor); } catch (NumberFormatException e) { val = 0; }
                v.add(rango == 0 ? 0 : (val - min) / rango);
            } else { 
                String[] vocab = (p.length > 2 && !p[2].isEmpty()) ? p[2].split(";", -1) : new String[0];
                for (String cat : vocab) v.add(cat.equals(valor) ? 1.0 : 0.0);
            }
        }
        double[] arr = new double[v.size()];
        for (int i = 0; i < arr.length; i++) arr[i] = v.get(i);
        return arr;
    }

    public static String nivelRiesgo(double prob) {
        if (prob >= 0.70) return "ALTO";
        if (prob >= 0.45) return "MEDIO";
        return "BAJO";
    }

    public static String recomendacion(double prob) {
        if (prob >= 0.70) return "Riesgo alto de baja: ofrecer una promoción de retención agresiva.";
        if (prob >= 0.45) return "Riesgo medio: ofrecer una promoción moderada.";
        return "Riesgo bajo: no requiere promoción por ahora.";
    }
}
