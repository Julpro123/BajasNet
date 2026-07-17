package bajasnet;

import java.util.ArrayList;
import java.util.List;

/**
 * Lógica de negocio de las asignaciones de promociones a clientes.
 */
public class AsignacionServicio {

    /**
     * Asigna una promoción a un cliente.
     * Devuelve un mensaje de error, o null si se asignó correctamente.
     */
    public static String asignar(String idCliente, String idPromocion) {
        if (AsignacionDB.yaAsignada(idCliente, idPromocion)) {
            return "Esa promoción ya está asignada a este cliente.";
        }
        AsignacionDB.registrar(idCliente, idPromocion);
        return null;
    }

    /** Saca (desasigna) una promoción de un cliente. Devuelve true si la sacó. */
    public static boolean desasignar(String idCliente, String idPromocion) {
        return AsignacionDB.eliminar(idCliente, idPromocion);
    }

    /** Devuelve las promociones (con todos sus datos) asignadas a un cliente. */
    public static List<Promociones> promocionesDeCliente(String idCliente) {
        List<Promociones> resultado = new ArrayList<>();
        for (String idPromocion : AsignacionDB.promocionesDeCliente(idCliente)) {
            Promociones p = PromocionesDB.buscarPromocion(idPromocion);
            if (p != null) resultado.add(p);   // ignora asignaciones a promos ya borradas
        }
        return resultado;
    }
}
