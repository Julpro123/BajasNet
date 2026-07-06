package bajasnet;

import java.util.List;

public class ClienteServicio {

    public static List<Cliente> listarClientes() {
        return ClienteDB.listarClientes();
    }

    public static Cliente buscarCliente(int id) {
        return ClienteDB.buscarCliente(id);
    }

    public static String validarDatos(String nombre, String apellido, String dni, String email, String telefono) {
        if (nombre.isEmpty() || apellido.isEmpty() || dni.isEmpty() || email.isEmpty() || telefono.isEmpty()) {
            return "Todos los campos son requeridos.";
        }
        if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) {
            return "El nombre solo puede contener letras.";
        }
        if (!apellido.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) {
            return "El apellido solo puede contener letras.";
        }
        if (!dni.matches("^\\d{7,8}$")) {
            return "El DNI debe tener 7 u 8 dígitos numéricos.";
        }
        if (!email.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            return "El email no tiene un formato válido.";
        }
        String dominio = email.substring(email.indexOf("@") + 1).toLowerCase();
        if (!dominio.equals("gmail.com") && distanciaLevenshtein(dominio, "gmail.com") <= 2) {
            return "El dominio parece estar mal escrito.";
        }
        if (!telefono.matches("^\\d{7,15}$")) {
            return "El teléfono debe tener entre 7 y 15 dígitos.";
        }
        if (ClienteDB.buscarClientePorDni(dni) != null) {
            return "Ya existe un cliente con ese DNI.";
        }
        ClienteDB.registrarCliente(new Cliente(ClienteDB.generarId(), nombre, apellido, dni, email, telefono));
        return null;
    }

    public static String validarDatos(int idOriginal, String nombre, String apellido, String dni, String email, String telefono) {
        if (nombre.isEmpty() || apellido.isEmpty() || dni.isEmpty() || email.isEmpty() || telefono.isEmpty()) {
            return "Todos los campos son requeridos.";
        }
        if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) {
            return "El nombre solo puede contener letras.";
        }
        if (!apellido.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) {
            return "El apellido solo puede contener letras.";
        }
        if (!dni.matches("^\\d{7,8}$")) {
            return "El DNI debe tener 7 u 8 dígitos numéricos.";
        }
        if (!email.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            return "El email no tiene un formato válido.";
        }
        String dominio = email.substring(email.indexOf("@") + 1).toLowerCase();
        if (!dominio.equals("gmail.com") && distanciaLevenshtein(dominio, "gmail.com") <= 2) {
            return "El dominio parece estar mal escrito.";
        }
        if (!telefono.matches("^\\d{7,15}$")) {
            return "El teléfono debe tener entre 7 y 15 dígitos.";
        }
        Cliente existente = ClienteDB.buscarClientePorDni(dni);
        if (existente != null && existente.getId() != idOriginal) {
            return "Ya existe un cliente con ese DNsu -I.";
        }
        ClienteDB.modificarCliente(idOriginal, new Cliente(idOriginal, nombre, apellido, dni, email, telefono));
        return null;
    }

    public static boolean eliminarCliente(int id) {
        return ClienteDB.eliminarCliente(id);
    }

    private static int distanciaLevenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) dp[i][j] = j;
                else if (j == 0) dp[i][j] = i;
                else if (a.charAt(i - 1) == b.charAt(j - 1)) dp[i][j] = dp[i - 1][j - 1];
                else dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
            }
        }
        return dp[a.length()][b.length()];
    }
}