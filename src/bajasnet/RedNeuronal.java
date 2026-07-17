package bajasnet;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.neuroph.core.*;
import org.neuroph.core.data.*;
import org.neuroph.nnet.*;
import org.neuroph.nnet.learning.*;

/**
 * Entrena una red neuronal para predecir la baja (churn) de un cliente.
 *
 * El proceso completo está en main() y son 7 pasos:
 *   1. Leer el CSV.
 *   2. Analizar las columnas: cuáles son números y cuáles categorías.
 *   3. Convertir cada fila en un vector de números (normalizar + one-hot).
 *   4. Separar en entrenamiento (80%) y prueba (20%).
 *   5. Balancear el entrenamiento (misma cantidad de "se va" y "se queda").
 *   6. Entrenar la red y guardarla junto al preprocesamiento.
 *   7. Evaluar con el conjunto de prueba.
 *
 * Genera dos archivos que luego usa PrediccionServicio:
 *   - redEntrenada.nnet      : la red ya entrenada.
 *   - preprocesamiento.txt   : cómo transformar los datos de un cliente.
 */
public class RedNeuronal {

    static final String ARCHIVO_CSV  = "DatasetTelcoChurn.csv";
    static final String ARCHIVO_RED  = "redEntrenada.nnet";
    static final String ARCHIVO_PREP = "preprocesamiento.txt";

    static final double UMBRAL = 0.7;  
    static final double PORCENTAJE_TRAIN = 0.80;  
    static final Random RND = new Random(42); // Semilla Fija

    // ---- Estado del preprocesamiento ----
    static int nCols;                 // cantidad total de columnas del CSV
    static int idxId, idxChurn;       // posición de customerID y de Churn
    static int[]    columnaFeature;   // para cada feature, en qué columna del CSV está
    static String[] nombre;           // nombre de cada feature
    static boolean[] esNumerica;      // ¿la feature es numérica o categórica?
    static double[] min, max;         // rango (solo para features numéricas)
    static final List<List<String>> categorias = new ArrayList<>(); // valores posibles (categóricas)
    static int nEntradas;             // total de entradas de la red

    public static void main(String[] args) throws IOException {
        List<String[]> filas = leerCsv(ARCHIVO_CSV);
        String[] encabezado = filas.remove(0);

        // 2. Analizar columnas y preparar el preprocesamiento.
        inicializarPreprocesamiento(encabezado, filas);
        System.out.println("Cantidad de entradas de la red: " + nEntradas);

        // 3. Convertir cada fila del CSV en un ejemplo (vector de entrada + resultado esperado).
        List<DataSetRow> ejemplos = vectorizarTodo(filas);

        // 4. Mezclar y separar en entrenamiento / prueba.
        Collections.shuffle(ejemplos, RND);
        int corte = (int) Math.round(ejemplos.size() * PORCENTAJE_TRAIN);
        DataSet train = crearDataSet(nEntradas, ejemplos.subList(0, corte));
        DataSet test  = crearDataSet(nEntradas, ejemplos.subList(corte, ejemplos.size()));

        // 5. Balancear solo el entrenamiento (la prueba se deja como está).
        train = balancear(train, nEntradas);

        // 6. Entrenar la red y guardar todo.
        NeuralNetwork red = entrenar(train, nEntradas);
        red.save(ARCHIVO_RED);
        guardarPreprocesamiento(ARCHIVO_PREP);

        // 7. Evaluar con datos que la red nunca vio.
        evaluar(red, test);
    }

    // ====================================================================
    //  Lectura del CSV
    // ====================================================================

    /** Lee el CSV y devuelve una lista de filas; cada fila es un arreglo de celdas. */
    static List<String[]> leerCsv(String archivo) throws IOException {
        List<String[]> filas = new ArrayList<>();
        for (String linea : Files.readAllLines(Paths.get(archivo))) {
            if (linea.trim().isEmpty()) continue;
            filas.add(linea.split(",", -1));   // -1 = conservar celdas vacías del final
        }
        return filas;
    }

    // ====================================================================
    //  Preprocesamiento: convierte una fila de texto en un vector de números
    // ====================================================================

    /** Analiza las columnas del CSV y arma la configuración del preprocesamiento. */
    static void inicializarPreprocesamiento(String[] encabezado, List<String[]> filas) {
        nCols    = encabezado.length;
        idxId    = buscarColumna(encabezado, "customerID");
        idxChurn = buscarColumna(encabezado, "Churn");
        
        // Las features son todas las columnas MENOS el id y el churn.
        List<Integer> columnas = new ArrayList<>();
        for (int c = 0; c < nCols; c++)
            if (c != idxId && c != idxChurn) columnas.add(c);

        int nFeat = columnas.size();
        columnaFeature = new int[nFeat];
        nombre         = new String[nFeat];
        esNumerica     = new boolean[nFeat];
        min            = new double[nFeat];
        max            = new double[nFeat];
        categorias.clear();

        // Analizar cada feature para saber si es número o categoría.
        for (int f = 0; f < nFeat; f++) {
            int col = columnas.get(f);
            columnaFeature[f] = col;
            nombre[f] = encabezado[col].trim();
            analizarColumna(f, col, filas);
        }

        // Una entrada por cada feature numérica; para las categóricas, una
        // entrada por cada valor posible (codificación one-hot).
        int n = 0;
        for (int f = 0; f < nFeat; f++)
            n += esNumerica[f] ? 1 : categorias.get(f).size();
        nEntradas = n;
    }

