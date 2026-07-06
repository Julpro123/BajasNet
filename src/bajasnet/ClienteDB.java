package bajasnet;
import java.io.*;
import java.util.*;

public class ClienteDB {

    private static final String ARCHIVO = "clientes.txt";

    public static List<Cliente> listarClientes() {
        List<Cliente> resultado = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split("\\|");
                if (campos.length == 6) {
                    resultado.add(new Cliente(Integer.parseInt(campos[0]),
                        campos[1], campos[2], campos[3], campos[4], campos[5]));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultado;
    }

    public static Cliente buscarCliente(int id) {
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split("\\|");
                if (campos.length == 6 && Integer.parseInt(campos[0]) == id) {
                    return new Cliente(Integer.parseInt(campos[0]),
                        campos[1], campos[2], campos[3], campos[4], campos[5]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Cliente buscarClientePorDni(String dni) {
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split("\\|");
                if (campos.length == 6 && campos[3].equals(dni)) {
                    return new Cliente(Integer.parseInt(campos[0]),
                        campos[1], campos[2], campos[3], campos[4], campos[5]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int generarId() {
        int maxId = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split("\\|");
                if (campos.length == 6) {
                    maxId = Math.max(maxId, Integer.parseInt(campos[0]));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return maxId + 1;
    }

    public static void registrarCliente(Cliente c) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO, true))) {
            bw.write(c.getId() + "|" + c.getNombre() + "|" + c.getApellido() + "|" +
                     c.getDni() + "|" + c.getEmail() + "|" + c.getTelefono());
            bw.newLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void modificarCliente(int idOriginal, Cliente nuevo) {
        List<String> lineas = new ArrayList<>();
        boolean encontrado = false;
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split("\\|");
                if (campos.length == 6 && Integer.parseInt(campos[0]) == idOriginal) {
                    lineas.add(nuevo.getId() + "|" + nuevo.getNombre() + "|" + nuevo.getApellido() + "|" +
                               nuevo.getDni() + "|" + nuevo.getEmail() + "|" + nuevo.getTelefono());
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

    public static boolean eliminarCliente(int id) {
        List<String> lineas = new ArrayList<>();
        boolean encontrado = false;
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split("\\|");
                if (campos.length == 6 && Integer.parseInt(campos[0]) == id) {
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
}
