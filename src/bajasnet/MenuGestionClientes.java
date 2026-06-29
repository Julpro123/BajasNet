package bajasnet;

import javax.swing.*;
import java.awt.*;

public class MenuGestionClientes extends JFrame {

    public MenuGestionClientes(JFrame parent) {
        setTitle("Gestión de Clientes");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(360, 310);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel titulo = new JLabel("Gestión de Clientes");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        titulo.setForeground(new Color(30, 40, 60));
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 24, 0);
        panel.add(titulo, gbc);

        JButton btnVer      = new JButton("Ver Clientes");
        JButton btnCrear    = new JButton("Crear Cliente");
        JButton btnEliminar = new JButton("Eliminar Cliente");
        JButton btnModificar = new JButton("Modificar Cliente");
        for (JButton b : new JButton[]{btnVer, btnCrear, btnEliminar, btnModificar})
            b.setFont(new Font("SansSerif", Font.PLAIN, 13));

        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 12, 0);
        panel.add(btnVer, gbc);
        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 12, 0);
        panel.add(btnCrear, gbc);
        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 12, 0);
        panel.add(btnEliminar, gbc);
        gbc.gridy = 4; gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(btnModificar, gbc);

        btnVer.addActionListener(e -> mostrarDialogoVer());
        btnCrear.addActionListener(e -> mostrarDialogoCrear());
        btnEliminar.addActionListener(e -> mostrarDialogoEliminar());
        btnModificar.addActionListener(e -> mostrarDialogoModificar());

        add(panel);
        setLocationRelativeTo(parent);
    }

    private void mostrarDialogoVer() {
        if (!ClienteServicio.hayClientes()) {
            JOptionPane.showMessageDialog(this, "No hay clientes registrados.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Cliente[] clientes = ClienteServicio.getClientes().toArray(new Cliente[0]);

        JList<Cliente> lista = new JList<>(clientes);
        lista.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(lista);
        scroll.setPreferredSize(new Dimension(320, 200));

        JOptionPane.showMessageDialog(this, scroll, "Clientes registrados", JOptionPane.PLAIN_MESSAGE);
    }

    private void mostrarDialogoCrear() {
        mostrarFormulario("Crear Cliente", null);
    }

    private void mostrarDialogoEliminar() {
        if (!ClienteServicio.hayClientes()) {
            JOptionPane.showMessageDialog(this, "No hay clientes registrados.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Cliente[] opciones = ClienteServicio.getClientes().toArray(new Cliente[0]);

        Cliente seleccion = (Cliente) JOptionPane.showInputDialog(this,
            "Seleccioná el cliente a eliminar:", "Eliminar Cliente",
            JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);

        if (seleccion == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Eliminar al cliente \"" + seleccion.getNombre() + " " + seleccion.getApellido() + "\"?", "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            ClienteServicio.eliminarCliente(seleccion.getId());
            JOptionPane.showMessageDialog(this, "Cliente eliminado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void mostrarDialogoModificar() {
        if (!ClienteServicio.hayClientes()) {
            JOptionPane.showMessageDialog(this, "No hay clientes registrados.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Cliente[] opciones = ClienteServicio.getClientes().toArray(new Cliente[0]);

        Cliente seleccion = (Cliente) JOptionPane.showInputDialog(this,
            "Seleccioná el cliente a modificar:", "Modificar Cliente",
            JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);

        if (seleccion == null) return;

        mostrarFormulario("Modificar Cliente", seleccion);
    }

    private void mostrarFormulario(String titulo, Cliente clienteExistente) {
        JDialog dialog = new JDialog(this, titulo, true);
        boolean esNuevo = clienteExistente == null;
        int idOriginal = esNuevo ? -1 : clienteExistente.getId();

        JTextField nombreField   = new JTextField(esNuevo ? "" : clienteExistente.getNombre(),    20);
        JTextField apellidoField = new JTextField(esNuevo ? "" : clienteExistente.getApellido(),  20);
        JTextField dniField      = new JTextField(esNuevo ? "" : clienteExistente.getDni(),       20);
        JTextField emailField    = new JTextField(esNuevo ? "" : clienteExistente.getEmail(),     20);
        JTextField telefonoField = new JTextField(esNuevo ? "" : clienteExistente.getTelefono(),  20);

        JLabel errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(errorLabel.getFont().deriveFont(Font.PLAIN, 11f));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 4, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 4, 3, 4);

        Object[][] filas = {
            {"Nombre:",    nombreField},
            {"Apellido:",  apellidoField},
            {"DNI:",       dniField},
            {"Email:",     emailField},
            {"Teléfono:",  telefonoField}
        };

        for (int i = 0; i < filas.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;
            formPanel.add(new JLabel((String) filas[i][0]), gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            formPanel.add((Component) filas[i][1], gbc);
        }

        gbc.gridx = 0; gbc.gridy = filas.length; gbc.gridwidth = 2;
        formPanel.add(errorLabel, gbc);

        JButton okBtn     = new JButton("Aceptar");
        JButton cancelBtn = new JButton("Cancelar");
        JPanel btnPanel   = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnPanel.add(cancelBtn);
        btnPanel.add(okBtn);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(okBtn);

        cancelBtn.addActionListener(e -> dialog.dispose());
        okBtn.addActionListener(e -> {
            String nombre   = nombreField.getText().trim();
            String apellido = apellidoField.getText().trim();
            String dni      = dniField.getText().trim();
            String email    = emailField.getText().trim();
            String telefono = telefonoField.getText().trim();

            String error = esNuevo
                ? ClienteServicio.validarDatos(nombre, apellido, dni, email, telefono)
                : ClienteServicio.validarDatos(idOriginal, nombre, apellido, dni, email, telefono);

            if (error != null) {
                errorLabel.setText(error);
            } else {
                dialog.dispose();
                String msg = esNuevo ? "Cliente creado correctamente." : "Cliente modificado correctamente.";
                JOptionPane.showMessageDialog(this, msg, "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        dialog.pack();
        dialog.setMinimumSize(new Dimension(340, dialog.getHeight()));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
