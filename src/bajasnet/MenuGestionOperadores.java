package bajasnet;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MenuGestionOperadores extends JFrame {

    public MenuGestionOperadores(JFrame parent) {
        setTitle("Gestión de Operadores");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(360, 260);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel titulo = new JLabel("Gestión de Operadores");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        titulo.setForeground(new Color(30, 40, 60));
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 24, 0);
        panel.add(titulo, gbc);

        JButton btnCrear    = new JButton("Crear Operador");
        JButton btnEliminar = new JButton("Eliminar Operador");
        JButton btnModificar = new JButton("Modificar Operador");
        for (JButton b : new JButton[]{btnCrear, btnEliminar, btnModificar})
            b.setFont(new Font("SansSerif", Font.PLAIN, 13));

        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 12, 0);
        panel.add(btnCrear, gbc);
        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 12, 0);
        panel.add(btnEliminar, gbc);
        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(btnModificar, gbc);

        btnCrear.addActionListener(e -> mostrarDialogoCrear());
        btnEliminar.addActionListener(e -> mostrarDialogoEliminar());
        btnModificar.addActionListener(e -> mostrarDialogoModificar());

        add(panel);
        setLocationRelativeTo(parent);
    }

    private void mostrarDialogoCrear() {
        mostrarFormulario("Crear Operador", null);
    }

    private void mostrarDialogoEliminar() {
        List<Operador> lista = OperadorServicio.cargar();
        if (lista.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay operadores registrados.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] opciones = lista.stream()
            .map(o -> o.getEmail() + " — " + o.getNombre() + " " + o.getApellido())
            .toArray(String[]::new);

        String seleccion = (String) JOptionPane.showInputDialog(this,
            "Seleccioná el operador a eliminar:", "Eliminar Operador",
            JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);

        if (seleccion == null) return;

        String emailSel = seleccion.split(" — ", 2)[0];
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Eliminar al operador \"" + emailSel + "\"?", "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            OperadorServicio.eliminarOperador(emailSel);
            JOptionPane.showMessageDialog(this, "Operador eliminado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void mostrarDialogoModificar() {
        List<Operador> lista = OperadorServicio.cargar();
        if (lista.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay operadores registrados.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] opciones = lista.stream().map(o -> o.getEmail() + " — " + o.getNombre() + " " + o.getApellido())
            .toArray(String[]::new);

        String seleccion = (String) JOptionPane.showInputDialog(this,
            "Seleccioná el operador a modificar:", "Modificar Operador",
            JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);

        if (seleccion == null) return;

        String emailOriginal = seleccion.split(" — ", 2)[0];
        Operador op = OperadorServicio.buscarOperador(emailOriginal);

        mostrarFormulario("Modificar Operador", op);
    }

    private void mostrarFormulario(String titulo, Operador opExistente) {
        JDialog dialog = new JDialog(this, titulo, true);
        boolean esNuevo = opExistente == null;
        String emailOriginal = esNuevo ? null : opExistente.getEmail();

        JTextField nombreField   = new JTextField(esNuevo ? "" : opExistente.getNombre(),    20);
        JTextField apellidoField = new JTextField(esNuevo ? "" : opExistente.getApellido(),  20);
        JTextField emailField    = new JTextField(esNuevo ? "" : opExistente.getEmail(),     20);
        JPasswordField passField = new JPasswordField(esNuevo ? "" : opExistente.getPassword(), 20);
        JTextField dniField      = new JTextField(esNuevo ? "" : opExistente.getDni(),       20);

        JLabel errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(errorLabel.getFont().deriveFont(Font.PLAIN, 11f));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 4, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 4, 3, 4);

        Object[][] filas = {
            {"Nombre:",     nombreField},
            {"Apellido:",   apellidoField},
            {"Email:",      emailField},
            {"Contraseña:", passField},
            {"DNI:",        dniField}
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
            String nombre  = nombreField.getText().trim();
            String apellido  = apellidoField.getText().trim();
            String email = emailField.getText().trim();
            String password  = new String(passField.getPassword()).trim();
            String dni  = dniField.getText().trim();

            String error = esNuevo
                ? OperadorServicio.validarDatosRegistro(nombre, apellido, email, password, dni)
                : OperadorServicio.validarDatosModificacion(emailOriginal, nombre, apellido, email, password, dni);

            if (error != null) {
                errorLabel.setText(error);
                enfocarCampoError(error, nombre, apellido, email, password, dni,
                    nombreField, apellidoField, emailField, passField, dniField);
            } else {
                dialog.dispose();
                String msg = esNuevo ? "Operador creado correctamente." : "Operador modificado correctamente.";
                JOptionPane.showMessageDialog(this, msg, "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        dialog.pack();
        dialog.setMinimumSize(new Dimension(340, dialog.getHeight()));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void enfocarCampoError(String error, String nombre, String apellido, String email, String password, String dni,
            JTextField nombreField, JTextField apellidoField, JTextField emailField,
            JPasswordField passField, JTextField dniField) {
        JTextField foco;
        if (error.contains("requeridos")) {
            if (nombre.isEmpty())       foco = nombreField;
            else if (apellido.isEmpty())  foco = apellidoField;
            else if (email.isEmpty()) foco = emailField;
            else if (password.isEmpty())  foco = passField;
            else                   foco = dniField;
        } else if (error.contains("nombre"))    foco = nombreField;
        else if (error.contains("apellido"))    foco = apellidoField;
        else if (error.contains("email") || error.contains("operador")) foco = emailField;
        else if (error.contains("contraseña"))  foco = passField;
        else                                    foco = dniField;
        foco.requestFocusInWindow();
        foco.selectAll();
    }
}
