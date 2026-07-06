package bajasnet;

import javax.swing.*;
import java.awt.*;

public class MenuPrincipal extends JFrame {

    public MenuPrincipal(Operador operador) {
        setTitle("BajasNet — Menú Principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(new Color(245, 247, 250));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 12));

        JButton btnCerrarSesion = new JButton("Cerrar Sesión");
        btnCerrarSesion.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnCerrarSesion.setBackground(new Color(200, 40, 40));
        btnCerrarSesion.setForeground(Color.WHITE);
        btnCerrarSesion.setOpaque(true);
        btnCerrarSesion.setBorderPainted(false);
        btnCerrarSesion.setFocusPainted(false);
        btnCerrarSesion.addActionListener(e -> {
            dispose();
            new LoginForm().setVisible(true);
        });

        headerPanel.add(btnCerrarSesion, BorderLayout.EAST);
        contenedor.add(headerPanel, BorderLayout.NORTH);

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

        // Gestión de Clientes (todos)
        JButton btnClientes = new JButton("Gestión de Clientes");
        btnClientes.setFont(new Font("SansSerif", Font.PLAIN, 13));
        gbc.gridy = fila++;
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(btnClientes, gbc);

        btnClientes.addActionListener(e ->
                new MenuGestionClientes(this).setVisible(true)
        );

        // Gestión de Operadores (solo administrador)
        if (operador.isAdmin()) {
            JButton btnOperadores = new JButton("Gestión de Operadores");
            btnOperadores.setFont(new Font("SansSerif", Font.PLAIN, 13));

            gbc.gridy = fila++;
            gbc.insets = new Insets(0, 0, 10, 0);
            panel.add(btnOperadores, gbc);

            btnOperadores.addActionListener(e ->
                    new MenuGestionOperadores(this).setVisible(true)
            );
        }

        // Promociones (todos)
        JButton btnPromociones = new JButton("Promociones");
        btnPromociones.setFont(new Font("SansSerif", Font.PLAIN, 13));

        gbc.gridy = fila++;
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(btnPromociones, gbc);

        btnPromociones.addActionListener(e ->
                new MenuGestionPromocion(operador).setVisible(true)
        );

        contenedor.add(panel, BorderLayout.CENTER);
        add(contenedor);
        setLocationRelativeTo(null);
    }
}