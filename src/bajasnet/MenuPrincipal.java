package bajasnet;
import javax.swing.*;
import java.awt.*;

public class MenuPrincipal extends JFrame {

    public MenuPrincipal(Operador operador) {
        setTitle("BajasNet — Menú Principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;

        JLabel bienvenida = new JLabel("Bienvenido, " + operador.getApellido());
        bienvenida.setFont(new Font("SansSerif", Font.BOLD, 20));
        bienvenida.setForeground(new Color(30, 40, 60));
        bienvenida.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 30, 0);
        panel.add(bienvenida, gbc);

        int fila = 1;

        if (operador.isAdmin()) {
            JLabel adminLabel = new JLabel("Panel de Administración");
            adminLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            adminLabel.setForeground(new Color(80, 100, 140));
            adminLabel.setHorizontalAlignment(SwingConstants.CENTER);
            gbc.gridy = fila++;
            gbc.insets = new Insets(0, 0, 16, 0);
            panel.add(adminLabel, gbc);

            JButton btnGestion = new JButton("Gestión de Operadores");
            btnGestion.setFont(new Font("SansSerif", Font.PLAIN, 13));
            gbc.gridy = fila++;
            gbc.insets = new Insets(0, 0, 12, 0);
            panel.add(btnGestion, gbc);

            btnGestion.addActionListener(e -> new MenuGestionOperadores(this).setVisible(true));
        }

        JButton btnClientes = new JButton("Gestión de Clientes");
        btnClientes.setFont(new Font("SansSerif", Font.PLAIN, 13));
        gbc.gridy = fila;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(btnClientes, gbc);
        btnClientes.addActionListener(e -> new MenuGestionClientes(this).setVisible(true));

        add(panel);
        setLocationRelativeTo(null);
    }
}
