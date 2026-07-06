package bajasnet;
import java.io.*;
import java.util.*;

public class OperadorDB {

    private static final String ARCHIVO = "operadores.txt";

    public static List<Operador> listarOperadores() {
        List<Operador> resultado = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split("\\|");
                if (campos.length == 5) {
                    resultado.add(new Operador(campos[0], campos[1], campos[2], campos[3], campos[4]));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultado;
    }

    public static Operador buscarLogin(String email, String password) {
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split("\\|");
                if (campos.length == 5 && campos[0].equals(email) && campos[1].equals(password)) {
                    return new Operador(campos[0], campos[1], campos[2], campos[3], campos[4]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Operador buscarOperador(String email) {
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split("\\|");
                if (campos.length == 5 && campos[0].equals(email)) {
                    return new Operador(campos[0], campos[1], campos[2], campos[3], campos[4]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void registrarOperador(Operador op) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO, true))) {
            bw.write(op.getEmail() + "|" + op.getPassword() + "|" +
                     op.getNombre() + "|" + op.getApellido() + "|" + op.getDni());
            bw.newLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void modificarOperador(String emailOriginal, Operador nuevo) {
        List<String> lineas = new ArrayList<>();
        boolean encontrado = false;
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split("\\|");
                if (campos.length == 5 && campos[0].equals(emailOriginal)) {
                    lineas.add(nuevo.getEmail() + "|" + nuevo.getPassword() + "|" +
                               nuevo.getNombre() + "|" + nuevo.getApellido() + "|" + nuevo.getDni());
                    encontrado = true;
                } else {
                    lineas.add(linea);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (!encontrado) return;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO))) {
            for (String linea : lineas) {
                bw.write(linea);
                bw.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean eliminarOperador(String email) {
        List<String> lineas = new ArrayList<>();
        boolean encontrado = false;
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split("\\|");
                if (campos.length == 5 && campos[0].equals(email)) {
                    encontrado = true;
                    continue;
                }
                lineas.add(linea);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        if (!encontrado) return false;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO))) {
            for (String linea : lineas) {
                bw.write(linea);
                bw.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
