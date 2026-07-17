package bajasnet;
import java.awt.*;
import javax.swing.*;

public class LoginForm extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public LoginForm() {
        setTitle("BajasNet — Iniciar Sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        JLabel titulo = new JLabel("BajasNet");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(titulo, gbc);

        // Label Email
        JLabel emailLabel = new JLabel("Correo electrónico");
        emailLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        gbc.gridy = 1;
        gbc.insets = new Insets(6, 0, 2, 0);
        panel.add(emailLabel, gbc);

        // Campo Email
        emailField = new JTextField(20);
        emailField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(emailField, gbc);

        // Label Contraseña
        JLabel passLabel = new JLabel("Contraseña");
        passLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        gbc.gridy = 3;
        gbc.insets = new Insets(6, 0, 2, 0);
        panel.add(passLabel, gbc);

        // Fila: campo contraseña + botón ojo
        JPanel passRow = new JPanel(new BorderLayout(4, 0));
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JButton toggleBtn = new JButton("👁");
        toggleBtn.setFocusPainted(false);
        toggleBtn.setMargin(new Insets(0, 6, 0, 6));
        toggleBtn.setToolTipText("Mostrar/ocultar contraseña");
        toggleBtn.addActionListener(e -> {
            if (passwordField.getEchoChar() == 0) {
                passwordField.setEchoChar('•');
                toggleBtn.setText("Ver");
            } else {
                passwordField.setEchoChar((char) 0);
                toggleBtn.setText("Ocultar");
            }
        });

        passRow.add(passwordField, BorderLayout.CENTER);
        passRow.add(toggleBtn, BorderLayout.EAST);

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 24, 0);
        panel.add(passRow, gbc);

        // Botón Login
        loginButton = new JButton("Iniciar Sesión");
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginButton.setFocusPainted(false);

        loginButton.addActionListener(e -> iniciarSesion());

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(loginButton, gbc);

        add(panel);
        pack();
        setMinimumSize(new Dimension(360, 300));
        setLocationRelativeTo(null);
    }

    private void iniciarSesion() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        // Primero se intenta como admin (AdminServicio/AdminDB); si no, como operador.
        Operador op = AdminServicio.validarLogin(email, password);
        if (op == null) {
            op = OperadorServicio.validarLogin(email, password);
        }
        if (op == null) {
            JOptionPane.showMessageDialog(this,
                "Email o contraseña incorrectos.",
                "Error de acceso",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        dispose();
        new MenuPrincipal(op).setVisible(true);
    }
}