package bajasnet;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ClienteDB {

    private static final String ARCHIVO = "clientes.txt";
    private static final int CANTIDAD_CAMPOS = 20;

    public static int importarDesdeCsv(String csvArchivo) {
        int importados = 0;
        try {
            Set<String> existentes = new HashSet<>();
            for (Cliente c : listarClientes()) existentes.add(c.getIdCliente());

            List<String> lineas = Files.readAllLines(Paths.get(csvArchivo));
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO, true))) {
                for (int i = 1; i < lineas.size(); i++) {   // i=0 es el header
                    String linea = lineas.get(i).trim();
                    if (linea.isEmpty()) continue;
                    String[] c = linea.split(",", -1);
                    if (c.length < CANTIDAD_CAMPOS) continue;   // fila malformada

                    String id = c[0].trim();
                    if (id.isEmpty() || existentes.contains(id)) continue;

                    String[] campos = new String[CANTIDAD_CAMPOS];   // customerID + 19 features (sin Churn)
                    for (int k = 0; k < CANTIDAD_CAMPOS; k++) {
                        String v = c[k].trim();
                        campos[k] = v.isEmpty() ? "0" : v;           // hueco (TotalCharges) -> 0
                    }
                    bw.write(String.join("|", campos));
                    bw.newLine();
                    existentes.add(id);
                    importados++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return importados;
    }

    public static List<Cliente> listarClientes() {
        List<Cliente> resultado = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] c = linea.split("\\|", -1);
                if (c.length == CANTIDAD_CAMPOS) {
                    resultado.add(crearCliente(c));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultado;
    }

    public static Cliente buscarCliente(String idCliente) {
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] c = linea.split("\\|", -1);
                if (c.length == CANTIDAD_CAMPOS && c[0].equals(idCliente)) {
                    return crearCliente(c);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void registrarCliente(Cliente c) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO, true))) {
            bw.write(lineaDeCliente(c));
            bw.newLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void modificarCliente(String idOriginal, Cliente nuevo) {
        List<String> lineas = new ArrayList<>();
        boolean encontrado = false;
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] c = linea.split("\\|", -1);
                if (c.length == CANTIDAD_CAMPOS && c[0].equals(idOriginal)) {
                    lineas.add(lineaDeCliente(nuevo));
                    encontrado = true;
                } else {
                    lineas.add(linea);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (!encontrado) return;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO))) {
            for (String linea : lineas) {
                bw.write(linea);
                bw.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean eliminarCliente(String customerID) {
        List<String> lineas = new ArrayList<>();
        boolean encontrado = false;
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] c = linea.split("\\|", -1);
                if (c.length == CANTIDAD_CAMPOS && c[0].equals(customerID)) {
                    encontrado = true;
                    continue;
                }
                lineas.add(linea);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        if (!encontrado) return false;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO))) {
            for (String linea : lineas) {
                bw.write(linea);
                bw.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private static Cliente crearCliente(String[] c) {
        return new Cliente(c[0], c[1], c[2], c[3], c[4], c[5], c[6], c[7], c[8], c[9],
                c[10], c[11], c[12], c[13], c[14], c[15], c[16], c[17], c[18], c[19]);
    }

    private static String lineaDeCliente(Cliente c) {
        return String.join("|", c.toArray());
    }
}