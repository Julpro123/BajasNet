package bajasnet;
import java.io.Serializable;

public class Operador implements Serializable {

    private String email;
    private String password;
    private String nombre;
    private String apellido;
    private String dni;
    private boolean esAdmin;

    public Operador(String email, String password, String nombre, String apellido, String dni) {
        this(email, password, nombre, apellido, dni, false);
    }

    public Operador(String email, String password, String nombre, String apellido, String dni, boolean esAdmin) {
        this.email = email;
        this.password = password;
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.esAdmin = esAdmin;
    }

    public String getEmail()    { return email; }
    public String getPassword() { return password; }
    public String getNombre()   { return nombre; }
    public String getApellido() { return apellido; }
    public String getDni()      { return dni; }
    public boolean isAdmin()    { return esAdmin; }
}