package bajasnet;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PromocionesDB {

    private static final String ARCHIVO = "promociones.txt";

    public static List<Promociones> listarPromociones() {
        List<Promociones> resultado = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split("\\|");
                if (campos.length == 4) {
                    resultado.add(new Promociones(campos[0], campos[1], campos[2], campos[3]));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultado;
    }

    public static Promociones buscarPromocion(String id) {
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split("\\|");
                if (campos.length == 4 && campos[0].equals(id)) {
                    return new Promociones(campos[0], campos[1], campos[2], campos[3]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Promociones buscarPromocion(String servicio, String descuento, String descripcion) {
        for (Promociones p : listarPromociones()) {
            if (p.getServicio().equalsIgnoreCase(servicio)
                    && p.getDescuento().equalsIgnoreCase(descuento)
                    && p.getDescripcion().equalsIgnoreCase(descripcion)) {
                return p;
            }
        }
        return null;
    }

    public static void añadirPromocion(Promociones promocion) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO, true))) {
            bw.write(promocion.getId() + "|" + promocion.getServicio() + "|" +
                     promocion.getDescuento() + "|" + promocion.getDescripcion());
            bw.newLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void modificarPromocion(String id, String servicio,
                                          String descuento, String descripcion) {
        List<String> lineas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split("\\|");
                if (campos.length == 4 && campos[0].equals(id)) {
                    lineas.add(id + "|" + servicio + "|" + descuento + "|" + descripcion);
                } else {
                    lineas.add(linea);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO))) {
            for (String linea : lineas) {
                bw.write(linea);
                bw.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void eliminarPromocion(String id) {
        List<String> lineas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split("\\|");
                if (campos.length == 4 && campos[0].equals(id)) {
                    continue;
                }
                lineas.add(linea);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO))) {
            for (String linea : lineas) {
                bw.write(linea);
                bw.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
