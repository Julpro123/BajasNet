package bajasnet;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Muestra las promociones asignadas a un cliente y permite quitarlas.
 */
public class PromocionesDelCliente extends JDialog {

    private static final String[] COLUMNAS = {"ID", "Servicio", "Descuento", "Descripción"};
    private static final int[] ANCHOS = {90, 150, 90, 220};

    private final String idCliente;
    private List<Promociones> promociones;
    private final DefaultTableModel tableModel;
    private final JTable tabla;

    private PromocionesDelCliente(Frame owner, String idCliente) {
        super(owner, "Promociones del cliente " + idCliente, true);   // modal
        this.idCliente = idCliente;
        setSize(640, 420);

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel titulo = new JLabel("Promociones asignadas a " + idCliente);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        titulo.setForeground(new Color(30, 40, 60));
        panel.add(titulo, BorderLayout.NORTH);

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
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        for (int i = 0; i < ANCHOS.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(ANCHOS[i]);
        }
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel botones = new JPanel(new GridLayout(1, 3, 12, 0));
        botones.setOpaque(false);
        JButton btnAsignar = new JButton("Asignar Promoción");
        JButton btnQuitar = new JButton("Quitar Promoción");
        JButton btnCerrar = new JButton("Cerrar");
        btnAsignar.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btnQuitar.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btnCerrar.setFont(new Font("SansSerif", Font.PLAIN, 13));
        botones.add(btnAsignar);
        botones.add(btnQuitar);
        botones.add(btnCerrar);
        panel.add(botones, BorderLayout.SOUTH);

        btnAsignar.addActionListener(e -> asignar());
        btnQuitar.addActionListener(e -> quitar());
        btnCerrar.addActionListener(e -> dispose());

        add(panel);
        setLocationRelativeTo(owner);
        cargar();
    }

    private void cargar() {
        tableModel.setRowCount(0);
        promociones = AsignacionServicio.promocionesDeCliente(idCliente);
        for (Promociones p : promociones) {
            tableModel.addRow(new Object[]{p.getId(), p.getServicio(), p.getDescuento(), p.getDescripcion()});
        }
    }

    private void asignar() {
        // Otro listado: selector de promociones (mismo aspecto que Gestión de Promociones, con filtro).
        Promociones elegida = SelectorPromociones.elegir(this);
        if (elegida == null) return;   // canceló o no hay promociones

        String error = AsignacionServicio.asignar(idCliente, elegida.getId());
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Info", JOptionPane.WARNING_MESSAGE);
            return;
        }
        cargar();   // refresca la tabla con la nueva promoción
    }

    private void quitar() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccioná una promoción para quitar.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Promociones p = promociones.get(fila);   // sin sorter: fila de vista == fila del modelo
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Quitar la promoción \"" + p.getServicio() + "\" del cliente " + idCliente + "?",
            "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        AsignacionServicio.desasignar(idCliente, p.getId());
        cargar();   // refresca la tabla
    }

    /** Abre la ventana con las promociones asignadas al cliente. */
    public static void mostrar(Frame parent, String idCliente) {
        new PromocionesDelCliente(parent, idCliente).setVisible(true);
    }
}
