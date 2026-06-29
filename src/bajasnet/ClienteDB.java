package bajasnet;
import java.io.*;
import java.util.*;

public class ClienteDB {

    private static final String ARCHIVO = "clientes.txt";
    private static List<Cliente> clientes = new ArrayList<>();

    public static void inicializar() {
        if (new File(ARCHIVO).exists()) {
            cargar();
        } else {
            guardar();
        }
    }

    public static List<Cliente> getClientes() {
        return Collections.unmodifiableList(clientes);
    }

    private static void cargar() {
        clientes.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split("\\|");
                if (campos.length == 6) {
                    clientes.add(new Cliente(
                        Integer.parseInt(campos[0]),
                        campos[1], campos[2], campos[3], campos[4], campos[5]
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void guardar() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO))) {
            for (Cliente c : clientes) {
                bw.write(c.getId() + "|" + c.getNombre() + "|" + c.getApellido() + "|" +
                         c.getDni() + "|" + c.getEmail() + "|" + c.getTelefono());
                bw.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Cliente buscarCliente(int id) {
        for (Cliente c : clientes) {
            if (c.getId() == id) return c;
        }
        return null;
    }

    public static Cliente buscarClientePorDni(String dni) {
        for (Cliente c : clientes) {
            if (c.getDni().equals(dni)) return c;
        }
        return null;
    }

    public static int generarId() {
        return clientes.isEmpty() ? 1 : clientes.stream().mapToInt(Cliente::getId).max().getAsInt() + 1;
    }

    public static void registrarCliente(Cliente c) {
        clientes.add(c);
        guardar();
    }

    public static void modificarCliente(int idOriginal, Cliente nuevo) {
        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getId() == idOriginal) {
                clientes.set(i, nuevo);
                break;
            }
        }
        guardar();
    }

    public static boolean eliminarCliente(int id) {
        boolean encontrado = clientes.removeIf(c -> c.getId() == id);
        if (encontrado) guardar();
        return encontrado;
    }
}
