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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private JTextField txtNumeroVenta;
    private JButton btnBuscar;
    private DefaultTableModel modeloTabla;
    private JTable tablaTransaccion;
    private JComboBox<String> comboMetodos;
    private JButton btnProcesar;
    private JLabel lblEstado;
    private JLabel lblTotal;
    private JLabel lblResumen;

    private List<Document> itemsVenta = new ArrayList<>();
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
        setSize(1120, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        add(crearContenido());
        conectarEventos();

        if (numeroVenta != null) {
            txtNumeroVenta.setText(String.valueOf(numeroVenta));
            buscar();
        }
    }

    private JPanel crearContenido() {
        JPanel panel = crearPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 18, 16, 18));

        JLabel lblTitulo = new JLabel("PAGO DE VENTAS PENDIENTES", JLabel.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblTitulo.setForeground(Color.WHITE);

        agregar(panel, lblTitulo, 0, 0, new Insets(0, 0, 10, 0));
        agregar(panel, crearPanelBusqueda(), 1, 0, MARGEN);
        agregar(panel, crearPanelDatosVenta(), 2, 0, new Insets(8, 0, 6, 0));
        agregar(panel, crearPanelTabla(), 3, 1, SIN_MARGEN);
        agregar(panel, crearPanelPago(), 4, 0, new Insets(8, 0, 4, 0));
        agregar(panel, crearPanelResumen(), 5, 0, SIN_MARGEN);
        agregar(panel, crearPanelBotones(), 6, 0, new Insets(8, 0, 0, 0));
        return panel;
    }

    private JPanel crearPanelBusqueda() {
        JPanel panel = crearPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));

        txtNumeroVenta = campo();
        txtNumeroVenta.setPreferredSize(new Dimension(110, 32));
        btnBuscar = boton("BUSCAR VENTA", COLOR_VERDE, Color.BLACK);

        JLabel lblAyuda = new JLabel("Se pueden cobrar ventas PENDIENTES o RECHAZADAS; las PROCESADAS ya están pagadas.");
        lblAyuda.setForeground(COLOR_TEXTO_SECUNDARIO);
        lblAyuda.setFont(FUENTE_ETIQUETA);

        panel.add(etiqueta("N° DE VENTA:"));
        panel.add(txtNumeroVenta);
        panel.add(btnBuscar);
        panel.add(lblAyuda);
        return panel;
    }

    private JPanel crearPanelDatosVenta() {
        JPanel panel = crearPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
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

        int[] anchos = {90, 180, 140, 120, 180};
        for (int i = 0; i < anchos.length; i++) {
            tablaTransaccion.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        JPanel panel = crearPanel(new BorderLayout(0, 4));
        panel.add(new JScrollPane(tablaTransaccion), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelPago() {
        JPanel panel = crearPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));

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
        JPanel panel = crearPanel(new BorderLayout());
        lblResumen = new JLabel("Busque una venta por su número para ver su detalle.");
        lblResumen.setForeground(COLOR_TEXTO_SECUNDARIO);
        lblResumen.setFont(FUENTE_ETIQUETA);
        panel.add(lblResumen, BorderLayout.WEST);
        return panel;
    }

    private JPanel crearPanelBotones() {
        JPanel panel = crearPanel(new BorderLayout());
        JButton btnCerrar = boton("Cerrar", COLOR_ROJO, Color.WHITE);
        btnCerrar.addActionListener(e -> dispose());
        panel.add(btnCerrar, BorderLayout.EAST);
        return panel;
    }

    private void conectarEventos() {
        btnBuscar.addActionListener(e -> buscar());
        txtNumeroVenta.addActionListener(e -> buscar());
        btnProcesar.addActionListener(e -> procesarPago());
    }

    private void buscar() {
        if (dao == null) {
            JOptionPane.showMessageDialog(this, "No hay conexión con la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String texto = txtNumeroVenta.getText().trim();
        if (texto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el número de la venta que desea pagar.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int numero;
        try {
            numero = Integer.parseInt(texto);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El número de venta debe ser numérico.",
                    "Error de formato", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            mostrarVenta(numero, dao.listar(null, numero, null, null, null, null));
        } catch (MongoException ex) {
            JOptionPane.showMessageDialog(this, "Error al consultar la base de datos: " + ex.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
        }
    }

    // carga en pantalla el detalle de la venta y habilita el cobro solo si esta PENDIENTE
    private void mostrarVenta(int numero, List<Document> items) {
        itemsVenta = items;
        numeroVentaCargada = -1;
        modeloTabla.setRowCount(0);

        if (items.isEmpty()) {
            lblEstado.setText("-");
            lblEstado.setForeground(Color.WHITE);
            lblTotal.setText("$0.00");
            lblResumen.setText("No existe una venta con el número " + numero + ".");
            btnProcesar.setEnabled(false);
            return;
        }

        String estado = items.get(0).getString("estado");
        BigDecimal total = BigDecimal.ZERO;
        for (Document item : items) {
            total = total.add(Venta.fromDocument(item).getTotal());
        }

        Document transaccion = transaccionDAO == null ? null : transaccionDAO.buscarPorVenta(numero);
        String metodo = transaccion == null ? "-" : transaccion.getString("metodo");
        Object montoObj = transaccion == null ? total : transaccion.get("monto");
        BigDecimal monto = montoObj instanceof BigDecimal ? (BigDecimal) montoObj
                : montoObj instanceof org.bson.types.Decimal128 ? ((org.bson.types.Decimal128) montoObj).bigDecimalValue()
                : total;
        java.util.Date fecha = transaccion == null ? null : transaccion.getDate("fecha");
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");

        modeloTabla.addRow(new Object[] {
            numero,
            metodo == null ? "-" : metodo,
            "$" + MetodoPago.formatear(monto),
            estado == null ? "-" : estado,
            fecha == null ? "" : fmt.format(fecha)
        });

        numeroVentaCargada = numero;
        lblEstado.setText(estado == null ? "-" : estado);
        lblEstado.setForeground(colorEstado(estado));
        lblTotal.setText("$" + MetodoPago.formatear(total));

        if (EstadoPago.PROCESADO.name().equals(estado)) {
            btnProcesar.setEnabled(false);
            lblResumen.setText("La venta N° " + numero + " ya fue pagada: no se puede cobrar de nuevo.");
        } else if (EstadoPago.PENDIENTE.name().equals(estado)) {
            btnProcesar.setEnabled(true);
            lblResumen.setText("Venta N° " + numero
                    + " pendiente de pago. Seleccione el método y procese el cobro.");
        } else if (EstadoPago.RECHAZADO.name().equals(estado)) {
            btnProcesar.setEnabled(true);
            lblResumen.setText("El último cobro de la venta N° " + numero
                    + " fue rechazado.");
        } else {
            btnProcesar.setEnabled(false);
            lblResumen.setText("La venta N° " + numero + " está " + estado + ": no se puede cobrar.");
        }
    }

    private void procesarPago() {
        if (dao == null || transaccionDAO == null) {
            JOptionPane.showMessageDialog(this, "No hay conexión con la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
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
            // el estado se vuelve a leer, evita cobrar dos veces la misma venta
            items = dao.listar(null, numero, null, null, null, null);
        } catch (MongoException ex) {
            JOptionPane.showMessageDialog(this, "Error al consultar la base de datos: " + ex.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (items.isEmpty()) {
            mostrarVenta(numero, items);
            JOptionPane.showMessageDialog(this, "La venta N° " + numero + " ya no existe en la base de datos.",
                    "Venta no encontrada", JOptionPane.WARNING_MESSAGE);
            return;
        }
        for (Document item : items) {
            String estado = item.getString("estado");
            // solo se bloquea una venta ya cobrada; PENDIENTE y RECHAZADO se pueden pagar
            if (EstadoPago.PROCESADO.name().equals(estado)) {
                mostrarVenta(numero, items);
                JOptionPane.showMessageDialog(this,
                        "La venta N° " + numero + " ya fue pagada, cobrara de nuevo.",
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

        // la venta toma el resultado del cobro, PROCESADO si se pago, RECHAZADO si hubo error
        EstadoPago resultado = transaccion.getEstado();

        try {
            // se reutiliza la transaccion creada al registrar la venta, una sola por venta
            Document registrada = transaccionDAO.buscarPorVenta(numero);
            if (registrada != null) {
                transaccionDAO.actualizar(registrada.getObjectId("_id"), resultado, metodoNombre);
            } else {
                transaccionDAO.insertar(transaccion);
            }

            if (!dao.cambiarEstado(numero, resultado, metodoNombre)) {
                JOptionPane.showMessageDialog(this,
                        "La transacción se registró, pero la venta no pudo actualizarse a " + resultado + ".",
                        "Error al actualizar", JOptionPane.ERROR_MESSAGE);
                return;
            }

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
            return;
        }

        mostrarVenta(numero, recargar(numero));
    }

    private List<Document> recargar(int numero) {
        try {
            return dao.listar(null, numero, null, null, null, null);
        } catch (MongoException ex) {
            return new ArrayList<>();
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
