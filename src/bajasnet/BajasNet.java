package bajasnet;

public class BajasNet {
    public static void main(String[] args) {
        PromocionesServicio.precargarSiVacio();   // carga promociones por defecto la primera vez
        LoginForm ventana = new LoginForm();
        ventana.setVisible(true);
    }
}