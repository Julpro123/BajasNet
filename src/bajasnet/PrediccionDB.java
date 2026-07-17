package bajasnet;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Historial / auditoría de predicciones.
 *
 * Cada predicción se guarda como una línea en predicciones.txt, con los campos
 * separados por '|':
 *
 *   idRegistro | fechaHora | emailOperador | probabilidad% | riesgo | recomendacion | (20 campos del cliente)
 *
 * El snapshot del cliente (los 20 campos, empezando por customerID) queda
 * guardado tal como estaba al momento de predecir: si el cliente cambia
 * después, el registro sigue mostrando con qué datos se predijo.
 */
public class PrediccionDB {

    private static final String ARCHIVO = "predicciones.txt";
    private static final int CAMPOS_CLIENTE = 20;                 // customerID + 19 features
    private static final int CAMPOS_FIJOS   = 6;                  // id, fecha, email, prob, riesgo, recomendacion
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Agrega una predicción al historial. Genera el id correlativo y la fecha/hora.
     * Devuelve el registro guardado. Lanza IOException si no se pudo escribir
     * (para que la app lo informe: en una auditoría un fallo no debe pasar en silencio).
     */
    public static Prediccion registrar(Cliente cliente, String emailOperador,
                                       double probabilidad, String riesgo,
                                       String recomendacion) throws IOException {
        String id = String.valueOf(siguienteId());
        String fechaHora = LocalDateTime.now().format(FORMATO_FECHA);

        Prediccion p = new Prediccion(id, fechaHora, emailOperador,
                probabilidad, riesgo, recomendacion, cliente);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO, true))) {
            bw.write(aLinea(p));
            bw.newLine();
        }
        return p;
    }

    /** Devuelve todas las predicciones guardadas, de la más vieja a la más nueva. */
    public static List<Prediccion> listar() {
        List<Prediccion> resultado = new ArrayList<>();
        if (!Files.exists(Paths.get(ARCHIVO))) return resultado;
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                Prediccion p = aPrediccion(linea);
                if (p != null) resultado.add(p);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultado;
    }

    /** Próximo id correlativo = mayor id existente + 1 (1 si el archivo está vacío). */
    private static int siguienteId() {
        int max = 0;
        for (Prediccion p : listar()) {
            try { max = Math.max(max, Integer.parseInt(p.getIdRegistro())); }
            catch (NumberFormatException ignore) { /* línea vieja o rara: la salteamos */ }
        }
        return max + 1;
    }

    /** Arma la línea de texto de una predicción. */
    private static String aLinea(Prediccion p) {
        List<String> campos = new ArrayList<>();
        campos.add(p.getIdRegistro());
        campos.add(p.getFechaHora());
        campos.add(p.getEmailOperador());
        campos.add(String.format(Locale.US, "%.1f", p.getProbabilidad() * 100));
        campos.add(p.getRiesgo());
        campos.add(p.getRecomendacion());
        campos.addAll(Arrays.asList(p.getCliente().toArray())); // snapshot: los 20 campos del cliente
        return String.join("|", campos);
    }

    /** Reconstruye una predicción desde una línea (o null si está malformada). */
    private static Prediccion aPrediccion(String linea) {
        String[] c = linea.split("\\|", -1);
        if (c.length != CAMPOS_FIJOS + CAMPOS_CLIENTE) return null;

        String[] d = Arrays.copyOfRange(c, CAMPOS_FIJOS, c.length); // 20 campos del cliente
        Cliente cliente = new Cliente(d[0], d[1], d[2], d[3], d[4], d[5], d[6], d[7], d[8], d[9],
                d[10], d[11], d[12], d[13], d[14], d[15], d[16], d[17], d[18], d[19]);

        double prob;
        try { prob = Double.parseDouble(c[3]) / 100.0; }
        catch (NumberFormatException e) { prob = 0; }

        return new Prediccion(c[0], c[1], c[2], prob, c[4], c[5], cliente);
    }
}
