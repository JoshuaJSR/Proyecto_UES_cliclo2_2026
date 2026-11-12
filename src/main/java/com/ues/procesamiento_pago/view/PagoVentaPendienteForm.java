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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;

import org.bson.Document;
import org.bson.types.Decimal128;

import com.mongodb.MongoException;
import com.ues.procesamiento_pago.Model.EstadoPago;
import com.ues.procesamiento_pago.Model.Transaccion;
import com.ues.procesamiento_pago.Model.Venta;
import com.ues.procesamiento_pago.Strategy.IMetodoPago;
import com.ues.procesamiento_pago.Strategy.MetodoPago;
import com.ues.procesamiento_pago.config.MongoDBConnection;
import com.ues.procesamiento_pago.dao.TransaccionDAO;
import com.ues.procesamiento_pago.dao.VentaDAO;
import com.ues.procesamiento_pago.util.Estilos;

public class PagoVentaPendienteForm extends JFrame {
    private static final String[] COLUMNAS = {"N° VENTA", "MÉTODO DE PAGO", "MONTO", "ESTADO", "FECHA"};

    private static final String[] METODOS = {
        "Efectivo", "Pago con tarjeta", "Pago Electronico", "Transferencia Bancaria"
    };

    private static final Map<String, String[]> DATOS_POR_METODO = Map.of(
        "Pago con tarjeta", new String[]{"Número de la tarjeta:", "Nombre del titular:"},
        "Pago Electronico", new String[]{"Correo de la cuenta:", "Plataforma de pago:"},
        "Transferencia Bancaria", new String[]{"Número de cuenta origen:", "Número de cuenta destino:"}
    );

    private JTextField txtFiltroId;
    private JFormattedTextField txtFechaDesde;
    private JFormattedTextField txtFechaHasta;
    private JButton btnFiltrar;
    private JButton btnLimpiar;
    private DefaultTableModel modeloTabla;
    private JTable tablaTransaccion;
    private JComboBox<String> comboMetodos;
    private JButton btnProcesar;
    private JLabel lblEstado;
    private JLabel lblTotal;
    private JLabel lblInfo;
    private JLabel lblResumen;

    private final Map<Integer, List<Document>> itemsPorVenta = new LinkedHashMap<>();
    private final List<Document> itemsVenta = new ArrayList<>();
    private int numeroVentaCargada = -1;

    private final VentaDAO dao;
    private final TransaccionDAO transaccionDAO;

    private static final Color COLOR_FONDO = Color.DARK_GRAY;
    private static final Color COLOR_TEXTO_SECUNDARIO = new Color(160, 160, 160);
    private static final Color COLOR_VERDE = new Color(60, 220, 130);
    private static final Color COLOR_ROJO = new Color(220, 60, 60);
    private static final Font FUENTE_ETIQUETA = new Font("SansSerif", Font.BOLD, 12);
    private static final Font FUENTE_DESTACADA = new Font("SansSerif", Font.BOLD, 14);
    private static final Insets MARGEN = new Insets(3, 0, 3, 0);
    private static final Insets SIN_MARGEN = new Insets(0, 0, 0, 0);

    public PagoVentaPendienteForm(MongoDBConnection mongoDB) {
        this(mongoDB, null);
    }

    public PagoVentaPendienteForm(MongoDBConnection mongoDB, Integer numeroVenta) {
        if (mongoDB != null && mongoDB.getDatabase() != null) {
            this.dao = new VentaDAO(mongoDB);
        } else {
            this.dao = null;
            JOptionPane.showMessageDialog(null, "No hay una conexión activa a MongoDB.");
        }
        this.transaccionDAO = dao != null ? new TransaccionDAO(mongoDB) : null;

        setTitle("Pago de ventas pendientes");
        setSize(1120, 740);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        add(crearContenido());
        conectarEventos();

        if (numeroVenta != null) {
            txtFiltroId.setText(String.valueOf(numeroVenta));
            aplicarFiltro();
        } else {
            cargarListaInicial();
        }
    }

