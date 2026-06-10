package bajasnet;
import java.io.*;
import java.util.*;

public class OperadorDB {

    private static final String ARCHIVO = "operadores.txt";

    public static void inicializar() {
        if (!new File(ARCHIVO).exists()) {
            guardar(new ArrayList<>());
        }
    }

    public static List<Operador> cargar() {
        List<Operador> lista = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split("\\|");
                if (campos.length == 5) {
                    lista.add(new Operador(campos[0], campos[1], campos[2], campos[3], campos[4]));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    private static void guardar(List<Operador> lista) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO))) {
            for (Operador u : lista) {
                bw.write(u.getEmail() + "|" + u.getPassword() + "|" +
                         u.getNombre() + "|" + u.getApellido() + "|" + u.getDni());
                bw.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Operador buscarLogin(String email, String password) {
        for (Operador op : cargar()) {
            if (op.getEmail().equals(email) && op.getPassword().equals(password)) {
                return op;
            }
        }
        return null;
    }

    public static Operador buscarOperador(String email) {
        for (Operador u : cargar()) {
            if (u.getEmail().equals(email)) {
                return u;
            }
        }
        return null;
    }

    public static void registrarOperador(Operador op) {
        List<Operador> lista = cargar();
        lista.add(op);
        guardar(lista);
    }

    public static void modificarOperador(String emailOriginal, Operador nuevo) {
        List<Operador> lista = cargar();
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getEmail().equals(emailOriginal)) {
                lista.set(i, nuevo);
                break;
            }
        }
        guardar(lista);
    }

    public static boolean eliminarOperador(String email) {
        List<Operador> lista = cargar();
        boolean encontrado = lista.removeIf(u -> u.getEmail().equals(email));
        if (encontrado)
            guardar(lista);
        return encontrado;
    }
}
