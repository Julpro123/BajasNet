package bajasnet;

public class Cliente {
    private String idCliente;
    private String genero;
    private String ciudadanoMayor;
    private String pareja;
    private String dependientes;
    private String antiguedad;
    private String servicioTelefonico;
    private String lineasMultiples;
    private String servicioInternet;
    private String seguridadOnline;
    private String respaldoOnline;
    private String proteccionDispositivo;
    private String soporteTecnico;
    private String streamingTv;
    private String streamingPeliculas;
    private String contrato;
    private String facturaSinPapel;
    private String metodoPago;
    private String cargosMensuales;
    private String cargosTotales;

    public Cliente(String idCliente, String genero, String ciudadanoMayor, String pareja, String dependientes,
            String antiguedad, String servicioTelefonico, String lineasMultiples, String servicioInternet,
            String seguridadOnline, String respaldoOnline, String proteccionDispositivo, String soporteTecnico,
            String streamingTv, String streamingPeliculas, String contrato, String facturaSinPapel,
            String metodoPago, String cargosMensuales, String cargosTotales) {
        this.idCliente = idCliente;
        this.genero = genero;
        this.ciudadanoMayor = ciudadanoMayor;
        this.pareja = pareja;
        this.dependientes = dependientes;
        this.antiguedad = antiguedad;
        this.servicioTelefonico = servicioTelefonico;
        this.lineasMultiples = lineasMultiples;
        this.servicioInternet = servicioInternet;
        this.seguridadOnline = seguridadOnline;
        this.respaldoOnline = respaldoOnline;
        this.proteccionDispositivo = proteccionDispositivo;
        this.soporteTecnico = soporteTecnico;
        this.streamingTv = streamingTv;
        this.streamingPeliculas = streamingPeliculas;
        this.contrato = contrato;
        this.facturaSinPapel = facturaSinPapel;
        this.metodoPago = metodoPago;
        this.cargosMensuales = cargosMensuales;
        this.cargosTotales = cargosTotales;
    }

    public String getIdCliente()             { return idCliente; }
    public String getGenero()                { return genero; }
    public String getCiudadanoMayor()        { return ciudadanoMayor; }
    public String getPareja()                { return pareja; }
    public String getDependientes()          { return dependientes; }
    public String getAntiguedad()            { return antiguedad; }
    public String getServicioTelefonico()    { return servicioTelefonico; }
    public String getLineasMultiples()       { return lineasMultiples; }
    public String getServicioInternet()      { return servicioInternet; }
    public String getSeguridadOnline()       { return seguridadOnline; }
    public String getRespaldoOnline()        { return respaldoOnline; }
    public String getProteccionDispositivo() { return proteccionDispositivo; }
    public String getSoporteTecnico()        { return soporteTecnico; }
    public String getStreamingTv()           { return streamingTv; }
    public String getStreamingPeliculas()    { return streamingPeliculas; }
    public String getContrato()              { return contrato; }
    public String getFacturaSinPapel()       { return facturaSinPapel; }
    public String getMetodoPago()            { return metodoPago; }
    public String getCargosMensuales()       { return cargosMensuales; }
    public String getCargosTotales()         { return cargosTotales; }

    public String[] toArray() {
        return new String[] {
            idCliente, genero, ciudadanoMayor, pareja, dependientes, antiguedad, servicioTelefonico,
            lineasMultiples, servicioInternet, seguridadOnline, respaldoOnline, proteccionDispositivo,
            soporteTecnico, streamingTv, streamingPeliculas, contrato, facturaSinPapel,
            metodoPago, cargosMensuales, cargosTotales
        };
    }

    @Override
    public String toString() {
        return idCliente + " — " + genero + ", " + antiguedad + " meses, " + contrato;
    }
}
