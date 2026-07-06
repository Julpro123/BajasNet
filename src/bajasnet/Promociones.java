package bajasnet;

public class Promociones {

    private String id;
    private String servicio;
    private String descuento;
    private String descripcion;

    public Promociones(String id, String servicio, String descuento, String descripcion) {
        this.id = id;
        this.servicio = servicio;
        this.descuento = descuento;
        this.descripcion = descripcion;
    }

    public String getId()          { return id; }
    public String getServicio()    { return servicio; }
    public String getDescuento()   { return descuento; }
    public String getDescripcion() { return descripcion; }
}