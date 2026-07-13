package bajasnet;

import java.util.List;
import java.util.UUID;

public class PromocionesServicio {

    public static List<Promociones> listarPromociones() {
        return PromocionesDB.listarPromociones();
    }

    public static Promociones buscarPromocion(String id) {
        return PromocionesDB.buscarPromocion(id);
    }

    public static String validarDatos(String idOriginal, String servicio, String descuento, String descripcion) {
        if (servicio.isEmpty() || descuento.isEmpty() || descripcion.isEmpty()) {
            return "Todos los campos son requeridos.";
        }

        if (idOriginal == null) {
            if (PromocionesDB.buscarPromocion(servicio, descuento, descripcion) != null) {
                return "Ya existe una promoción con esos datos.";
            }
            String nuevoId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            PromocionesDB.añadirPromocion(new Promociones(nuevoId, servicio, descuento, descripcion));
        } else {
            // Modificación: que la promoción exista.
            if (PromocionesDB.buscarPromocion(idOriginal) == null) {
                return "No se encontró la promoción a modificar.";
            }
            PromocionesDB.modificarPromocion(idOriginal, servicio, descuento, descripcion);
        }
        return null;
    }

    public static boolean eliminarPromocion(String id) {
        if (PromocionesDB.buscarPromocion(id) == null) {
            return false;
        }
        PromocionesDB.eliminarPromocion(id);
        return true;
    }
}
