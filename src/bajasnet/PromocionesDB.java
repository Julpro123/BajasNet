package bajasnet;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PromocionesDB {

    private static final String ARCHIVO = "promociones.txt";

    public static void inicializar() {
        if (!new File(ARCHIVO).exists()) {
            guardar(new ArrayList<>());
        }
    }

    public static List<Promociones> cargar() {
        List<Promociones> lista = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split("\\|");
                if (campos.length == 4) {
                    lista.add(new Promociones(campos[0], campos[1], campos[2], campos[3]));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    private static void guardar(List<Promociones> lista) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO))) {
            for (Promociones p : lista) {
                bw.write(p.getId()          + "|" +
                         p.getServicio()    + "|" +
                         p.getDescuento()   + "|" +
                         p.getDescripcion());
                bw.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Promociones añadirPromocion(String servicio, String descuento, String descripcion) {
        List<Promociones> lista = cargar();
        for (Promociones p : lista) {
            if (p.getServicio().equalsIgnoreCase(servicio)
                    && p.getDescuento().equalsIgnoreCase(descuento)
                    && p.getDescripcion().equalsIgnoreCase(descripcion)) {
                return null;
            }
        }
        String nuevoId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Promociones nueva = new Promociones(nuevoId, servicio, descuento, descripcion);
        lista.add(nueva);
        guardar(lista);
        return nueva;
    }

    public static Promociones buscarPromocion(String id) {
        for (Promociones p : cargar()) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    public static boolean desaparecerPromocion(String id) {
        List<Promociones> lista = cargar();
        boolean encontrado = lista.removeIf(p -> p.getId().equals(id));
        if (encontrado) {
            guardar(lista);
        }
        return encontrado;
    }

    public static boolean modificarPromocion(String id, String servicio,
                                             String descuento, String descripcion) {
        List<Promociones> lista = cargar();
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getId().equals(id)) {
                lista.set(i, new Promociones(id, servicio, descuento, descripcion));
                guardar(lista);
                return true;
            }
        }
        return false;
    }
}