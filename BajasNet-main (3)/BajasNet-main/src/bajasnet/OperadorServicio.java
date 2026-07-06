package bajasnet;

import java.util.List;

public class OperadorServicio {

    public static List<Operador> listarOperadores() {
        return OperadorDB.listarOperadores();
    }

    public static Operador buscarOperador(String email) {
        return OperadorDB.buscarOperador(email);
    }

    public static Operador validarLogin(String email, String password) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            return null;
        }
        if ("admin".equals(email) && "admin".equals(password)) {
            return new Operador("admin", "admin", "Admin", "Sistema", "00000000", true);
        }
        if (!email.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            return null;
        }
        if (password.length() < 6) {
            return null;
        }
        return OperadorDB.buscarLogin(email, password);
    }

    public static String validarDatos(String nombre, String apellido, String email, String password, String dni) {
        if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty() || password.isEmpty() || dni.isEmpty()) {
            return "Todos los campos son requeridos.";
        }
        if (!email.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            return "El email no tiene un formato válido.";
        }
        String dominio = email.substring(email.indexOf("@") + 1).toLowerCase();
        if (!dominio.equals("gmail.com") && distanciaLevenshtein(dominio, "gmail.com") <= 2) {
            return "El dominio parece estar mal escrito.";
        }
        if (password.length() < 6) {
            return "La contraseña debe tener al menos 6 caracteres.";
        }
        if (!dni.matches("^\\d{7,8}$")) {
            return "El DNI debe tener 7 u 8 dígitos numéricos.";
        }
        if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) {
            return "El nombre solo puede contener letras.";
        }
        if (!apellido.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) {
            return "El apellido solo puede contener letras.";
        }
        if (OperadorDB.buscarOperador(email) != null) {
            return "Ya existe un operador con ese email.";
        }
        OperadorDB.registrarOperador(new Operador(email, password, nombre, apellido, dni));
        return null;
    }

    public static String validarDatos(String emailOriginal, String nombre, String apellido, String email, String password, String dni) {
        if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty() || password.isEmpty() || dni.isEmpty()) {
            return "Todos los campos son requeridos.";
        }
        if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) {
            return "El nombre solo puede contener letras.";
        }
        if (!apellido.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) {
            return "El apellido solo puede contener letras.";
        }
        if (!email.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            return "El email no tiene un formato válido.";
        }
        String dominio = email.substring(email.indexOf("@") + 1).toLowerCase();
        if (!dominio.equals("gmail.com") && distanciaLevenshtein(dominio, "gmail.com") <= 2) {
            return "El dominio parece estar mal escrito.";
        }
        if (password.length() < 6) {
            return "La contraseña debe tener al menos 6 caracteres.";
        }
        if (!dni.matches("^\\d{7,8}$")) {
            return "El DNI debe tener 7 u 8 dígitos numéricos.";
        }
        if (!email.equals(emailOriginal) && OperadorDB.buscarOperador(email) != null) {
            return "Ya existe un operador con ese email.";
        }
        OperadorDB.modificarOperador(emailOriginal, new Operador(email, password, nombre, apellido, dni));
        return null;
    }

    public static boolean eliminarOperador(String email) {
        return OperadorDB.eliminarOperador(email);
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