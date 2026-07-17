package bajasnet;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Diálogo reutilizable para elegir una promoción de un listado, con el mismo
 * aspecto y filtro que {@link MenuGestionPromocion} (tabla + filtro por servicio).
 *
 * Uso:
 *   Promociones p = SelectorPromociones.elegir(this);   // null si se canceló
 */
public class SelectorPromociones extends JDialog {

    private static final String[] COLUMNAS = {"ID", "Servicio", "Descuento", "Descripción"};
    private static final int[] ANCHOS = {90, 150, 90, 220};

    private final List<Promociones> promociones;   // mismo orden que las filas del modelo
    private final DefaultTableModel tableModel;
    private final JTable tabla;
    private final TableRowSorter<DefaultTableModel> sorter;

    private Promociones seleccionada;   // resultado (null si se cancela)

    private SelectorPromociones(Window owner, List<Promociones> promociones) {
        super(owner, "Seleccionar Promoción", ModalityType.APPLICATION_MODAL);   // modal
        this.promociones = promociones;
        setSize(640, 420);

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Filtro por servicio (igual que en Gestión de Promociones)
        JPanel filtroPanel = new JPanel(new BorderLayout(8, 0));
        filtroPanel.setOpaque(false);
        JLabel filtroLabel = new JLabel("Filtrar por servicio:");
        filtroLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JTextField filtroField = new JTextField();
        filtroPanel.add(filtroLabel, BorderLayout.WEST);
        filtroPanel.add(filtroField, BorderLayout.CENTER);
        panel.add(filtroPanel, BorderLayout.NORTH);

        // Tabla
        tableModel = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (Promociones p : promociones) {
            tableModel.addRow(new Object[]{p.getId(), p.getServicio(), p.getDescuento(), p.getDescripcion()});
        }
        tabla = new JTable(tableModel);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tabla.setRowHeight(22);
        tabla.setFillsViewportHeight(true);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        for (int i = 0; i < ANCHOS.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(ANCHOS[i]);
        }
        sorter = new TableRowSorter<>(tableModel);
        tabla.setRowSorter(sorter);

        filtroField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void filtrar() {
                String texto = filtroField.getText().trim();
                sorter.setRowFilter(texto.isEmpty() ? null
                    : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto), 1));
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
        });

        JScrollPane scrollTabla = new JScrollPane(tabla,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollTabla, BorderLayout.CENTER);

        // Botones
        JPanel botones = new JPanel(new GridLayout(1, 2, 12, 0));
        botones.setOpaque(false);
        JButton btnSeleccionar = new JButton("Seleccionar");
        JButton btnCancelar = new JButton("Cancelar");
        btnSeleccionar.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btnCancelar.setFont(new Font("SansSerif", Font.PLAIN, 13));
        botones.add(btnSeleccionar);
        botones.add(btnCancelar);
        panel.add(botones, BorderLayout.SOUTH);

        btnSeleccionar.addActionListener(e -> confirmar());
        btnCancelar.addActionListener(e -> dispose());
        // Doble click en una fila también selecciona.
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) confirmar();
            }
        });

        add(panel);
        setLocationRelativeTo(owner);
    }

    private void confirmar() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar una promoción.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = tabla.convertRowIndexToModel(fila);
        seleccionada = promociones.get(modelRow);
        dispose();
    }

    /**
     * Abre el selector y devuelve la promoción elegida, o null si se canceló
     * o si no hay promociones registradas.
     */
    public static Promociones elegir(Window parent) {
        List<Promociones> promociones = PromocionesServicio.listarPromociones();
        if (promociones.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "No hay promociones registradas.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        SelectorPromociones dialogo = new SelectorPromociones(parent, promociones);
        dialogo.setVisible(true);   // bloquea hasta cerrar (modal)
        return dialogo.seleccionada;
    }
}
