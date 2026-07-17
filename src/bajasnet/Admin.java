package bajasnet;

/**
 * Un Admin es un Operador con permisos ampliados (acceder a la gestión de
 * operadores). Hereda todos los datos y getters de {@link Operador}.
 *
 * La lógica propia del admin (buscar/validar login) vive en {@link AdminServicio}
 * y {@link AdminDB}, siguiendo el mismo patrón que Operador.
 */
public class Admin extends Operador {

    public Admin(String email, String password, String nombre, String apellido, String dni) {
        super(email, password, nombre, apellido, dni);
    }
}
