package bajasnet;
import java.io.Serializable;

public class Operador implements Serializable {

    private String email;
    private String password;
    private String nombre;
    private String apellido;
    private String dni;

    public Operador(String email, String password, String nombre, String apellido, String dni) {
        this.email = email;
        this.password = password;
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
    }

    public String getEmail()    { return email; }
    public String getPassword() { return password; }
    public String getNombre()   { return nombre; }
    public String getApellido() { return apellido; }
    public String getDni()      { return dni; }

    @Override
    public String toString() {
        return email + " — " + nombre + " " + apellido;
    }
}