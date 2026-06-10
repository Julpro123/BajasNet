package bajasnet;
import javax.swing.*;

public class BajasNet {
    public static void main(String[] args) {
        OperadorServicio.inicializar();
        LoginForm ventana = new LoginForm();
        ventana.setVisible(true);
    }
}