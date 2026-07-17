package bajasnet;

import java.util.List;
import java.util.UUID;

public class PromocionesServicio {

    public static List<Promociones> listarPromociones() {
        return PromocionesDB.listarPromociones();
    }

    /**
     * Carga un set de promociones por defecto (una por cada servicio del dataset
     * Telco) solo si todavía no hay ninguna promoción registrada.
     */
    public static void precargarSiVacio() {
        if (!listarPromociones().isEmpty()) return;

        String[][] base = {
            {"Servicio Telefónico",       "10%",  "10% de descuento en el abono de telefonía."},
            {"Líneas Múltiples",          "15%",  "15% off al contratar líneas adicionales."},
            {"Internet DSL",              "10%",  "10% de descuento en el plan de Internet DSL."},
            {"Internet Fibra Óptica",     "20%",  "20% off el primer año en fibra óptica."},
            {"Seguridad Online",          "100%", "Seguridad Online gratis por 3 meses."},
            {"Respaldo Online",           "50%",  "50% de descuento en el respaldo en la nube."},
            {"Protección de Dispositivo", "25%",  "25% off en la protección de equipos."},
            {"Soporte Técnico",           "100%", "Soporte técnico premium sin cargo por 2 meses."},
            {"Streaming TV",              "30%",  "30% de descuento en el paquete de Streaming TV."},
            {"Streaming Películas",       "30%",  "30% off en el Streaming de películas."},
        };
        for (String[] p : base) {
            String id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            PromocionesDB.añadirPromocion(new Promociones(id, p[0], p[1], p[2]));
        }
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