    /** Decide si la columna es numérica o categórica y calcula su rango o sus categorías. */
    static void analizarColumna(int f, int col, List<String[]> filas) {
        boolean numerica = true;
        double menor = Double.MAX_VALUE, mayor = -Double.MAX_VALUE;
        LinkedHashSet<String> valoresVistos = new LinkedHashSet<>();

        for (String[] fila : filas) {
            if (fila.length != nCols) continue;
            String v = valor(fila, col);
            if (esNumero(v)) {
                double d = Double.parseDouble(v);
                menor = Math.min(menor, d);
                mayor = Math.max(mayor, d);
            } else {    
                numerica = false;   
            }
            valoresVistos.add(v);
        }
        System.out.println(valoresVistos);
        esNumerica[f] = numerica;
        min[f] = menor;
        max[f] = mayor;
        categorias.add(numerica ? List.of() : new ArrayList<>(valoresVistos));
    }

    /** Convierte todas las filas del CSV en ejemplos (entrada + resultado esperado). */
    static List<DataSetRow> vectorizarTodo(List<String[]> filas) {
        List<DataSetRow> ejemplos = new ArrayList<>();
        for (String[] fila : filas) {
            if (fila.length != nCols) continue;
            double[] entrada = vectorizar(fila);
            double objetivo = fila[idxChurn].trim().equalsIgnoreCase("Yes") ? 1 : 0;
            ejemplos.add(new DataSetRow(entrada, new double[]{ objetivo }));
        }
        return ejemplos;
    }

    /** Convierte UNA fila del CSV en el vector de números que entiende la red. */
    static double[] vectorizar(String[] fila) {
        double[] vector = new double[nEntradas];
        int idx = 0;
        for (int f = 0; f < columnaFeature.length; f++) {
            String v = valor(fila, columnaFeature[f]);
            if (esNumerica[f]) {
                vector[idx++] = normalizar(v, min[f], max[f]);
            } else {
                // one-hot: 1.0 en la categoría que coincide, 0.0 en el resto.
                for (String categoria : categorias.get(f))
                    vector[idx++] = categoria.equals(v) ? 1.0 : 0.0;
            }
        }
        return vector;
    }

    /** Lleva un número a la escala 0..1 según el rango visto en el entrenamiento. */
    static double normalizar(String texto, double min, double max) {
        double d = esNumero(texto) ? Double.parseDouble(texto) : 0;
        double rango = max - min;
        return rango == 0 ? 0 : (d - min) / rango;
    }

