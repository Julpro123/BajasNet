package bajasnet;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Acceso a datos de las asignaciones de promociones a clientes.
 * Se guardan en asignaciones.txt, un registro por línea:
 *
 *   idCliente|idPromocion
 */
public class AsignacionDB {

    private static final String ARCHIVO = "asignaciones.txt";
    private static final int CANTIDAD_CAMPOS = 2;

    /** Agrega una asignación (cliente ↔ promoción) al archivo. */
    public static void registrar(String idCliente, String idPromocion) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO, true))) {
            bw.write(idCliente + "|" + idPromocion);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Indica si esa promoción ya está asignada a ese cliente. */
    public static boolean yaAsignada(String idCliente, String idPromocion) {
        if (!Files.exists(Paths.get(ARCHIVO))) return false;
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] c = linea.split("\\|", -1);
                if (c.length == CANTIDAD_CAMPOS
                        && c[0].equals(idCliente) && c[1].equals(idPromocion)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Elimina la asignación de esa promoción a ese cliente. Devuelve true si la borró. */
    public static boolean eliminar(String idCliente, String idPromocion) {
        if (!Files.exists(Paths.get(ARCHIVO))) return false;
        List<String> lineas = new ArrayList<>();
        boolean encontrado = false;
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] c = linea.split("\\|", -1);
                if (c.length == CANTIDAD_CAMPOS && c[0].equals(idCliente) && c[1].equals(idPromocion)) {
                    encontrado = true;   // esta línea NO se vuelve a escribir
                    continue;
                }
                lineas.add(linea);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        if (!encontrado) return false;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO))) {
            for (String l : lineas) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /** Devuelve los IDs de promociones asignadas a un cliente. */
    public static List<String> promocionesDeCliente(String idCliente) {
        List<String> ids = new ArrayList<>();
        if (!Files.exists(Paths.get(ARCHIVO))) return ids;
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] c = linea.split("\\|", -1);
                if (c.length == CANTIDAD_CAMPOS && c[0].equals(idCliente)) {
                    ids.add(c[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ids;
    }
}
