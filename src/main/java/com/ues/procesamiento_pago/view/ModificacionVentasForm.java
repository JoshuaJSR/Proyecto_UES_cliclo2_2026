package com.ues.procesamiento_pago.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.ues.procesamiento_pago.Model.EstadoPago;
import com.ues.procesamiento_pago.Strategy.MetodoPago;
import com.ues.procesamiento_pago.config.MongoDBConnection;
import com.ues.procesamiento_pago.dao.TransaccionDAO;
import com.ues.procesamiento_pago.dao.VentaDAO;
import com.ues.procesamiento_pago.util.Estilos;

public class ModificacionVentasForm extends JFrame {
    private static final String TODOS = "Todos";

    private JTextField txtFiltroId;
    private JComboBox<String> comboFiltroEstado;
    private JComboBox<String> comboFiltroMetodo;
    private JButton btnBuscar;
    private JButton btnLimpiarFiltros;
    private JButton btnGuardar;
    private JButton btnAnular;
    private JLabel lblResumen;
    private JTable tablaTransacciones;
    private DefaultTableModel modeloTabla;

    private JTextField txtIdVenta;
    private JTextField txtMonto;
    private JComboBox<String> comboEstado;
    private JComboBox<String> comboMetodo;

    private Document transaccionSeleccionada;

    private final VentaDAO daoVenta;
    private final TransaccionDAO daoTransaccion;

    private static final String[] METODOS = {
        "Efectivo", "Pago con tarjeta", "Pago Electronico", "Transferencia Bancaria"
    };

    private static final String[] ESTADOS = {"PENDIENTE", "PROCESADO", "RECHAZADO"};

    private static final String[] COLUMNAS = {"N° VENTA", "MÉTODO DE PAGO", "MONTO", "ESTADO", "FECHA"};

    private static final Color COLOR_FONDO = Color.DARK_GRAY;
    private static final Color COLOR_TEXTO_SECUNDARIO = new Color(160, 160, 160);
    private static final Color COLOR_VERDE = new Color(60, 220, 130);
    private static final Color COLOR_ROJO = new Color(220, 60, 60);
    private static final Font FUENTE_ETIQUETA = new Font("SansSerif", Font.BOLD, 12);
    private static final Insets MARGEN = new Insets(3, 0, 3, 0);
    private static final Insets SIN_MARGEN = new Insets(0, 0, 0, 0);
    private static final SimpleDateFormat FORMATO_FECHA = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public ModificacionVentasForm(MongoDBConnection mongoDB) {
        if (mongoDB != null && mongoDB.getDatabase() != null) {
            this.daoVenta = new VentaDAO(mongoDB);
            this.daoTransaccion = new TransaccionDAO(mongoDB);
        } else {
            this.daoVenta = null;
            this.daoTransaccion = null;
            JOptionPane.showMessageDialog(null, "No hay una conexión activa a MongoDB.");
        }

        setTitle("Modificación de ventas");
        setSize(1000, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        add(crearContenido());
        conectarEventos();

        if (daoTransaccion != null) {
            buscar();
        }
    }

    private JPanel crearContenido() {
        JPanel panel = crearPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 18, 16, 18));

        JLabel lblTitulo = new JLabel("MODIFICACIÓN DE VENTAS", JLabel.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblTitulo.setForeground(Color.WHITE);

        agregar(panel, lblTitulo, 0, 0, new Insets(0, 0, 10, 0));
        agregar(panel, crearPanelFiltros(), 1, 0, MARGEN);
        agregar(panel, crearPanelTabla(), 2, 1, SIN_MARGEN);
        agregar(panel, crearPanelEdicion(), 3, 0, new Insets(10, 0, 4, 0));
        agregar(panel, crearPanelBotones(), 4, 0, SIN_MARGEN);
        return panel;
    }