    private void cargarListaInicial() {
        if (dao != null) {
            aplicarFiltro();
        }
    }

    private JPanel crearContenido() {
        JPanel panel = crearPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 18, 16, 18));

        JLabel lblTitulo = new JLabel("PAGO DE VENTAS PENDIENTES", JLabel.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblTitulo.setForeground(Color.WHITE);

        agregar(panel, lblTitulo, 0, 0, new Insets(0, 0, 10, 0));
        agregar(panel, crearPanelFiltros(), 1, 0, MARGEN);
        agregar(panel, crearPanelDatosVenta(), 2, 0, new Insets(8, 0, 6, 0));
        agregar(panel, crearPanelTabla(), 3, 1, SIN_MARGEN);
        agregar(panel, crearPanelPago(), 4, 0, new Insets(8, 0, 4, 0));
        agregar(panel, crearPanelResumen(), 5, 0, SIN_MARGEN);
        agregar(panel, crearPanelBotones(), 6, 0, new Insets(8, 0, 0, 0));
        return panel;
    }

    private JPanel crearPanelFiltros() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        barra.setBackground(COLOR_FONDO);

        txtFiltroId = campo();
        txtFiltroId.setPreferredSize(new Dimension(90, 32));
        
        try {
            // Máscara estricta para dd/MM/yyyy
            MaskFormatter mascaraFecha = new MaskFormatter("##/##/####");
            mascaraFecha.setPlaceholderCharacter('_'); 

            txtFechaDesde = new JFormattedTextField(mascaraFecha);
            txtFechaHasta = new JFormattedTextField(mascaraFecha);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String fechaActual = sdf.format(new Date());

            txtFechaDesde.setText(fechaActual);
            txtFechaHasta.setText(fechaActual);

            txtFechaDesde.setPreferredSize(new Dimension(130, 32));
            txtFechaHasta.setPreferredSize(new Dimension(130, 32));
        } catch (ParseException e) {
            txtFechaDesde = new JFormattedTextField();
            txtFechaHasta = new JFormattedTextField();
        }

        btnFiltrar = boton("FILTRAR", COLOR_VERDE, Color.BLACK);
        btnLimpiar = boton("LIMPIAR", COLOR_ROJO, Color.WHITE);
        btnLimpiar.setPreferredSize(new Dimension(110, 32));

        barra.add(etiqueta("N° VENTA:"));
        barra.add(txtFiltroId);
        barra.add(etiqueta("Desde:"));
        barra.add(txtFechaDesde);
        barra.add(etiqueta("Hasta:"));
        barra.add(txtFechaHasta);
        barra.add(btnFiltrar);
        barra.add(btnLimpiar);

        JLabel lblHint = new JLabel(
            "Listado de ventas PENDIENTES o RECHAZADAS. Filtre por número de venta y/o rango de fecha.");
        lblHint.setForeground(COLOR_TEXTO_SECUNDARIO);
        lblHint.setFont(FUENTE_ETIQUETA);

        JPanel section = new JPanel(new BorderLayout(0, 4));
        section.setBackground(COLOR_FONDO);
        section.add(barra, BorderLayout.CENTER);
        section.add(lblHint, BorderLayout.SOUTH);
        return section;
    }

    private JPanel crearPanelDatosVenta() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        panel.setBackground(COLOR_FONDO);

        lblEstado = new JLabel("-");
        lblEstado.setForeground(Color.WHITE);
        lblEstado.setFont(FUENTE_DESTACADA);
        lblTotal = new JLabel("$0.00");
        lblTotal.setForeground(COLOR_VERDE);
        lblTotal.setFont(FUENTE_DESTACADA);

        panel.add(etiquetaDestacada("ESTADO:"));
        panel.add(lblEstado);
        panel.add(javax.swing.Box.createHorizontalStrut(20));
        panel.add(etiquetaDestacada("TOTAL A PAGAR:"));
        panel.add(lblTotal);

        lblInfo = new JLabel("-");
        lblInfo.setForeground(COLOR_TEXTO_SECUNDARIO);
        lblInfo.setFont(FUENTE_ETIQUETA);
        panel.add(javax.swing.Box.createHorizontalStrut(20));
        panel.add(lblInfo);

        return panel;
    }

    private JPanel crearPanelTabla() {
        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaTransaccion = new JTable(modeloTabla);
        tablaTransaccion.setBackground(new Color(25, 25, 25));
        tablaTransaccion.setForeground(Color.WHITE);
        tablaTransaccion.setGridColor(new Color(60, 60, 60));
        tablaTransaccion.setSelectionBackground(new Color(45, 120, 85));
        tablaTransaccion.setSelectionForeground(Color.WHITE);
        tablaTransaccion.setRowHeight(24);
        tablaTransaccion.getTableHeader().setBackground(Color.BLACK);
        tablaTransaccion.getTableHeader().setForeground(Color.WHITE);
        tablaTransaccion.getTableHeader().setFont(FUENTE_ETIQUETA);
        tablaTransaccion.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int[] anchos = {90, 180, 140, 120, 180};
        for (int i = 0; i < anchos.length; i++) {
            tablaTransaccion.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        JPanel panel = crearPanel(new BorderLayout(0, 4));
        panel.add(new JScrollPane(tablaTransaccion), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelPago() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        panel.setBackground(COLOR_FONDO);

        comboMetodos = new JComboBox<>(METODOS);
        comboMetodos.setPreferredSize(new Dimension(200, 30));
        comboMetodos.setBackground(Color.WHITE);
        comboMetodos.setForeground(Color.BLACK);
        comboMetodos.setFont(FUENTE_ETIQUETA);

        btnProcesar = boton("PROCESAR PAGO", COLOR_VERDE, Color.BLACK);
        btnProcesar.setEnabled(false);

        panel.add(etiquetaDestacada("MÉTODO DE PAGO:"));
        panel.add(comboMetodos);
        panel.add(btnProcesar);
        return panel;
    }

    private JPanel crearPanelResumen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_FONDO);
        lblResumen = new JLabel("Seleccione una venta de la lista para ver su detalle.");
        lblResumen.setForeground(COLOR_TEXTO_SECUNDARIO);
        lblResumen.setFont(FUENTE_ETIQUETA);
        panel.add(lblResumen, BorderLayout.WEST);
        return panel;
    }

    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_FONDO);
        JButton btnCerrar = boton("Cerrar", COLOR_ROJO, Color.WHITE);
        btnCerrar.addActionListener(e -> dispose());
        panel.add(btnCerrar, BorderLayout.EAST);
        return panel;
    }

    private void conectarEventos() {
        btnFiltrar.addActionListener(e -> aplicarFiltro());

        btnLimpiar.addActionListener(e -> {
            txtFiltroId.setText("");
            SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
            String fechaActual = sdf.format(new Date());
            txtFechaDesde.setText(fechaActual);
            txtFechaHasta.setText(fechaActual);
            aplicarFiltro();
        });
        btnProcesar.addActionListener(e -> procesarPago());

        txtFiltroId.addActionListener(e -> aplicarFiltro());
        txtFechaDesde.addActionListener(e -> aplicarFiltro());
        txtFechaHasta.addActionListener(e -> aplicarFiltro());

        tablaTransaccion.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int fila = tablaTransaccion.getSelectedRow();
            if (fila < 0) {
                limpiarSeleccion();
            } else {
                int numero = (int) modeloTabla.getValueAt(fila, 0);
                mostrarSeleccion(numero);
            }
        });
    }

    private void aplicarFiltro() {
        if (dao == null) {
            JOptionPane.showMessageDialog(this, "No hay conexión con la base de datos.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String idTexto = txtFiltroId.getText().trim();
        if (!idTexto.isEmpty()) {
            try {
                Integer.valueOf(idTexto);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "El número de venta debe ser numérico.",
                        "Error de formato", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        Date desde = parsearFecha(txtFechaDesde.getText().trim());
        if (txtFechaDesde.getText().trim().isEmpty()) {
            desde = null;
        } else if (desde == null) {
            JOptionPane.showMessageDialog(this, "Formato de fecha 'Desde' inválido. Use dd/MM/yyyy.",
                    "Error de formato", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Date hasta = parsearFecha(txtFechaHasta.getText().trim());
        if (txtFechaHasta.getText().trim().isEmpty()) {
            hasta = null;
        } else if (hasta == null) {
            JOptionPane.showMessageDialog(this, "Formato de fecha 'Hasta' inválido. Use dd/MM/yyyy.",
                    "Error de formato", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (hasta != null) {
            hasta = hastaFinDeDia(hasta);
        }

        Integer idFiltro = idTexto.isEmpty() ? null : Integer.valueOf(idTexto);

        try {
            listarPendientesYRechazados(idFiltro, desde, hasta);
        } catch (MongoException ex) {
            JOptionPane.showMessageDialog(this, "Error al consultar la base de datos: " + ex.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void listarPendientesYRechazados(Integer idFiltro, Date desde, Date hasta) {
        List<Document> todos = new ArrayList<>();
        for (String estado : new String[]{EstadoPago.PENDIENTE.name(), EstadoPago.RECHAZADO.name()}) {
            todos.addAll(dao.listar(null, idFiltro, estado, null, desde, hasta));
        }

        Map<Integer, List<Document>> porId = new LinkedHashMap<>();
        for (Document item : todos) {
            Integer n = item.getInteger("id");
            if (n != null) {
                porId.computeIfAbsent(n, k -> new ArrayList<>()).add(item);
            }
        }
        itemsPorVenta.clear();
        itemsPorVenta.putAll(porId);

        List<Integer> ids = new ArrayList<>(porId.keySet());
        Map<Integer, Document> ultimas = transaccionDAO.ultimaTransaccionPorVenta(ids);

        modeloTabla.setRowCount(0);
        BigDecimal granTotal = BigDecimal.ZERO;

        for (Integer n : ids) {
            List<Document> items = porId.get(n);
            BigDecimal total = totalVenta(items);
            Document ult = ultimas.get(n);

            String metodo = ult != null ? ult.getString("metodo") : "-";
            Object montoObj = ult != null ? ult.get("monto") : null;
            BigDecimal monto;
            if (montoObj == null) {
                monto = total;
            } else {
                switch (montoObj) {
                    case BigDecimal decimal -> monto = decimal;
                    case Decimal128 decimal128 -> monto = decimal128.bigDecimalValue();
                    default -> monto = total;
                }
            }

            Date fecha = ult != null ? ult.getDate("fecha") : null;
            String estado = items.get(0).getString("estado");
            SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            modeloTabla.addRow(new Object[]{
                n,
                metodo,
                "$" + MetodoPago.formatear(monto),
                estado == null ? "-" : estado,
                fecha == null ? "" : fmt.format(fecha)
            });

            granTotal = granTotal.add(total);
        }

        int cuenta = ids.size();
        lblInfo.setText(cuenta + " venta(s) | Total: $" + MetodoPago.formatear(granTotal));

        Integer seleccionado = numeroVentaCargada;
        if (ids.contains(seleccionado)) {
            seleccionarFila(seleccionado);
            mostrarSeleccion(seleccionado);
        } else if (idFiltro != null && ids.size() == 1) {
            Integer unico = ids.get(0);
            seleccionarFila(unico);
            mostrarSeleccion(unico);
        } else {
            limpiarSeleccion();
        }
    }

    private void seleccionarFila(int numero) {
        for (int r = 0; r < modeloTabla.getRowCount(); r++) {
            Object valor = modeloTabla.getValueAt(r, 0);
            if (valor instanceof Integer && ((Integer) valor) == numero) {
                tablaTransaccion.setRowSelectionInterval(r, r);
                tablaTransaccion.scrollRectToVisible(tablaTransaccion.getCellRect(r, 0, true));
                break;
            }
        }
    }

    private void mostrarSeleccion(int numero) {
        List<Document> items = itemsPorVenta.get(numero);
        if (items == null || items.isEmpty()) {
            limpiarSeleccion();
            return;
        }

        itemsVenta.clear();
        itemsVenta.addAll(items);
        numeroVentaCargada = numero;

        String estado = items.get(0).getString("estado");
        BigDecimal total = totalVenta(items);

        lblEstado.setText(estado == null ? "-" : estado);
        lblEstado.setForeground(colorEstado(estado));
        lblTotal.setText("$" + MetodoPago.formatear(total));

        if (EstadoPago.PROCESADO.name().equals(estado)) {
            btnProcesar.setEnabled(false);
            lblResumen.setText("La venta N° " + numero + " ya fue pagada: no se puede cobrar de nuevo.");
        } else if (EstadoPago.PENDIENTE.name().equals(estado)) {
            btnProcesar.setEnabled(true);
            lblResumen.setText("Venta N° " + numero + " pendiente de pago. Seleccione el método y procese el cobro.");
        } else if (EstadoPago.RECHAZADO.name().equals(estado)) {
            btnProcesar.setEnabled(true);
            lblResumen.setText("El último cobro de la venta N° " + numero + " fue rechazado.");
        } else {
            btnProcesar.setEnabled(false);
            lblResumen.setText("La venta N° " + numero + " está " + estado + ": no se puede cobrar.");
        }
    }

    private void limpiarSeleccion() {
        itemsVenta.clear();
        numeroVentaCargada = -1;
        lblEstado.setText("-");
        lblEstado.setForeground(Color.WHITE);
        lblTotal.setText("$0.00");
        btnProcesar.setEnabled(false);
        lblResumen.setText("Seleccione una venta de la lista para ver su detalle.");
    }

    private void procesarPago() {
        if (dao == null || transaccionDAO == null) {
            JOptionPane.showMessageDialog(this, "No hay conexión con la base de datos.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (numeroVentaCargada < 0 || itemsVenta.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione la venta pendiente que desea pagar.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int numero = numeroVentaCargada;
        List<Document> items;
        try {
            items = dao.listar(null, numero, null, null, null, null);
        } catch (MongoException ex) {
            JOptionPane.showMessageDialog(this, "Error al consultar la base de datos: " + ex.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (items.isEmpty()) {
            limpiarSeleccion();
            JOptionPane.showMessageDialog(this, "La venta N° " + numero + " ya no existe en la base de datos.",
                    "Venta no encontrada", JOptionPane.WARNING_MESSAGE);
            aplicarFiltro();
            return;
        }

        for (Document item : items) {
            String estado = item.getString("estado");
            if (EstadoPago.PROCESADO.name().equals(estado)) {
                mostrarSeleccion(numero);
                JOptionPane.showMessageDialog(this,
                        "La venta N° " + numero + " ya fue pagada, no podrá cobrarla de nuevo.",
                        "Pago no permitido", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        BigDecimal total = totalVenta(items);
        String metodoNombre = (String) comboMetodos.getSelectedItem();
        IMetodoPago metodoPago = pedirMetodoPago(metodoNombre, total);
        if (metodoPago == null) {
            JOptionPane.showMessageDialog(this,
                    "No se capturaron los datos del método de pago.\nLa venta N° " + numero,
                    "Pago no procesado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Venta resumen = new Venta("Venta de " + items.size() + " producto(s)", total, 1);
        resumen.setId(numero);
        Transaccion transaccion = metodoPago.procesar(resumen);
        transaccion.setMensaje(metodoPago.getMensajeError());

        EstadoPago resultado = transaccion.getEstado();

        try {
            Document registrada = transaccionDAO.buscarPorVenta(numero);
            if (registrada != null) {
                transaccionDAO.registrarResultado(registrada.getObjectId("_id"), transaccion);
            } else {
                transaccionDAO.insertar(transaccion);
            }

            if (!dao.cambiarEstado(numero, resultado, metodoNombre)) {
                JOptionPane.showMessageDialog(this,
                        "La transacción se registró, pero la venta no pudo actualizarse a " + resultado + ".",
                        "Error al actualizar", JOptionPane.ERROR_MESSAGE);
                return;
            }

            aplicarFiltro();

            if (resultado == EstadoPago.PROCESADO) {
                JOptionPane.showMessageDialog(this,
                        metodoPago.comprobante(transaccion) + "\nVenta N° " + numero + " pagada.",
                        "Pago registrado", JOptionPane.INFORMATION_MESSAGE);
            } else {
                String motivo = transaccion.getMensaje() != null ? transaccion.getMensaje() : "Pago no confirmado.";
                JOptionPane.showMessageDialog(this,
                        "El pago fue rechazado: " + motivo + "\nLa venta N° " + numero
                                + " quedó en estado " + resultado + ".\nPuede reintentar el cobro con otro método.",
                        "Pago rechazado", JOptionPane.WARNING_MESSAGE);
            }
        } catch (MongoException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar en la base de datos: " + ex.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static BigDecimal totalVenta(List<Document> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (Document item : items) {
            total = total.add(Venta.fromDocument(item).getTotal());
        }
        return total;
    }

    private IMetodoPago pedirMetodoPago(String metodo, BigDecimal total) {
        if (metodo == null) {
            return null;
        }
        if ("Efectivo".equals(metodo)) {
            BigDecimal recibido = pedirMonto("Monto entregado por el cliente:", total);
            return recibido == null ? null : MetodoPago.crear(metodo, recibido);
        }

        String[] preguntas = DATOS_POR_METODO.get(metodo);
        if (preguntas == null) {
            return null;
        }
        String[] datos = new String[preguntas.length];
        for (int i = 0; i < preguntas.length; i++) {
            String dato = JOptionPane.showInputDialog(this, preguntas[i]);
            if (dato == null) {
                return null;
            }
            datos[i] = dato.trim();
        }
        return MetodoPago.crear(metodo, null, datos);
    }

    private BigDecimal pedirMonto(String mensaje, BigDecimal sugerido) {
        while (true) {
            String entrada = JOptionPane.showInputDialog(this, mensaje, sugerido.toPlainString());
            if (entrada == null) {
                return null;
            }
            try {
                return new BigDecimal(entrada.trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Ingrese un monto numérico válido.",
                        "Error de formato", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static Date parsearFecha(String texto) {
    if (texto == null || texto.trim().isEmpty()) {
        return null;
    }
    String limpio = texto.replace("_", "").trim();
    if (limpio.equals("/")) { 
        return null;
    }
    try {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);
        return sdf.parse(limpio);
    } catch (ParseException e) {
        return null;
    }
}
    private static Date hastaFinDeDia(Date d) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTime(d);
        c.set(java.util.Calendar.HOUR_OF_DAY, 23);
        c.set(java.util.Calendar.MINUTE, 59);
        c.set(java.util.Calendar.SECOND, 59);
        c.set(java.util.Calendar.MILLISECOND, 999);
        return c.getTime();
    }

    private static Color colorEstado(String estado) {
        if (EstadoPago.PROCESADO.name().equals(estado)) {
            return COLOR_VERDE;
        }
        if (EstadoPago.RECHAZADO.name().equals(estado)) {
            return COLOR_ROJO;
        }
        return Color.WHITE;
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

    private static JLabel etiquetaDestacada(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(Color.WHITE);
        etiqueta.setFont(FUENTE_DESTACADA);
        return etiqueta;
    }

    private static JTextField campo() {
        JTextField campo = new JTextField();
        Estilos.estilarCampo(campo);
        return campo;
    }

    private static JButton boton(String texto, Color fondo, Color colorTexto) {
        JButton boton = new JButton(texto);
        boton.setBackground(fondo);
        boton.setForeground(colorTexto);
        boton.setFont(FUENTE_ETIQUETA);
        return boton;
    }
}
