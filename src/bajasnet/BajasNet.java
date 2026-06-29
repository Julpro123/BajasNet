package bajasnet;
import javax.swing.*;

public class BajasNet {
    public static void main(String[] args) {
        OperadorServicio.inicializar();
        ClienteServicio.inicializar();
        LoginForm ventana = new LoginForm();
        ventana.setVisible(true);
    }
}