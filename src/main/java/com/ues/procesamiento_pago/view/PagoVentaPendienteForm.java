package com.ues.procesamiento_pago.view;

import java.awt.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
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

import static com.ues.procesamiento_pago.util.Estilos.*;

public class PagoVentaPendienteForm extends JFrame {
    private static final String[] COLUMNAS = {"N° VENTA", "MÉTODO DE PAGO", "MONTO", "ESTADO", "FECHA"};
    private static final String[] METODOS = {"Efectivo", "Pago con tarjeta", "Pago Electronico", "Transferencia Bancaria"};
    private static final Map<String, String[]> DATOS_POR_METODO = Map.of(
        "Pago con tarjeta", new String[]{"Número de la tarjeta:", "Nombre del titular:"},
        "Pago Electronico", new String[]{"Correo de la cuenta:", "Plataforma de pago:"},
        "Transferencia Bancaria", new String[]{"Número de cuenta origen:", "Número de cuenta destino:"}
    );

    private JTextField txtFiltroId;
    private JFormattedTextField txtFechaDesde, txtFechaHasta;
    private JButton btnFiltrar, btnLimpiar, btnProcesar;
    private DefaultTableModel modeloTabla;
    private JTable tablaTransaccion;
    private JComboBox<String> comboMetodos;
    private JLabel lblEstado, lblTotal, lblInfo, lblResumen;

    private final Map<Integer, List<Document>> itemsPorVenta = new LinkedHashMap<>();
    private final List<Document> itemsVenta = new ArrayList<>();
    private int numeroVentaCargada = -1;
    private final VentaDAO dao;
    private final TransaccionDAO transaccionDAO;
    private final String usuario;

    public PagoVentaPendienteForm(MongoDBConnection mongoDB) { this(mongoDB, null, null); }

    public PagoVentaPendienteForm(MongoDBConnection mongoDB, Integer numeroVenta) { this(mongoDB, null, numeroVenta); }

    public PagoVentaPendienteForm(MongoDBConnection mongoDB, String usuario, Integer numeroVenta) {
        this.usuario = usuario;
        this.dao = (mongoDB != null && mongoDB.getDatabase() != null) ? new VentaDAO(mongoDB) : null;
        this.transaccionDAO = dao != null ? new TransaccionDAO(mongoDB) : null;
        if (dao == null) JOptionPane.showMessageDialog(null, "No hay conexión activa a MongoDB.");

        setTitle("Pago de ventas pendientes");
        setSize(1120, 740);
        setLocationRelativeTo(null);
        setDefaultDataSource();

        add(crearContenido());
        conectarEventos();

        if (numeroVenta != null) {
            txtFiltroId.setText(String.valueOf(numeroVenta));
            aplicarFiltro();
        } else if (dao != null) {
            aplicarFiltro();
        }
    }

