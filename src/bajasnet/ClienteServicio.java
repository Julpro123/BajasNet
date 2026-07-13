package bajasnet;
import java.util.List;
import java.util.Random;

public class ClienteServicio {

    public static List<Cliente> listarClientes() {
        return ClienteDB.listarClientes();
    }

    public static Cliente buscarCliente(String idCliente) {
        return ClienteDB.buscarCliente(idCliente);
    }

    /**
     * Valida los datos y, si son correctos, da de alta o modifica el cliente.
     * Si {@code idOriginal} es null es un alta (el id se genera solo); en caso
     * contrario, la modificación del cliente con ese id.
     * Devuelve el mensaje de error, o null si fue exitoso.
     */
    public static String validarDatos(String idOriginal, String genero, String ciudadanoMayor,
            String pareja, String dependientes, String antiguedad, String servicioTelefonico, String lineasMultiples,
            String servicioInternet, String seguridadOnline, String respaldoOnline, String proteccionDispositivo,
            String soporteTecnico, String streamingTv, String streamingPeliculas, String contrato,
            String facturaSinPapel, String metodoPago, String cargosMensuales, String cargosTotales) {

        if (genero.isEmpty() || ciudadanoMayor.isEmpty() || pareja.isEmpty()
                || dependientes.isEmpty() || antiguedad.isEmpty() || servicioTelefonico.isEmpty() || servicioInternet.isEmpty()
                || contrato.isEmpty() || facturaSinPapel.isEmpty() || metodoPago.isEmpty()
                || cargosMensuales.isEmpty() || cargosTotales.isEmpty()) {
            return "Todos los campos son requeridos.";
        }
        if (!genero.matches("^(Male|Female)$")) {
            return "El género debe ser 'Male' o 'Female'.";
        }
        if (!ciudadanoMayor.matches("^[01]$")) {
            return "Ciudadano mayor debe ser 0 o 1.";
        }
        if (!pareja.matches("^(Yes|No)$")) {
            return "Pareja debe ser 'Yes' o 'No'.";
        }
        if (!dependientes.matches("^(Yes|No)$")) {
            return "Dependientes debe ser 'Yes' o 'No'.";
        }
        if (!antiguedad.matches("^\\d+$")) {
            return "La antigüedad debe ser un número entero de meses.";
        }
        if (!servicioTelefonico.matches("^(Yes|No)$")) {
            return "Servicio telefónico debe ser 'Yes' o 'No'.";
        }
        if (lineasMultiples != null && !lineasMultiples.isEmpty()
                && !lineasMultiples.matches("^(Yes|No|No phone service)$")) {
            return "Líneas múltiples no tiene un valor válido.";
        }
        if (!servicioInternet.matches("^(DSL|Fiber optic|No)$")) {
            return "Servicio de internet no tiene un valor válido.";
        }
        if (seguridadOnline != null && !seguridadOnline.isEmpty()
                && !seguridadOnline.matches("^(Yes|No|No internet service)$")) {
            return "Seguridad online no tiene un valor válido.";
        }
        if (respaldoOnline != null && !respaldoOnline.isEmpty()
                && !respaldoOnline.matches("^(Yes|No|No internet service)$")) {
            return "Respaldo online no tiene un valor válido.";
        }
        if (proteccionDispositivo != null && !proteccionDispositivo.isEmpty()
                && !proteccionDispositivo.matches("^(Yes|No|No internet service)$")) {
            return "Protección de dispositivo no tiene un valor válido.";
        }
        if (soporteTecnico != null && !soporteTecnico.isEmpty()
                && !soporteTecnico.matches("^(Yes|No|No internet service)$")) {
            return "Soporte técnico no tiene un valor válido.";
        }
        if (streamingTv != null && !streamingTv.isEmpty()
                && !streamingTv.matches("^(Yes|No|No internet service)$")) {
            return "Streaming TV no tiene un valor válido.";
        }
        if (streamingPeliculas != null && !streamingPeliculas.isEmpty()
                && !streamingPeliculas.matches("^(Yes|No|No internet service)$")) {
            return "Streaming películas no tiene un valor válido.";
        }
        if (!contrato.matches("^(Month-to-month|One year|Two year)$")) {
            return "Contrato no tiene un valor válido.";
        }
        if (!facturaSinPapel.matches("^(Yes|No)$")) {
            return "Factura sin papel debe ser 'Yes' o 'No'.";
        }
        if (!metodoPago.matches("^(Electronic check|Mailed check|Bank transfer \\(automatic\\)|Credit card \\(automatic\\))$")) {
            return "Método de pago no tiene un valor válido.";
        }
        if (!cargosMensuales.matches("^\\d+(\\.\\d{1,2})?$")) {
            return "Cargos mensuales debe ser un número válido.";
        }
        if (!cargosTotales.matches("^\\d+(\\.\\d{1,2})?$")) {
            return "Cargos totales debe ser un número válido.";
        }

        if (idOriginal == null) {
            // Alta: el id se genera solo.
            String nuevoId = generarId();
            ClienteDB.registrarCliente(new Cliente(nuevoId, genero, ciudadanoMayor, pareja, dependientes, antiguedad,
                    servicioTelefonico, lineasMultiples, servicioInternet, seguridadOnline, respaldoOnline,
                    proteccionDispositivo, soporteTecnico, streamingTv, streamingPeliculas, contrato,
                    facturaSinPapel, metodoPago, cargosMensuales, cargosTotales));
        } else {
            // Modificación: que el cliente exista (el id no cambia).
            if (ClienteDB.buscarCliente(idOriginal) == null) {
                return "No se encontró el cliente a modificar.";
            }
            ClienteDB.modificarCliente(idOriginal, new Cliente(idOriginal, genero, ciudadanoMayor, pareja, dependientes, antiguedad,
                    servicioTelefonico, lineasMultiples, servicioInternet, seguridadOnline, respaldoOnline,
                    proteccionDispositivo, soporteTecnico, streamingTv, streamingPeliculas, contrato,
                    facturaSinPapel, metodoPago, cargosMensuales, cargosTotales));
        }
        return null;
    }

    /** Genera un ID único con el formato del dataset Telco: 0000-AAAAA. */
    private static String generarId() {
        Random r = new Random();
        String id;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) sb.append(r.nextInt(10));
            sb.append('-');
            for (int i = 0; i < 5; i++) sb.append((char) ('A' + r.nextInt(26)));
            id = sb.toString();
        } while (ClienteDB.buscarCliente(id) != null);
        return id;
    }

    public static boolean eliminarCliente(String idCliente) {
        return ClienteDB.eliminarCliente(idCliente);
    }

    
    public static int importarClientesCsv() {
        return ClienteDB.importarDesdeCsv("DatasetTelcoChurn.csv");
    }
}