    private JPanel crearPanelFiltros() {
        JPanel panel = crearPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));

        txtFiltroId = campo();
        txtFiltroId.setPreferredSize(new Dimension(90, 32));

        comboFiltroEstado = combo(ESTADOS, true);
        comboFiltroMetodo = combo(METODOS, true);
        btnBuscar = boton("BUSCAR", COLOR_VERDE, Color.BLACK);
        btnLimpiarFiltros = boton("LIMPIAR FILTROS", COLOR_ROJO, Color.WHITE);

        panel.add(etiqueta("N° DE VENTA:"));
        panel.add(txtFiltroId);
        panel.add(etiqueta("ESTADO:"));
        panel.add(comboFiltroEstado);
        panel.add(etiqueta("MÉTODO DE PAGO:"));
        panel.add(comboFiltroMetodo);
        panel.add(btnBuscar);
        panel.add(btnLimpiarFiltros);
        return panel;
    }

    private JPanel crearPanelTabla() {
        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaTransacciones = new JTable(modeloTabla);
        tablaTransacciones.setBackground(new Color(25, 25, 25));
        tablaTransacciones.setForeground(Color.WHITE);
        tablaTransacciones.setGridColor(new Color(60, 60, 60));
        tablaTransacciones.setSelectionBackground(new Color(45, 120, 85));
        tablaTransacciones.setSelectionForeground(Color.WHITE);
        tablaTransacciones.setRowHeight(24);
        tablaTransacciones.getTableHeader().setBackground(Color.BLACK);
        tablaTransacciones.getTableHeader().setForeground(Color.WHITE);
        tablaTransacciones.getTableHeader().setFont(FUENTE_ETIQUETA);
        tablaTransacciones.setAutoCreateRowSorter(true);

        JPanel panel = crearPanel(new BorderLayout(0, 4));
        panel.add(new JScrollPane(tablaTransacciones), BorderLayout.CENTER);

        lblResumen = new JLabel(" ");
        lblResumen.setForeground(COLOR_TEXTO_SECUNDARIO);
        lblResumen.setFont(FUENTE_ETIQUETA);
        panel.add(lblResumen, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelEdicion() {
        JPanel panel = crearPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        txtIdVenta = campo();
        txtIdVenta.setEditable(false);
        txtMonto = campo();
        txtMonto.setEditable(false);
        comboEstado = combo(ESTADOS, false);
        comboMetodo = combo(METODOS, false);

        agregarCampo(panel, "N° DE VENTA:", txtIdVenta, 0, 0);
        agregarCampo(panel, "MONTO:", txtMonto, 2, 0);
        agregarCampo(panel, "ESTADO:", comboEstado, 4, 0);
        agregarCampo(panel, "MÉTODO DE PAGO:", comboMetodo, 6, 0);
        return panel;
    }

    private JPanel crearPanelBotones() {
        JPanel panel = crearPanel(new BorderLayout());
        btnGuardar = boton("GUARDAR CAMBIOS", COLOR_VERDE, Color.BLACK);
        btnAnular = boton("ANULAR VENTA", COLOR_ROJO, Color.WHITE);
        JButton btnCancelar = boton("Cancelar", COLOR_ROJO, Color.WHITE);
        btnCancelar.addActionListener(e -> dispose());
        JPanel izquierda = crearPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        izquierda.add(btnGuardar);
        izquierda.add(btnAnular);
        panel.add(izquierda, BorderLayout.WEST);
        panel.add(btnCancelar, BorderLayout.EAST);
        return panel;
    }

    private void conectarEventos() {
        btnBuscar.addActionListener(e -> buscar());
        btnLimpiarFiltros.addActionListener(e -> limpiarFiltros());
        btnGuardar.addActionListener(e -> guardarCambios());
        btnAnular.addActionListener(e -> anularVenta());
        txtFiltroId.addActionListener(e -> buscar());

        tablaTransacciones.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaTransacciones.getSelectedRow() != -1) {
                int fila = tablaTransacciones.convertRowIndexToModel(tablaTransacciones.getSelectedRow());
                if (fila < 0 || fila >= modeloTabla.getRowCount()) {
                    return;
                }
                transaccionSeleccionada = obtenerTransaccionDeFila(fila);
                mostrarTransaccion(transaccionSeleccionada);
            }
        });
    }

    // recupera el Document de la transaccion de la fila indicada 
    private final List<Document> transaccionesActuales = new java.util.ArrayList<>();

    private Document obtenerTransaccionDeFila(int fila) {
        if (fila < 0 || fila >= transaccionesActuales.size()) {
            return null;
        }
        return transaccionesActuales.get(fila);
    }

    private void buscar() {
        if (daoTransaccion == null) {
            JOptionPane.showMessageDialog(this, "No hay conexión con la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Integer numeroVenta = null;
        String textoId = txtFiltroId.getText().trim();
        if (!textoId.isEmpty()) {
            try {
                numeroVenta = Integer.parseInt(textoId);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "El número de venta debe ser numérico.", "Error de formato", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String estado = valorFiltro(comboFiltroEstado);
        String metodo = valorFiltro(comboFiltroMetodo);

        List<Document> transacciones = daoTransaccion.listar(numeroVenta, estado, metodo);
        modeloTabla.setRowCount(0);
        transaccionesActuales.clear();
        for (Document t : transacciones) {
            transaccionesActuales.add(t);
            Integer idVentaValor = t.getInteger("idVenta");
            int idVenta = idVentaValor != null ? idVentaValor.intValue() : 0;
            String metodoPago = t.getString("metodo");
            BigDecimal monto = t.get("monto") instanceof BigDecimal ? (BigDecimal) t.get("monto")
                    : t.get("monto") instanceof org.bson.types.Decimal128
                            ? ((org.bson.types.Decimal128) t.get("monto")).bigDecimalValue()
                            : new BigDecimal(String.valueOf(t.get("monto")));
            String est = t.getString("estado");
            Date fecha = t.getDate("fecha");
            modeloTabla.addRow(new Object[]{
                    idVenta,
                    metodoPago,
                    "$" + MetodoPago.formatear(monto),
                    est,
                    fecha == null ? "" : FORMATO_FECHA.format(fecha)
            });
        }
        limpiarSeleccion();
        lblResumen.setText(transacciones.isEmpty()
                ? "No se encontraron transacciones con esos filtros."
                : transacciones.size() + " transacción(es) encontrada(s) (seleccione una fila para modificarla).");
    }

    private void limpiarFiltros() {
        txtFiltroId.setText("");
        comboFiltroEstado.setSelectedIndex(0);
        comboFiltroMetodo.setSelectedIndex(0);
        buscar();
    }

    private void mostrarTransaccion(Document documento) {
        if (documento == null) {
            return;
        }
        Integer idVentaDocumento = documento.getInteger("idVenta");
        int idVenta = idVentaDocumento == null ? 0 : idVentaDocumento;
        txtIdVenta.setText(String.valueOf(idVenta));
        Object montoObj = documento.get("monto");
        BigDecimal monto = montoObj instanceof BigDecimal ? (BigDecimal) montoObj
                : montoObj instanceof org.bson.types.Decimal128 ? ((org.bson.types.Decimal128) montoObj).bigDecimalValue()
                : new BigDecimal(String.valueOf(montoObj));
        txtMonto.setText("$" + MetodoPago.formatear(monto));
        seleccionar(comboEstado, documento.getString("estado"));
        seleccionar(comboMetodo, documento.getString("metodo"));
    }

    private void asegurarSeleccion() {
        if (transaccionSeleccionada == null && tablaTransacciones.getSelectedRow() != -1) {
            int fila = tablaTransacciones.convertRowIndexToModel(tablaTransacciones.getSelectedRow());
            transaccionSeleccionada = obtenerTransaccionDeFila(fila);
        }
    }

    private void guardarCambios() {
        asegurarSeleccion();
        if (transaccionSeleccionada == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una transacción de la tabla para modificarla.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (daoTransaccion == null || daoVenta == null) {
            JOptionPane.showMessageDialog(this, "No hay conexión con la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Integer idVenta = transaccionSeleccionada.getInteger("idVenta");
        int numeroVenta = idVenta != null ? idVenta : 0;
        ObjectId idTransaccion = transaccionSeleccionada.getObjectId("_id");
        String nuevoEstadoStr = (String) comboEstado.getSelectedItem();
        String nuevoMetodo = (String) comboMetodo.getSelectedItem();
        EstadoPago nuevoEstado;
        try {
            nuevoEstado = EstadoPago.valueOf(nuevoEstadoStr);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Estado inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean txOk = daoTransaccion.actualizar(idTransaccion, nuevoEstado, nuevoMetodo);
        boolean ventaOk = daoVenta.cambiarEstado(numeroVenta, nuevoEstado, nuevoMetodo);

        if (!txOk && !ventaOk) {
            JOptionPane.showMessageDialog(this, "No se pudo actualizar la transacción.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "Transacción modificada correctamente.", "Transacción actualizada", JOptionPane.INFORMATION_MESSAGE);
        buscar();
    }

    private void anularVenta() {
        asegurarSeleccion();
        if (transaccionSeleccionada == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una transacción de la tabla para anularla.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (daoTransaccion == null || daoVenta == null) {
            JOptionPane.showMessageDialog(this, "No hay conexión con la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Integer idVenta = transaccionSeleccionada.getInteger("idVenta");
        int numeroVenta = (idVenta != null) ? idVenta : 0;
        String estadoActual = transaccionSeleccionada.getString("estado");
        if ("PROCESADO".equals(estadoActual)) {
            int confirmProc = JOptionPane.showConfirmDialog(this,
                    "La venta N° " + numeroVenta + " está PROCESADA. ¿Anularla de todos modos?",
                    "Confirmar anulación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirmProc != JOptionPane.YES_OPTION) {
                return;
            }
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Anular la venta N° " + numeroVenta + " y su transacción? Esta acción no se puede deshacer.",
                "Confirmar anulación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        daoTransaccion.eliminarPorVenta(numeroVenta);
        daoVenta.eliminarVenta(numeroVenta);

        JOptionPane.showMessageDialog(this, "Venta N° " + numeroVenta + " anulada.", "Anulación", JOptionPane.INFORMATION_MESSAGE);
        buscar();
    }

    private void limpiarSeleccion() {
        transaccionSeleccionada = null;
        tablaTransacciones.clearSelection();
        txtIdVenta.setText("");
        txtMonto.setText("");
        comboEstado.setSelectedIndex(0);
        comboMetodo.setSelectedIndex(0);
    }

    private String valorFiltro(JComboBox<String> combo) {
        String valor = (String) combo.getSelectedItem();
        return valor == null || TODOS.equals(valor) ? null : valor;
    }

    private static void seleccionar(JComboBox<String> combo, String valor) {
        if (valor == null) {
            combo.setSelectedIndex(0);
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (valor.equals(combo.getItemAt(i))) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        combo.setSelectedIndex(0);
    }

    private static void agregar(JPanel panel, Component comp, int fila, double pesoVertical, Insets margen) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 1.0;
        gbc.weighty = pesoVertical;
        gbc.fill = pesoVertical > 0 ? GridBagConstraints.BOTH : GridBagConstraints.HORIZONTAL;
        gbc.insets = margen;
        panel.add(comp, gbc);
    }

    private static void agregarCampo(JPanel panel, String texto, Component comp, int columna, int fila) {
        GridBagConstraints gbcEtiqueta = new GridBagConstraints();
        gbcEtiqueta.gridx = columna;
        gbcEtiqueta.gridy = fila;
        gbcEtiqueta.weightx = 0.0;
        gbcEtiqueta.fill = GridBagConstraints.HORIZONTAL;
        gbcEtiqueta.insets = new Insets(4, 0, 0, 8);
        panel.add(etiqueta(texto), gbcEtiqueta);

        GridBagConstraints gbcCampo = new GridBagConstraints();
        gbcCampo.gridx = columna + 1;
        gbcCampo.gridy = fila + 1;
        gbcCampo.weightx = 0.25;
        gbcCampo.fill = GridBagConstraints.HORIZONTAL;
        gbcCampo.insets = new Insets(0, 0, 4, 8);
        panel.add(comp, gbcCampo);
    }

    private static JPanel crearPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(COLOR_FONDO);
        return panel;
    }

    private static JLabel etiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(COLOR_TEXTO_SECUNDARIO);
        etiqueta.setFont(FUENTE_ETIQUETA);
        return etiqueta;
    }

    private static JTextField campo() {
        JTextField campo = new JTextField();
        Estilos.estilarCampo(campo);
        return campo;
    }

    private static JComboBox<String> combo(String[] valores, boolean incluirTodos) {
        String[] items;
        if (incluirTodos) {
            items = new String[valores.length + 1];
            items[0] = TODOS;
            System.arraycopy(valores, 0, items, 1, valores.length);
        } else {
            items = valores;
        }
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setPreferredSize(new Dimension(incluirTodos ? 160 : 150, 32));
        combo.setBackground(Color.WHITE);
        combo.setForeground(Color.BLACK);
        combo.setFont(FUENTE_ETIQUETA);
        return combo;
    }

    private static JButton boton(String texto, Color fondo, Color colorTexto) {
        JButton boton = new JButton(texto);
        boton.setBackground(fondo);
        boton.setForeground(colorTexto);
        boton.setFont(FUENTE_ETIQUETA);
        return boton;
    }

}
