package bajasnet;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class MenuGestionClientes extends JFrame {

    private static final String[] COLUMNAS = {"Id", "Nombre", "Apellido", "DNI", "Email", "Teléfono"};

    private DefaultTableModel tableModel;
    private JTable tabla;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField filtroField;
    private JTextField detIdField, detNombreField, detApellidoField, detDniField, detEmailField, detTelefonoField;
    private JLabel detErrorLabel;
    private JPanel botonesPanel;
    private JButton btnCrear, btnEliminar, btnModificar, btnCancelar;
    private boolean modoEdicion = false;
    private boolean modoCreacion = false;
    private int idEnEdicion;

    public MenuGestionClientes(JFrame parent) {
        setTitle("Gestión de Clientes");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(760, 480);

        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titulo = new JLabel("Gestión de Clientes");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        titulo.setForeground(new Color(30, 40, 60));
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titulo, BorderLayout.CENTER);

        JButton btnVolver = new JButton("Volver");
        btnVolver.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnVolver.setBackground(new Color(200, 40, 40));
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setOpaque(true);
        btnVolver.setBorderPainted(false);
        btnVolver.setFocusPainted(false);
        btnVolver.addActionListener(e -> dispose());
        headerPanel.add(btnVolver, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.setOpaque(false);

        JPanel filtroPanel = new JPanel(new BorderLayout(8, 0));
        filtroPanel.setOpaque(false);
        JLabel filtroLabel = new JLabel("Filtrar por DNI:");
        filtroLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        filtroField = new JTextField();
        filtroPanel.add(filtroLabel, BorderLayout.WEST);
        filtroPanel.add(filtroField, BorderLayout.CENTER);
        centro.add(filtroPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla = new JTable(tableModel);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tabla.setRowHeight(22);
        tabla.setFillsViewportHeight(true);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] anchosColumnas = {50, 110, 110, 90, 160, 100};
        for (int i = 0; i < anchosColumnas.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchosColumnas[i]);
        }
        sorter = new TableRowSorter<>(tableModel);
        tabla.setRowSorter(sorter);

        filtroField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void filtrar() {
                String texto = filtroField.getText().trim();
                sorter.setRowFilter(texto.isEmpty() ? null
                    : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto), 3));
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
        });

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int fila = tabla.getSelectedRow();
            if (fila == -1) {
                limpiarDetalle();
                return;
            }
            int modelRow = tabla.convertRowIndexToModel(fila);
            detIdField.setText(String.valueOf(tableModel.getValueAt(modelRow, 0)));
            detNombreField.setText((String) tableModel.getValueAt(modelRow, 1));
            detApellidoField.setText((String) tableModel.getValueAt(modelRow, 2));
            detDniField.setText((String) tableModel.getValueAt(modelRow, 3));
            detEmailField.setText((String) tableModel.getValueAt(modelRow, 4));
            detTelefonoField.setText((String) tableModel.getValueAt(modelRow, 5));
        });

        JScrollPane scrollTabla = new JScrollPane(tabla,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollTabla.setPreferredSize(new Dimension(400, 300));
        centro.add(scrollTabla, BorderLayout.CENTER);
        centro.add(construirPanelDetalle(), BorderLayout.EAST);
        panel.add(centro, BorderLayout.CENTER);

        botonesPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        botonesPanel.setOpaque(false);
        btnCrear     = new JButton("Crear Cliente");
        btnEliminar  = new JButton("Eliminar Cliente");
        btnModificar = new JButton("Modificar Cliente");
        btnCancelar  = new JButton("Cancelar");
        for (JButton b : new JButton[]{btnCrear, btnEliminar, btnModificar, btnCancelar})
            b.setFont(new Font("SansSerif", Font.PLAIN, 13));

        botonesPanel.add(btnCrear);
        botonesPanel.add(btnEliminar);
        botonesPanel.add(btnModificar);
        panel.add(botonesPanel, BorderLayout.SOUTH);

        btnCrear.addActionListener(e -> {
            try {
                iniciarCreacion();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error inesperado: " + ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnEliminar.addActionListener(e -> {
            try {
                eliminarClienteSeleccionado();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error inesperado: " + ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnModificar.addActionListener(e -> {
            try {
                if (modoCreacion) {
                    confirmarCreacion();
                } else if (modoEdicion) {
                    confirmarEdicion();
                } else {
                    iniciarEdicion();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error inesperado: " + ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnCancelar.addActionListener(e -> cancelarEdicion());

        add(panel);
        setLocationRelativeTo(parent);
        actualizarTabla();
    }

    private JPanel construirPanelDetalle() {
        JPanel detalle = new JPanel(new GridBagLayout());
        detalle.setOpaque(false);
        detalle.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
        detalle.setPreferredSize(new Dimension(190, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.NORTH;

        JLabel titulo = new JLabel("Detalle");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 13));
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 8, 0);
        detalle.add(titulo, gbc);

        detIdField        = new JTextField();
        detNombreField    = new JTextField();
        detApellidoField  = new JTextField();
        detDniField       = new JTextField();
        detEmailField     = new JTextField();
        detTelefonoField  = new JTextField();
        for (JTextField f : new JTextField[]{detIdField, detNombreField, detApellidoField, detDniField, detEmailField, detTelefonoField})
            f.setEditable(false);

        Object[][] filas = {
            {"Id:",        detIdField},
            {"Nombre:",    detNombreField},
            {"Apellido:",  detApellidoField},
            {"DNI:",       detDniField},
            {"Email:",     detEmailField},
            {"Teléfono:",  detTelefonoField}
        };

        int fila = 1;
        for (Object[] f : filas) {
            JLabel lbl = new JLabel((String) f[0]);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
            gbc.gridy = fila++;
            gbc.insets = new Insets(6, 0, 2, 0);
            detalle.add(lbl, gbc);

            gbc.gridy = fila++;
            gbc.insets = new Insets(0, 0, 4, 0);
            detalle.add((JTextField) f[1], gbc);
        }

        detErrorLabel = new JLabel(" ");
        detErrorLabel.setForeground(Color.RED);
        detErrorLabel.setFont(detErrorLabel.getFont().deriveFont(Font.PLAIN, 11f));
        gbc.gridy = fila++;
        gbc.insets = new Insets(6, 0, 0, 0);
        detalle.add(detErrorLabel, gbc);

        gbc.gridy = fila;
        gbc.weighty = 1;
        detalle.add(Box.createVerticalGlue(), gbc);

        return detalle;
    }

    private void limpiarDetalle() {
        for (JTextField f : new JTextField[]{detIdField, detNombreField, detApellidoField, detDniField, detEmailField, detTelefonoField})
            f.setText("");
    }

    private void actualizarTabla() {
        tableModel.setRowCount(0);
        List<Cliente> clientes = ClienteServicio.listarClientes();
        for (Cliente c : clientes) {
            tableModel.addRow(new Object[]{
                c.getId(), c.getNombre(), c.getApellido(), c.getDni(), c.getEmail(), c.getTelefono()
            });
        }
    }

    private void iniciarEdicion() {
        if (tabla.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Seleccioná un cliente de la tabla para modificar.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        idEnEdicion = Integer.parseInt(detIdField.getText());
        modoEdicion = true;
        detErrorLabel.setText(" ");
        setDetalleEditable(true);
        mostrarBotones(true);
        detNombreField.requestFocusInWindow();
    }

    private void confirmarEdicion() {
        String nombre    = detNombreField.getText().trim();
        String apellido  = detApellidoField.getText().trim();
        String dni       = detDniField.getText().trim();
        String email     = detEmailField.getText().trim();
        String telefono  = detTelefonoField.getText().trim();

        String error = ClienteServicio.validarDatos(idEnEdicion, nombre, apellido, dni, email, telefono);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        modoEdicion = false;
        setDetalleEditable(false);
        mostrarBotones(false);
        actualizarTabla();
        JOptionPane.showMessageDialog(this, "Cliente modificado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void eliminarClienteSeleccionado() {
        if (tabla.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Seleccioná un cliente de la tabla para eliminar.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = Integer.parseInt(detIdField.getText());
        String nombreCompleto = detNombreField.getText() + " " + detApellidoField.getText();

        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Eliminar al cliente \"" + nombreCompleto + "\"?", "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            ClienteServicio.eliminarCliente(id);
            actualizarTabla();
            limpiarDetalle();
            JOptionPane.showMessageDialog(this, "Cliente eliminado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void iniciarCreacion() {
        tabla.clearSelection();
        limpiarDetalle();
        modoCreacion = true;
        detErrorLabel.setText(" ");
        setDetalleEditable(true);
        mostrarBotones(true);
        detNombreField.requestFocusInWindow();
    }

    private void confirmarCreacion() {
        String nombre    = detNombreField.getText().trim();
        String apellido  = detApellidoField.getText().trim();
        String dni       = detDniField.getText().trim();
        String email     = detEmailField.getText().trim();
        String telefono  = detTelefonoField.getText().trim();

        String error = ClienteServicio.validarDatos(nombre, apellido, dni, email, telefono);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        modoCreacion = false;
        setDetalleEditable(false);
        mostrarBotones(false);
        actualizarTabla();
        limpiarDetalle();
        JOptionPane.showMessageDialog(this, "Cliente creado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void cancelarEdicion() {
        boolean estabaCreando = modoCreacion;
        modoCreacion = false;
        modoEdicion = false;
        detErrorLabel.setText(" ");
        setDetalleEditable(false);
        mostrarBotones(false);

        int fila = tabla.getSelectedRow();
        if (estabaCreando || fila == -1) {
            limpiarDetalle();
            return;
        }
        int modelRow = tabla.convertRowIndexToModel(fila);
        detIdField.setText(String.valueOf(tableModel.getValueAt(modelRow, 0)));
        detNombreField.setText((String) tableModel.getValueAt(modelRow, 1));
        detApellidoField.setText((String) tableModel.getValueAt(modelRow, 2));
        detDniField.setText((String) tableModel.getValueAt(modelRow, 3));
        detEmailField.setText((String) tableModel.getValueAt(modelRow, 4));
        detTelefonoField.setText((String) tableModel.getValueAt(modelRow, 5));
    }

    private void setDetalleEditable(boolean editable) {
        detNombreField.setEditable(editable);
        detApellidoField.setEditable(editable);
        detDniField.setEditable(editable);
        detEmailField.setEditable(editable);
        detTelefonoField.setEditable(editable);
    }

    private void mostrarBotones(boolean edicion) {
        botonesPanel.removeAll();
        if (edicion) {
            btnModificar.setText("Aceptar");
            botonesPanel.setLayout(new GridLayout(1, 2, 12, 0));
            botonesPanel.add(btnModificar);
            botonesPanel.add(btnCancelar);
        } else {
            btnModificar.setText("Modificar Cliente");
            botonesPanel.setLayout(new GridLayout(1, 3, 12, 0));
            botonesPanel.add(btnCrear);
            botonesPanel.add(btnEliminar);
            botonesPanel.add(btnModificar);
        }
        botonesPanel.revalidate();
        botonesPanel.repaint();
    }

}
