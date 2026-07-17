package bajasnet;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class MenuGestionClientes extends JFrame {

    private static final String[] COLUMNAS = {
        "ID Cliente", "Género", "Ciudadano Mayor", "Pareja", "Dependientes", "Antigüedad",
        "Servicio Telefónico", "Líneas Múltiples", "Servicio Internet", "Seguridad Online", "Respaldo Online",
        "Protección Dispositivo", "Soporte Técnico", "Streaming TV", "Streaming Películas", "Contrato",
        "Factura sin Papel", "Método de Pago", "Cargos Mensuales", "Cargos Totales"
    };

    private static final int[] ANCHOS = {
        90, 70, 90, 60, 80, 60, 90, 110, 100, 110, 100, 120, 100, 100, 120, 130, 130, 170, 110, 100
    };

    private DefaultTableModel tableModel;
    private JTable tabla;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField filtroField;
    
    private JComponent[] detFields = new JComponent[COLUMNAS.length];
    
    private JLabel detErrorLabel;
    private JPanel botonesPanel;
    private JButton btnCrear, btnEliminar, btnModificar, btnCancelar, btnPredecir, btnVerPromos;
    private boolean modoEdicion = false;
    private boolean modoCreacion = false;
    private String idEnEdicion;

    private final Operador operador;   // operador logueado (para auditar quién predice)

    public MenuGestionClientes(JFrame parent, Operador operador) {
        this.operador = operador;
        setTitle("Gestión de Clientes");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1300, 600);

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

        JPanel centro = new JPanel(new BorderLayout(16, 8));
        centro.setOpaque(false);

        JPanel filtroPanel = new JPanel(new BorderLayout(8, 0));
        filtroPanel.setOpaque(false);
        JLabel filtroLabel = new JLabel("Filtrar por ID de Cliente:");
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
        for (int i = 0; i < ANCHOS.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(ANCHOS[i]);
        }
        sorter = new TableRowSorter<>(tableModel);
        tabla.setRowSorter(sorter);

        filtroField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void filtrar() {
                String texto = filtroField.getText().trim();
                sorter.setRowFilter(texto.isEmpty() ? null
                    : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto), 0));
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
            for (int i = 0; i < detFields.length; i++) {
                String valor = String.valueOf(tableModel.getValueAt(modelRow, i));
                setValorComponente(detFields[i], valor);
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tabla,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollTabla.setPreferredSize(new Dimension(850, 300));
        centro.add(scrollTabla, BorderLayout.CENTER);

        JScrollPane scrollDetalle = new JScrollPane(construirPanelDetalle(),
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollDetalle.setPreferredSize(new Dimension(260, 300));
        scrollDetalle.getVerticalScrollBar().setUnitIncrement(14);
        centro.add(scrollDetalle, BorderLayout.EAST);

        panel.add(centro, BorderLayout.CENTER);

        botonesPanel = new JPanel(new GridLayout(1, 5, 12, 0));
        botonesPanel.setOpaque(false);
        btnCrear     = new JButton("Crear Cliente");
        btnEliminar  = new JButton("Eliminar Cliente");
        btnModificar = new JButton("Modificar Cliente");
        btnPredecir  = new JButton("Predecir Churn");
        btnVerPromos = new JButton("Ver Promociones");
        btnCancelar  = new JButton("Cancelar");
        for (JButton b : new JButton[]{btnCrear, btnEliminar, btnModificar, btnPredecir, btnVerPromos, btnCancelar})
            b.setFont(new Font("SansSerif", Font.PLAIN, 13));

        botonesPanel.add(btnCrear);
        botonesPanel.add(btnEliminar);
        botonesPanel.add(btnModificar);
        botonesPanel.add(btnPredecir);
        botonesPanel.add(btnVerPromos);
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
        btnPredecir.addActionListener(e -> {
            try {
                predecirClienteSeleccionado();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error inesperado: " + ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnVerPromos.addActionListener(e -> {
            try {
                verPromocionesDelCliente();
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
        detalle.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 8));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;

        JLabel titulo = new JLabel("Detalle");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 13));
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 8, 0);
        detalle.add(titulo, gbc);

        int fila = 1;
        for (int i = 0; i < COLUMNAS.length; i++) {
            JLabel lbl = new JLabel(COLUMNAS[i] + ":");
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
            gbc.gridy = fila++;
            gbc.insets = new Insets(6, 0, 2, 0);
            detalle.add(lbl, gbc);

            detFields[i] = crearComponenteSegunServicio(COLUMNAS[i]);
            setComponenteEditable(detFields[i], false);
            
            gbc.gridy = fila++;
            gbc.insets = new Insets(0, 0, 4, 0);
            detalle.add(detFields[i], gbc);
        }

        detErrorLabel = new JLabel(" ");
        detErrorLabel.setForeground(Color.RED);
        detErrorLabel.setFont(detErrorLabel.getFont().deriveFont(Font.PLAIN, 11f));
        gbc.gridy = fila++;
        gbc.insets = new Insets(6, 0, 0, 0);
        detalle.add(detErrorLabel, gbc);

        return detalle;
    }

    private JComponent crearComponenteSegunServicio(String columna) {
        switch (columna) {
            case "Género":
                return vincularControl(new JComboBox<>(new String[]{"Male", "Female"}));
            case "Ciudadano Mayor":
                return vincularControl(new JComboBox<>(new String[]{"0", "1"}));
            case "Pareja":
            case "Dependientes":
            case "Servicio Telefónico":
            case "Factura sin Papel":
                return vincularControl(new JComboBox<>(new String[]{"No", "Yes"}));
            case "Líneas Múltiples":
                return vincularControl(new JComboBox<>(new String[]{"No", "Yes", "No phone service"}));
            case "Servicio Internet":
                return vincularControl(new JComboBox<>(new String[]{"No", "DSL", "Fiber optic"}));
            case "Seguridad Online":
            case "Respaldo Online":
            case "Protección Dispositivo":
            case "Soporte Técnico":
            case "Streaming TV":
            case "Streaming Películas":
                return vincularControl(new JComboBox<>(new String[]{"No", "Yes", "No internet service"}));
            case "Contrato":
                return vincularControl(new JComboBox<>(new String[]{"Month-to-month", "One year", "Two year"}));
            case "Método de Pago":
                return vincularControl(new JComboBox<>(new String[]{"Electronic check", "Mailed check", "Bank transfer (automatic)", "Credit card (automatic)"}));
            default:
                return new JTextField();
        }
    }

    // Intercepta los cambios para restaurar el valor original si el usuario NO está editando ni creando
    private JComboBox<String> vincularControl(JComboBox<String> combo) {
        combo.putClientProperty("indexBloqueado", 0);
        combo.addActionListener(e -> {
            if (!modoCreacion && !modoEdicion) {
                int indexCorrecto = (int) combo.getClientProperty("indexBloqueado");
                if (combo.getSelectedIndex() != indexCorrecto) {
                    // Quitamos temporalmente el listener para que el setSelectedIndex no provoque un bucle
                    ActionListener[] listeners = combo.getActionListeners();
                    for (ActionListener l : listeners) combo.removeActionListener(l);
                    
                    combo.setSelectedIndex(indexCorrecto);
                    
                    for (ActionListener l : listeners) combo.addActionListener(l);
                }
            } else {
                // Si el usuario sí está editando de verdad, guardamos su nueva elección sobre la marcha
                combo.putClientProperty("indexBloqueado", combo.getSelectedIndex());
            }
        });
        return combo;
    }

    private void limpiarDetalle() {
        for (JComponent c : detFields) {
            if (c instanceof JTextField) {
                ((JTextField) c).setText("");
            } else if (c instanceof JComboBox) {
                JComboBox<?> combo = (JComboBox<?>) c;
                combo.setSelectedIndex(0); 
                combo.putClientProperty("indexBloqueado", 0);
            }
        }
    }

    private void setValorComponente(JComponent c, String valor) {
        if (c instanceof JTextField) {
            ((JTextField) c).setText(valor);
        } else if (c instanceof JComboBox) {
            JComboBox<?> combo = (JComboBox<?>) c;
            combo.setSelectedItem(valor);
            combo.putClientProperty("indexBloqueado", combo.getSelectedIndex());
        }
    }

    private String getValorComponente(JComponent c) {
        if (c instanceof JTextField) {
            return ((JTextField) c).getText().trim();
        } else if (c instanceof JComboBox) {
            Object selected = ((JComboBox<?>) c).getSelectedItem();
            return selected != null ? selected.toString() : "";
        }
        return "";
    }

    private void setComponenteEditable(JComponent c, boolean editable) {
        if (c instanceof JTextField) {
            JTextField tf = (JTextField) c;
            tf.setEditable(editable);
            tf.setBackground(editable ? Color.WHITE : new Color(240, 240, 240));
        } else if (c instanceof JComboBox) {
            JComboBox<?> cb = (JComboBox<?>) c;
            // ¡MANTENEMOS SIEMPRE ENABLED(TRUE)! Así el combo responde al mouse, despliega y no se congela.
            cb.setEnabled(true); 
            cb.setBackground(editable ? Color.WHITE : new Color(242, 242, 242));
        }
    }

    private void actualizarTabla() {
        tableModel.setRowCount(0);
        List<Cliente> clientes = ClienteServicio.listarClientes();
        for (Cliente c : clientes) {
            tableModel.addRow(c.toArray());
        }
    }

    private void iniciarEdicion() {
        if (tabla.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Seleccioná un cliente de la tabla para modificar.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        idEnEdicion = getValorComponente(detFields[0]);
        modoEdicion = true;
        detErrorLabel.setText(" ");
        setDetalleEditable(true);
        mostrarBotones(true);
        detFields[1].requestFocusInWindow();
    }

    private void confirmarEdicion() {
        String[] v = valoresDetalle();
        String error = ClienteServicio.validarDatos(idEnEdicion, v[1], v[2], v[3], v[4], v[5], v[6], v[7],
                v[8], v[9], v[10], v[11], v[12], v[13], v[14], v[15], v[16], v[17], v[18], v[19]);
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
        String idCliente = getValorComponente(detFields[0]);

        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Eliminar al cliente \"" + idCliente + "\"?", "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            ClienteServicio.eliminarCliente(idCliente);
            actualizarTabla();
            limpiarDetalle();
            JOptionPane.showMessageDialog(this, "Cliente eliminado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void predecirClienteSeleccionado() throws Exception {
        if (tabla.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Seleccioná un cliente de la tabla para predecir.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] v = valoresDetalle();
        String idCliente = v[0];
        Cliente c = new Cliente(v[0],v[1], v[2], v[3], v[4], v[5], v[6], v[7],
                v[8], v[9], v[10], v[11], v[12], v[13], v[14], v[15], v[16], v[17], v[18], v[19]);

        // Predecir y registrar en el historial de auditoría (queda quién, cuándo y con qué datos).
        String emailOperador = operador != null ? operador.getEmail() : "desconocido";
        double prob = PrediccionServicio.predecirYRegistrar(c, emailOperador);
        JOptionPane.showMessageDialog(this,
            String.format("Cliente: %s%n%nProbabilidad de baja (churn): %.1f%%%nRiesgo: %s%n%n%s",
                idCliente, prob * 100, PrediccionServicio.nivelRiesgo(prob), PrediccionServicio.recomendacion(prob)),
            "Predicción de Churn", JOptionPane.INFORMATION_MESSAGE);
}

    private void verPromocionesDelCliente() {
        if (tabla.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Seleccioná un cliente para ver sus promociones.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String idCliente = getValorComponente(detFields[0]);
        PromocionesDelCliente.mostrar(this, idCliente);
    }

    private void iniciarCreacion() {
        tabla.clearSelection();
        limpiarDetalle();
        modoCreacion = true;
        detErrorLabel.setText(" ");
        setDetalleEditable(true);
        mostrarBotones(true);
        detFields[1].requestFocusInWindow();
    }

    private void confirmarCreacion() {
        String[] v = valoresDetalle();
        String error = ClienteServicio.validarDatos(null, v[1], v[2], v[3], v[4], v[5], v[6], v[7], v[8], v[9],
                v[10], v[11], v[12], v[13], v[14], v[15], v[16], v[17], v[18], v[19]);
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
        for (int i = 0; i < detFields.length; i++) {
            String valor = String.valueOf(tableModel.getValueAt(modelRow, i));
            setValorComponente(detFields[i], valor);
        }
    }

    private String[] valoresDetalle() {
        String[] v = new String[detFields.length];
        for (int i = 0; i < detFields.length; i++) {
            v[i] = getValorComponente(detFields[i]);
        }
        return v;
    }

    private void setDetalleEditable(boolean editable) {
        for (int i = 1; i < detFields.length; i++) {
            setComponenteEditable(detFields[i], editable);
        }
        // El ID se genera solo: nunca es editable.
        setComponenteEditable(detFields[0], false);
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
            botonesPanel.setLayout(new GridLayout(1, 5, 12, 0));
            botonesPanel.add(btnCrear);
            botonesPanel.add(btnEliminar);
            botonesPanel.add(btnModificar);
            botonesPanel.add(btnPredecir);
            botonesPanel.add(btnVerPromos);
        }
        botonesPanel.revalidate();
        botonesPanel.repaint();
    }
}