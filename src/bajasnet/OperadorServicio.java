package bajasnet;

import java.util.List;

public class OperadorServicio {

    private static final String REGEX_EMAIL  = "^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$";
    private static final String REGEX_DNI     = "^\\d{7,8}$";
    private static final String REGEX_LETRAS  = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$";

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

        if (!email.matches(REGEX_EMAIL)) {
            return null;
        }

        if (password.length() < 6) {
            return null;
        }

        return OperadorDB.buscarLogin(email, password);
    }

    public static String validarDatos(String emailOriginal, String nombre, String apellido, String email,
                                        String password, String dni) {
        if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty() || password.isEmpty() || dni.isEmpty()) {
            return "Todos los campos son requeridos.";
        }

        if (!email.matches(REGEX_EMAIL)) {
            return "El email no tiene un formato válido.";
        }

        if (password.length() < 6) {
            return "La contraseña debe tener al menos 6 caracteres.";
        }

        if (!dni.matches(REGEX_DNI)) {
            return "El DNI debe tener 7 u 8 dígitos numéricos.";
        }

        if (!nombre.matches(REGEX_LETRAS)) {
            return "El nombre solo puede contener letras.";
        }

        if (!apellido.matches(REGEX_LETRAS)) {
            return "El apellido solo puede contener letras.";
        }

        if ((emailOriginal == null || !email.equals(emailOriginal))
                && OperadorDB.buscarOperador(email) != null) {
            return "Ya existe un operador con ese email.";
        }

        Operador operador = new Operador(email, password, nombre, apellido, dni);
        if (emailOriginal == null) {
            OperadorDB.registrarOperador(operador);      // alta
        } else {
            OperadorDB.modificarOperador(emailOriginal, operador);  // modificación
        }
        return null;
    }

    public static boolean eliminarOperador(String email) {
        return OperadorDB.eliminarOperador(email);
    }
}
