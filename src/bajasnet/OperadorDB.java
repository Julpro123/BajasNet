package bajasnet;
import java.io.*;
import java.util.*;

public class OperadorDB {

    private static final String ARCHIVO = "operadores.txt";
    private static List<Operador> operadores = new ArrayList<>();

    public static void inicializar() {
        if (new File(ARCHIVO).exists()) {
            cargar();
        } else {
            guardar();
        }
    }

    public static List<Operador> getOperadores() {
        return Collections.unmodifiableList(operadores);
    }

    private static void cargar() {
        operadores.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split("\\|");
                if (campos.length == 5) {
                    operadores.add(new Operador(campos[0], campos[1], campos[2], campos[3], campos[4]));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void guardar() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO))) {
            for (Operador u : operadores) {
                bw.write(u.getEmail() + "|" + u.getPassword() + "|" +
                         u.getNombre() + "|" + u.getApellido() + "|" + u.getDni());
                bw.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Operador buscarLogin(String email, String password) {
        for (Operador op : operadores) {
            if (op.getEmail().equals(email) && op.getPassword().equals(password)) {
                return op;
            }
        }
        return null;
    }

    public static Operador buscarOperador(String email) {
        for (Operador u : operadores) {
            if (u.getEmail().equals(email)) {
                return u;
            }
        }
        return null;
    }

    public static void registrarOperador(Operador op) {
        operadores.add(op);
        guardar();
    }

    public static void modificarOperador(String emailOriginal, Operador nuevo) {
        for (int i = 0; i < operadores.size(); i++) {
            if (operadores.get(i).getEmail().equals(emailOriginal)) {
                operadores.set(i, nuevo);
                break;
            }
        }
        guardar();
    }

    public static boolean eliminarOperador(String email) {
        boolean encontrado = operadores.removeIf(u -> u.getEmail().equals(email));
        if (encontrado)
            guardar();
        return encontrado;
    }
}