    /**
     * Guarda cómo preprocesar cada feature, una por línea. Formatos:
     *   NUM|nombre|min|max
     *   CAT|nombre|cat1;cat2;cat3
     * (PrediccionServicio lee exactamente este formato.)
     */
    static void guardarPreprocesamiento(String archivo) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo))) {
            for (int f = 0; f < nombre.length; f++) {
                if (esNumerica[f])
                    bw.write("NUM|" + nombre[f] + "|" + min[f] + "|" + max[f]);
                else
                    bw.write("CAT|" + nombre[f] + "|" + String.join(";", categorias.get(f)));
                bw.newLine();
            }
        }
    }

    /** Lee una celda; si está vacía la trata como "0". */
    static String valor(String[] fila, int col) {
        String v = fila[col].trim();
        return v.isEmpty() ? "0" : v;
    }

    // ====================================================================
    //  Armado de DataSets y balanceo
    // ====================================================================

    /** Crea un DataSet de Neuroph a partir de una lista de ejemplos. */
    static DataSet crearDataSet(int nEntradas, List<DataSetRow> ejemplos) {
        DataSet ds = new DataSet(nEntradas, 1);
        for (DataSetRow ejemplo : ejemplos) ds.add(ejemplo);
        return ds;
    }

    /**
     * Balancea las clases del entrenamiento por oversampling: repite al azar
     * ejemplos de "se va" (la clase minoritaria) hasta igualar a "se queda".
     */
    static DataSet balancear(DataSet train, int nEntradas) {
        List<DataSetRow> seVan = new ArrayList<>();
        List<DataSetRow> seQuedan = new ArrayList<>();
        for (DataSetRow r : train.getRows()) {
            if (r.getDesiredOutput()[0] >= 0.5) seVan.add(r);
            else                                 seQuedan.add(r);
        }

        DataSet balanceado = new DataSet(nEntradas, 1);
        for (DataSetRow r : seQuedan) balanceado.add(r);                 // todos los que se quedan
        for (int i = 0; i < seQuedan.size() && !seVan.isEmpty(); i++) {  // igual cantidad de "se van"
            balanceado.add(seVan.get(RND.nextInt(seVan.size())));
        }
        balanceado.shuffle();

        System.out.println("Entrenamiento balanceado: " + balanceado.size()
                + " ejemplos (se quedan=" + seQuedan.size() + ", se van=" + seVan.size() + ")");
        return balanceado;
    }

    // ====================================================================
    //  Entrenamiento
    // ====================================================================

    static NeuralNetwork entrenar(DataSet train, int nEntradas) {
        // Red: entradas -> capa oculta de 8 -> capa oculta de 4 -> 1 salida.
        NeuralNetwork red = new MultiLayerPerceptron(nEntradas, 4, 2, 1);

        MomentumBackpropagation regla = new MomentumBackpropagation();
        regla.setMaxIterations(2000);
        regla.setLearningRate(0.03);
        regla.setMomentum(0.8);
        regla.setMaxError(0.05);
        // Mostrar el progreso cada 200 iteraciones.
        regla.addListener(e -> {
            MomentumBackpropagation bp = (MomentumBackpropagation) e.getSource();
            if (bp.getCurrentIteration() % 200 == 0)
                System.out.println("Iteracion " + bp.getCurrentIteration()
                        + " | Error " + bp.getTotalNetworkError());
        });
        red.setLearningRule(regla);

        long inicio = System.currentTimeMillis();
        red.learn(train);
        System.out.printf("Entrenado en %.1fs | error final %.4f%n",
                (System.currentTimeMillis() - inicio) / 1000.0, regla.getTotalNetworkError());
        return red;
    }

    // ====================================================================
    //  Evaluación
    // ====================================================================

    static void evaluar(NeuralNetwork red, DataSet test) {
        int n = test.size();

        // Matriz de confusión.
        int tp = 0, tn = 0, fp = 0, fn = 0;  // verdaderos/falsos positivos/negativos
        // Acumuladores para las métricas de regresión.
        double sumaError2 = 0, sumaErrorAbs = 0;
        double sumaReal = 0, sumaPred = 0, sumaRealPred = 0, sumaReal2 = 0, sumaPred2 = 0;

        for (DataSetRow fila : test.getRows()) {
            red.setInput(fila.getInput());
            red.calculate();
            double pred = red.getOutput()[0];
            double real = fila.getDesiredOutput()[0];

            double error = real - pred;
            sumaError2   += error * error;
            sumaErrorAbs += Math.abs(error);
            sumaReal     += real;
            sumaPred     += pred;
            sumaRealPred += real * pred;
            sumaReal2    += real * real;
            sumaPred2    += pred * pred;

            boolean prediceSeVa = pred >= UMBRAL;
            boolean realmenteSeVa = real >= 0.5;
            if      (prediceSeVa  && realmenteSeVa)  tp++;
            else if (!prediceSeVa && !realmenteSeVa) tn++;
            else if (prediceSeVa)                    fp++;
            else                                     fn++;
        }

        // Métricas de clasificación.
        double accuracy  = 100.0 * (tp + tn) / n;
        double precision = (tp + fp == 0) ? 0 : 100.0 * tp / (tp + fp);
        double recall    = (tp + fn == 0) ? 0 : 100.0 * tp / (tp + fn);
        double f1        = (precision + recall == 0) ? 0 : 2 * precision * recall / (precision + recall);

        // Métricas de regresión (qué tan cerca estuvo la probabilidad del valor real).
        double mse  = sumaError2 / n;
        double rmse = Math.sqrt(mse);
        double mae  = sumaErrorAbs / n;

        double mediaReal = sumaReal / n;
        double sumaTotal = 0;
        for (DataSetRow fila : test.getRows()) {
            double d = fila.getDesiredOutput()[0] - mediaReal;
            sumaTotal += d * d;
        }
        double r2 = (sumaTotal == 0) ? 0 : 1 - sumaError2 / sumaTotal;

        double denom = Math.sqrt((n * sumaReal2 - sumaReal * sumaReal)
                               * (n * sumaPred2 - sumaPred * sumaPred));
        double correlacion = (denom == 0) ? 0 : (n * sumaRealPred - sumaReal * sumaPred) / denom;

        System.out.println("\n---- PRUEBA (umbral " + UMBRAL + ") ----");
        System.out.println("           Pred NO   Pred SI");
        System.out.println("Real NO  |  " + tn + "        " + fp);
        System.out.println("Real SI  |  " + fn + "        " + tp);
        System.out.printf("Accuracy %.2f%% | Precision %.2f%% | Recall %.2f%% | F1 %.2f%%%n",
                accuracy, precision, recall, f1);
        System.out.printf("MSE %.4f | RMSE %.4f | MAE %.4f | R2 %.4f | correlacion %.4f%n",
                mse, rmse, mae, r2, correlacion);
    }

    // ====================================================================
    //  Utilidades
    // ====================================================================

    /** Devuelve el índice de una columna por nombre, o -1 si no existe. */
    static int buscarColumna(String[] encabezado, String nombre) {
        for (int i = 0; i < encabezado.length; i++)
            if (encabezado[i].trim().equalsIgnoreCase(nombre)) return i;
        return -1;
    }

    /** ¿El texto se puede interpretar como número? */
    static boolean esNumero(String s) {
        try { Double.parseDouble(s); return true; }
        catch (NumberFormatException e) { return false; }
    }
}