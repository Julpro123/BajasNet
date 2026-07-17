package bajasnet;

/**
 * Un registro de auditoría: una predicción de churn ejecutada por un operador.
 * Guarda el resultado, quién y cuándo la hizo, y un snapshot del cliente
 * (los datos tal como estaban en el momento de predecir).
 */
public class Prediccion {

    private final String idRegistro;    // id correlativo del registro
    private final String fechaHora;     // cuándo se hizo la predicción
    private final String emailOperador; // quién la ejecutó
    private final double probabilidad;  // probabilidad de baja, 0..1
    private final String riesgo;        // BAJO / MEDIO / ALTO
    private final String recomendacion; // sugerencia asociada al riesgo
    private final Cliente cliente;      // snapshot del cliente al momento de predecir

    public Prediccion(String idRegistro, String fechaHora, String emailOperador,
                      double probabilidad, String riesgo, String recomendacion, Cliente cliente) {
        this.idRegistro = idRegistro;
        this.fechaHora = fechaHora;
        this.emailOperador = emailOperador;
        this.probabilidad = probabilidad;
        this.riesgo = riesgo;
        this.recomendacion = recomendacion;
        this.cliente = cliente;
    }

    public String getIdRegistro()    { return idRegistro; }
    public String getFechaHora()     { return fechaHora; }
    public String getEmailOperador() { return emailOperador; }
    public double getProbabilidad()  { return probabilidad; }
    public String getRiesgo()        { return riesgo; }
    public String getRecomendacion() { return recomendacion; }
    public Cliente getCliente()      { return cliente; }

    @Override
    public String toString() {
        return "#" + idRegistro + " [" + fechaHora + "] " + emailOperador
                + " -> cliente " + cliente.getIdCliente()
                + " (" + String.format("%.1f", probabilidad * 100) + "% - " + riesgo + ")";
    }
}
