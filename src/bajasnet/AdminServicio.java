package bajasnet;

/**
 * Lógica de negocio de los administradores: valida y busca el login de admin.
 * Mismo patrón que {@link OperadorServicio}, pero para la clase {@link Admin}.
 */
public class AdminServicio {

    /**
     * Valida el login de un administrador.
     * Devuelve el Admin si las credenciales son correctas, o null si no.
     */
    public static Admin validarLogin(String email, String password) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            return null;
        }
        return AdminDB.buscarLogin(email, password);
    }

}
