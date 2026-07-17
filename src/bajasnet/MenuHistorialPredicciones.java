package bajasnet;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Muestra el historial de predicciones (auditoría) guardado en predicciones.txt.
 * Es de solo lectura: se puede consultar y filtrar, pero no editar.
 */
public class MenuHistorialPredicciones extends JFrame {

    private static final String[] COLUMNAS = {
        "ID", "Fecha y Hora", "Operador", "Cliente", "Probabilidad", "Riesgo", "Recomendación"
    };

    private static final int[] ANCHOS = { 50, 140, 180, 110, 90, 70, 320 };

    private DefaultTableModel tableModel;
    private JTable tabla;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField filtroField;

    public MenuHistorialPredicciones(JFrame parent) {
        setTitle("Historial de Predicciones");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 500);

        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header: título + volver
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titulo = new JLabel("Historial de Predicciones");
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

        // Filtro (busca en cualquier columna: operador, cliente, fecha, etc.)
        JPanel filtroPanel = new JPanel(new BorderLayout(8, 0));
        filtroPanel.setOpaque(false);
        JLabel filtroLabel = new JLabel("Filtrar (operador, cliente, fecha...):");
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
                    : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto)));
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
        });

        JScrollPane scrollTabla = new JScrollPane(tabla,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        centro.add(scrollTabla, BorderLayout.CENTER);
        panel.add(centro, BorderLayout.CENTER);

        add(panel);
        setLocationRelativeTo(parent);
        cargarHistorial();
    }

    /** Carga las predicciones guardadas, de la más nueva a la más vieja. */
    private void cargarHistorial() {
        tableModel.setRowCount(0);
        List<Prediccion> predicciones = PrediccionDB.listar();
        for (int i = predicciones.size() - 1; i >= 0; i--) {   // más recientes primero
            Prediccion p = predicciones.get(i);
            tableModel.addRow(new Object[]{
                p.getIdRegistro(),
                p.getFechaHora(),
                p.getEmailOperador(),
                p.getCliente().getIdCliente(),
                String.format("%.1f%%", p.getProbabilidad() * 100),
                p.getRiesgo(),
                p.getRecomendacion()
            });
        }
    }
}
