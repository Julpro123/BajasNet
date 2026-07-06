package bajasnet;

public class PromocionesServicio {

    public static String validarPromocion(String servicio, String descuento, String descripcion) {

        if (servicio == null || servicio.isEmpty()
                || descuento == null || descuento.isEmpty()
                || descripcion == null || descripcion.isEmpty()) {
            return "Error: todos los campos son obligatorios.";
        }

        Promociones existente = buscarPorDatos(servicio, descuento, descripcion);
        if (existente != null) {
            return "Se informa al administrador que la promoción ya existe.";
        }

        Promociones nueva = PromocionesDB.añadirPromocion(servicio, descuento, descripcion);
        if (nueva != null) {
            return "Se informa que se creó correctamente la promoción. ID: " + nueva.getId();
        }
        return "Error inesperado al crear la promoción.";
    }

    public static String editarPromocion(String idPromocion, String servicio,
                                         String descuento, String descripcion) {

        if (idPromocion == null || idPromocion.isEmpty()
                || servicio == null || servicio.isEmpty()
                || descuento == null || descuento.isEmpty()
                || descripcion == null || descripcion.isEmpty()) {
            return "Error: el formato ingresado no es correcto.";
        }

        Promociones actual = PromocionesDB.buscarPromocion(idPromocion);
        if (actual == null) {
            return "Se informa que debe cambiar los datos correspondientes (la promoción no existe).";
        }

        if (actual.getServicio().equalsIgnoreCase(servicio)
                && actual.getDescuento().equalsIgnoreCase(descuento)
                && actual.getDescripcion().equalsIgnoreCase(descripcion)) {
            return "Se informa que ya existe un operador con los mismos datos.";
        }

        boolean ok = PromocionesDB.modificarPromocion(idPromocion, servicio, descuento, descripcion);
        if (ok) {
            return "Se informa que se modificó al operador.";
        }
        return "Error inesperado al modificar la promoción.";
    }

    public static String eliminarPromocion(String id, boolean confirmar) {

        if (!confirmar) {
            return "Se informa al Administrador que se canceló la operación.";
        }

        Promociones p = PromocionesDB.buscarPromocion(id);
        if (p == null) {
            return "No se encontró la promoción.";
        }

        boolean ok = PromocionesDB.desaparecerPromocion(id);
        if (ok) {
            return "Se informa que la promoción se eliminó correctamente.";
        }
        return "Error inesperado al eliminar la promoción.";
    }

    private static Promociones buscarPorDatos(String servicio, String descuento, String descripcion) {
        for (Promociones p : PromocionesDB.cargar()) {
            if (p.getServicio().equalsIgnoreCase(servicio)
                    && p.getDescuento().equalsIgnoreCase(descuento)
                    && p.getDescripcion().equalsIgnoreCase(descripcion)) {
                return p;
            }
        }
        return null;
    }
}