    private void setDefaultDataSource() { setsetDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); setResizable(false); }
    private void setsetDefaultCloseOperation(int op) { setDefaultCloseOperation(op); }

    private JPanel crearContenido() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 18, 16, 18));

        JLabel lblTitulo = etiqueta("PAGO DE VENTAS PENDIENTES", true);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 26));

        agregar(panel, lblTitulo, 0, 0);
        agregar(panel, crearPanelFiltros(), 1, 0);
        agregar(panel, crearPanelDatosVenta(), 2, 0);
        agregar(panel, crearPanelTabla(), 3, 1);
        agregar(panel, crearPanelPago(), 4, 0);
        agregar(panel, crearPanelResumen(), 5, 0);
        agregar(panel, crearPanelBotones(), 6, 0);
        return panel;
    }

    private JPanel crearPanelFiltros() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        barra.setBackground(COLOR_FONDO);

        txtFiltroId = new JTextField();
        Estilos.estilarCampo(txtFiltroId, 90);

        try {
            MaskFormatter mascara = new MaskFormatter("##/##/####");
            mascara.setPlaceholderCharacter('_');
            txtFechaDesde = new JFormattedTextField(mascara);
            txtFechaHasta = new JFormattedTextField(mascara);
            String hoy = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
            txtFechaDesde.setText(hoy); txtFechaHasta.setText(hoy);
        } catch (ParseException e) {
            txtFechaDesde = new JFormattedTextField(); txtFechaHasta = new JFormattedTextField();
        }
        txtFechaDesde.setPreferredSize(new Dimension(130, 32));
        txtFechaHasta.setPreferredSize(new Dimension(130, 32));

        btnFiltrar = boton("FILTRAR", COLOR_VERDE, Color.BLACK);
        btnLimpiar = boton("LIMPIAR", COLOR_ROJO, Color.WHITE);
        btnLimpiar.setPreferredSize(new Dimension(110, 32));

        barra.add(etiqueta("N° VENTA:", false)); barra.add(txtFiltroId);
        barra.add(etiqueta("Desde:", false)); barra.add(txtFechaDesde);
        barra.add(etiqueta("Hasta:", false)); barra.add(txtFechaHasta);
        barra.add(btnFiltrar); barra.add(btnLimpiar);

        JLabel lblHint = etiqueta("Listado de ventas PENDIENTES o RECHAZADAS. Filtre por número y/o fecha.", false);
        JPanel section = new JPanel(new BorderLayout(0, 4));
        section.setBackground(COLOR_FONDO);
        section.add(barra, BorderLayout.CENTER);
        section.add(lblHint, BorderLayout.SOUTH);
        return section;
    }

    private JPanel crearPanelDatosVenta() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        panel.setBackground(COLOR_FONDO);
        lblEstado = etiqueta("-", true);
        lblTotal = etiqueta("$0.00", true);
        lblTotal.setForeground(COLOR_VERDE);
        lblInfo = etiqueta("-", false);

        panel.add(etiqueta("ESTADO:", true)); panel.add(lblEstado);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(etiqueta("TOTAL A PAGAR:", true)); panel.add(lblTotal);
        panel.add(Box.createHorizontalStrut(20)); panel.add(lblInfo);
        return panel;
    }

    private JPanel crearPanelTabla() {
        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override public boolean isCellEditable(int f, int c) { return false; }
        };
        tablaTransaccion = new JTable(modeloTabla);
        Estilos.estilarTabla(tablaTransaccion, new int[]{90, 180, 140, 120, 180});

        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBackground(COLOR_FONDO);
        panel.add(new JScrollPane(tablaTransaccion), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelPago() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        panel.setBackground(COLOR_FONDO);
        comboMetodos = new JComboBox<>(METODOS);
        comboMetodos.setPreferredSize(new Dimension(200, 30));
        comboMetodos.setBackground(Color.WHITE);
        comboMetodos.setFont(FUENTE_ETIQUETA);

        btnProcesar = boton("PROCESAR PAGO", COLOR_VERDE, Color.BLACK);
        btnProcesar.setEnabled(false);

        panel.add(etiqueta("MÉTODO DE PAGO:", true));
        panel.add(comboMetodos);
        panel.add(btnProcesar);
        return panel;
    }

    private JPanel crearPanelResumen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_FONDO);
        lblResumen = etiqueta("Seleccione una venta de la lista para ver su detalle.", false);
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
        btnLimpiar.addActionListener(e -> { txtFiltroId.setText(""); aplicarFiltro(); });
        btnProcesar.addActionListener(e -> procesarPago());
        tablaTransaccion.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaTransaccion.getSelectedRow() >= 0) {
                mostrarSeleccion((int) modeloTabla.getValueAt(tablaTransaccion.getSelectedRow(), 0));
            }
        });
    }

    private void aplicarFiltro() {
        if (dao == null) return;
        String idTxt = txtFiltroId.getText().trim();
        Integer idFiltro = idTxt.isEmpty() ? null : Integer.valueOf(idTxt);
        Date desde = parsearFecha(txtFechaDesde.getText());
        Date hasta = parsearFecha(txtFechaHasta.getText());
        if (hasta != null) {
            Calendar c = Calendar.getInstance(); c.setTime(hasta);
            c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59); c.set(Calendar.SECOND, 59);
            hasta = c.getTime();
        }

        List<Document> todos = new ArrayList<>();
        for (String est : List.of(EstadoPago.PENDIENTE.name(), EstadoPago.RECHAZADO.name())) {
            todos.addAll(dao.listar(null, idFiltro, est, null, desde, hasta));
        }

        itemsPorVenta.clear();
        for (Document d : todos) {
            Integer id = d.getInteger("id");
            if (id != null) itemsPorVenta.computeIfAbsent(id, k -> new ArrayList<>()).add(d);
        }

        List<Integer> ids = new ArrayList<>(itemsPorVenta.keySet());
        Map<Integer, Document> ultimas = transaccionDAO.ultimaTransaccionPorVenta(ids);
        modeloTabla.setRowCount(0);
        BigDecimal granTotal = BigDecimal.ZERO;

        for (int id : ids) {
            List<Document> items = itemsPorVenta.get(id);
            BigDecimal total = totalVenta(items);
            Document ult = ultimas.get(id);
            String metodo = ult != null ? ult.getString("metodo") : "-";
            Object mObj = ult != null ? ult.get("monto") : null;
            BigDecimal monto = (mObj instanceof BigDecimal d) ? d : (mObj instanceof Decimal128 d128) ? d128.bigDecimalValue() : total;
            Date fecha = ult != null ? ult.getDate("fecha") : null;
            String estado = items.get(0).getString("estado");

            modeloTabla.addRow(new Object[]{id, metodo, "$" + MetodoPago.formatear(monto), estado, fecha == null ? "" : new SimpleDateFormat("dd/MM/yyyy HH:mm").format(fecha)});
            granTotal = granTotal.add(total);
        }
        lblInfo.setText(ids.size() + " venta(s) | Total: $" + MetodoPago.formatear(granTotal));
    }

    private void mostrarSeleccion(int numero) {
        List<Document> items = itemsPorVenta.get(numero);
        if (items == null || items.isEmpty()) return;
        itemsVenta.clear(); itemsVenta.addAll(items);
        numeroVentaCargada = numero;

        String estado = items.get(0).getString("estado");
        BigDecimal total = totalVenta(items);
        lblEstado.setText(estado);
        lblEstado.setForeground(EstadoPago.PROCESADO.name().equals(estado) ? COLOR_VERDE : EstadoPago.RECHAZADO.name().equals(estado) ? COLOR_ROJO : Color.WHITE);
        lblTotal.setText("$" + MetodoPago.formatear(total));

        boolean activo = EstadoPago.PENDIENTE.name().equals(estado) || EstadoPago.RECHAZADO.name().equals(estado);
        btnProcesar.setEnabled(activo);
        lblResumen.setText("Venta N° " + numero + (activo ? " lista para procesar." : " en estado " + estado + "."));
    }

    private void procesarPago() {
        if (numeroVentaCargada < 0) return;
        List<Document> items = dao.listar(null, numeroVentaCargada, null, null, null, null);
        if (items.isEmpty()) return;

        BigDecimal total = totalVenta(items);
        String metodoNombre = (String) comboMetodos.getSelectedItem();
        
        IMetodoPago metodoPago;
        if ("Efectivo".equals(metodoNombre)) {
            String res = JOptionPane.showInputDialog(this, "Monto entregado:", total.toPlainString());
            if (res == null) return;
            metodoPago = MetodoPago.crear(metodoNombre, new BigDecimal(res.trim()));
        } else {
            String[] preguntas = DATOS_POR_METODO.get(metodoNombre);
            String[] datos = new String[preguntas.length];
            for (int i = 0; i < preguntas.length; i++) {
                datos[i] = JOptionPane.showInputDialog(this, preguntas[i]);
                if (datos[i] == null) return;
            }
            metodoPago = MetodoPago.crear(metodoNombre, null, datos);
        }

        Venta resumen = new Venta("Venta", total, 1);
        resumen.setId(numeroVentaCargada);
        Transaccion tx = metodoPago.procesar(resumen);
        tx.setMensaje(metodoPago.getMensajeError());
        if (usuario != null) {
            tx.setUsuario(usuario);
        }

        try {
            Document reg = transaccionDAO.buscarPorVenta(numeroVentaCargada);
            if (reg != null) transaccionDAO.registrarResultado(reg.getObjectId("_id"), tx);
            else transaccionDAO.insertar(tx);

            dao.cambiarEstado(numeroVentaCargada, tx.getEstado(), metodoNombre);
            aplicarFiltro();
            JOptionPane.showMessageDialog(this, tx.getEstado() == EstadoPago.PROCESADO ? "Pago exitoso." : "Pago rechazado: " + tx.getMensaje());
        } catch (MongoException ex) {
            JOptionPane.showMessageDialog(this, "Error BD: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static BigDecimal totalVenta(List<Document> items) {
        return items.stream().map(i -> Venta.fromDocument(i).getTotal()).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static Date parsearFecha(String texto) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);
            return sdf.parse(texto.replace("_", "").trim());
        } catch (ParseException | NullPointerException e) { return null; }
    }

    private static void agregar(JPanel panel, Component comp, int fila, double peso) {
        panel.add(comp, new GridBagConstraints(0, fila, 1, 1, 1.0, peso, 
            GridBagConstraints.CENTER, peso > 0 ? GridBagConstraints.BOTH : GridBagConstraints.HORIZONTAL, new Insets(3, 0, 3, 0), 0, 0));
    }
}