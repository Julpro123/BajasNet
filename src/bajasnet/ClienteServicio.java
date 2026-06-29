package bajasnet;
import java.util.List;

public class ClienteServicio {

    public static void inicializar() {
        ClienteDB.inicializar();
    }

    public static List<Cliente> getClientes() {
        return ClienteDB.getClientes();
    }

    public static boolean hayClientes() {
        return !ClienteDB.getClientes().isEmpty();
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
        if (!telefono.matches("^\\d{7,15}$")) {
            return "El teléfono debe tener entre 7 y 15 dígitos.";
        }
        Cliente existente = ClienteDB.buscarClientePorDni(dni);
        if (existente != null && existente.getId() != idOriginal) {
            return "Ya existe un cliente con ese DNI.";
        }
        ClienteDB.modificarCliente(idOriginal, new Cliente(idOriginal, nombre, apellido, dni, email, telefono));
        return null;
    }

    public static boolean eliminarCliente(int id) {
        return ClienteDB.eliminarCliente(id);
    }
}
