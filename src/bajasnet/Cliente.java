package bajasnet;

public class Cliente {

    private int id;
    private String nombre;
    private String apellido;
    private String dni;
    private String email;
    private String telefono;

    public Cliente(int id, String nombre, String apellido, String dni, String email, String telefono) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.email = email;
        this.telefono = telefono;
    }

    public int getId()          { return id; }
    public String getNombre()   { return nombre; }
    public String getApellido() { return apellido; }
    public String getDni()      { return dni; }
    public String getEmail()    { return email; }
    public String getTelefono() { return telefono; }

    @Override
    public String toString() {
        return dni + " — " + nombre + " " + apellido;
    }
}
