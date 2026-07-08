package bajasnet;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MenuGestionPromocion extends JFrame {

    private DefaultTableModel modelo;
    private JTable tabla;
    private Operador operador;

    public MenuGestionPromocion(Operador operador) {
        super("Gestión de Promociones");
        this.operador = operador;

        PromocionesDB.inicializar();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 420);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // ----- Tabla -----
        modelo = new DefaultTableModel(
                new Object[]{"ID", "Servicio", "Descuento", "Descripción"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabla = new JTable(modelo);
        tabla.setRowHeight(24);

        JScrollPane scroll = new JScrollPane(tabla);
        add(scroll, BorderLayout.CENTER);

        // ----- Botones -----
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton btnNueva = new JButton("Nueva promoción");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnAsignar = new JButton("Asignar a Cliente");
        JButton btnVolver = new JButton("Volver");

        panelBotones.add(btnNueva);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnAsignar);
        panelBotones.add(btnVolver);

        add(panelBotones, BorderLayout.SOUTH);

        // Si no es administrador
        if (!operador.isAdmin()) {
            btnNueva.setVisible(false);
            btnEditar.setVisible(false);
            btnEliminar.setVisible(false);
        }

        btnNueva.addActionListener(e -> nuevaPromocion());
        btnEditar.addActionListener(e -> editarPromocion());
        btnEliminar.addActionListener(e -> eliminarPromocion());

        btnAsignar.addActionListener(e -> asignarPromocion());

        btnVolver.addActionListener(e -> dispose());

        cargarTabla();
    }

    private void cargarTabla() {
        modelo.setRowCount(0);

        List<Promociones> lista = PromocionesDB.cargar();

        for (Promociones p : lista) {
            modelo.addRow(new Object[]{
                    p.getId(),
                    p.getServicio(),
                    p.getDescuento(),
                    p.getDescripcion()
            });
        }
    }

    private String getIdSeleccionado() {

        int fila = tabla.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar una promoción.",
                    "Sin selección",
                    JOptionPane.WARNING_MESSAGE);

            return null;
        }

        return (String) modelo.getValueAt(fila, 0);
    }

    private void nuevaPromocion() {

        JTextField txtServicio = new JTextField();
        JTextField txtDescuento = new JTextField();
        JTextField txtDescripcion = new JTextField();

        JPanel panel = construirFormulario(
                txtServicio,
                txtDescuento,
                txtDescripcion);

        int opcion = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Nueva promoción",
                JOptionPane.OK_CANCEL_OPTION);

        if (opcion == JOptionPane.OK_OPTION) {

            String mensaje = PromocionesServicio.validarPromocion(
                    txtServicio.getText().trim(),
                    txtDescuento.getText().trim(),
                    txtDescripcion.getText().trim());

            mostrarResultado(mensaje);
            cargarTabla();
        }
    }

    private void editarPromocion() {

        String id = getIdSeleccionado();

        if (id == null)
            return;

        Promociones actual = PromocionesDB.buscarPromocion(id);

        JTextField txtServicio = new JTextField(actual.getServicio());
        JTextField txtDescuento = new JTextField(actual.getDescuento());
        JTextField txtDescripcion = new JTextField(actual.getDescripcion());

        JPanel panel = construirFormulario(
                txtServicio,
                txtDescuento,
                txtDescripcion);

        int opcion = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Editar promoción",
                JOptionPane.OK_CANCEL_OPTION);

        if (opcion == JOptionPane.OK_OPTION) {

            String mensaje = PromocionesServicio.editarPromocion(
                    id,
                    txtServicio.getText().trim(),
                    txtDescuento.getText().trim(),
                    txtDescripcion.getText().trim());

            mostrarResultado(mensaje);
            cargarTabla();
        }
    }

    private void eliminarPromocion() {

        String id = getIdSeleccionado();

        if (id == null)
            return;

        int confirmar = JOptionPane.showConfirmDialog(
                this,
                "¿Eliminar esta promoción?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION);

        String mensaje = PromocionesServicio.eliminarPromocion(
                id,
                confirmar == JOptionPane.YES_OPTION);

        mostrarResultado(mensaje);
        cargarTabla();
    }
    private void asignarPromocion() {

    List<Cliente> clientes = ClienteServicio.listarClientes();

    if (clientes.isEmpty()) {
        JOptionPane.showMessageDialog(this,
                "No hay clientes registrados.");
        return;
    }

    DefaultTableModel modeloClientes = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "Apellido", "DNI"}, 0);

    for (Cliente c : clientes) {
        modeloClientes.addRow(new Object[]{
                c.getId(),
                c.getNombre(),
                c.getApellido(),
                c.getDni()
        });
    }

    JTable tablaClientes = new JTable(modeloClientes);
    JScrollPane scroll = new JScrollPane(tablaClientes);

    int opcion = JOptionPane.showConfirmDialog(
            this,
            scroll,
            "Seleccionar Cliente",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);

    if (opcion == JOptionPane.OK_OPTION) {

        int fila = tablaClientes.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar un cliente.");
            return;
        }

        int idCliente = (Integer) modeloClientes.getValueAt(fila, 0);

        String idPromocion = getIdSeleccionado();

        if (idPromocion == null)
            return;

        JOptionPane.showMessageDialog(this,
                "Cliente ID: " + idCliente +
                "\nPromoción: " + idPromocion +
                "\n\nLa asignación se realizará aquí.");
    }
}

    private JPanel construirFormulario(
            JTextField servicio,
            JTextField descuento,
            JTextField descripcion) {

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));

        panel.add(new JLabel("Servicio:"));
        panel.add(servicio);

        panel.add(new JLabel("Descuento:"));
        panel.add(descuento);

        panel.add(new JLabel("Descripción:"));
        panel.add(descripcion);

        return panel;
    }

    private void mostrarResultado(String mensaje) {
        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "Información",
                JOptionPane.INFORMATION_MESSAGE);
    }
}