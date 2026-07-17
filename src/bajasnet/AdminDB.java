package bajasnet;

import java.io.*;

/**
 * Acceso a datos de los administradores. Los admins se guardan en admins.txt,
 * un registro por línea con los campos separados por '|':
 *
 *   email|password|nombre|apellido|dni
 *
 * Hereda de {@link OperadorDB}: reutiliza su comportamiento y agrega las
 * búsquedas propias de admin sobre su propio archivo.
 */
public class AdminDB extends OperadorDB {

    private static final String ARCHIVO = "admins.txt";
    private static final int CANTIDAD_CAMPOS = 5;

    /** Busca un admin por email y contraseña. Devuelve el Admin o null si no existe. */
    public static Admin buscarLogin(String email, String password) {
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] c = linea.split("\\|");
                if (c.length == CANTIDAD_CAMPOS && c[0].equals(email) && c[1].equals(password)) {
                    return new Admin(c[0], c[1], c[2], c[3], c[4]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
