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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.ues.procesamiento_pago.Model.EstadoPago;
import com.ues.procesamiento_pago.Model.Transaccion;
import com.ues.procesamiento_pago.Model.Venta;
import com.ues.procesamiento_pago.Strategy.IMetodoPago;
import com.ues.procesamiento_pago.Strategy.MetodoPago;
import com.ues.procesamiento_pago.config.MongoDBConnection;
import com.ues.procesamiento_pago.dao.VentaDAO;
import com.ues.procesamiento_pago.util.Estilos;
import com.ues.procesamiento_pago.view.components.TableVentasModel;

public class VentasForm extends JFrame {
    private JTextField txtNombre;
    private JTextField txtPrecio;
    private JTextField txtCantidad;
    private JTextField txtTotal;
    private JTextField txtTotalVenta;
    private JComboBox<String> comboMetodos;
    private JButton btnAgregar;
    private JButton btnGuardar;
    private TableVentasModel modeloTabla;

    private final String usuario;
    private final VentaDAO dao;
    private final List<Venta> ventasPendientes = new ArrayList<>();

    private final String[] METODOS = {
        "Efectivo", "Pago con tarjeta", "Pago Electronico", "Transferencia Bancaria"
    };

    private static final Map<String, String[]> DATOS_POR_METODO = Map.of(
        "Pago con tarjeta", new String[]{"Número de la tarjeta:", "Nombre del titular:"},
        "Pago Electronico", new String[]{"Correo de la cuenta:", "Plataforma de pago:"},
        "Transferencia Bancaria", new String[]{"Número de cuenta origen:", "Número de cuenta destino:"}
    );

    private static final Color COLOR_FONDO = Color.DARK_GRAY;
    private static final Color COLOR_TEXTO_SECUNDARIO = new Color(160, 160, 160);
    private static final Color COLOR_VERDE = new Color(60, 220, 130);
    private static final Color COLOR_ROJO = new Color(220, 60, 60);
    private static final Font FUENTE_ETIQUETA = new Font("SansSerif", Font.BOLD, 12);
    private static final Font FUENTE_DESTACADA = new Font("SansSerif", Font.BOLD, 14);
    private static final Insets MARGEN = new Insets(3, 0, 3, 0);
    private static final Insets SIN_MARGEN = new Insets(0, 0, 0, 0);

    public VentasForm(MongoDBConnection mongoDB, String usuario) {
        this.usuario = usuario;
        if (mongoDB != null && mongoDB.getDatabase() != null) {
            this.dao = new VentaDAO(mongoDB);
        } else {
            this.dao = null;
            JOptionPane.showMessageDialog(null, "No hay una conexión activa a MongoDB.");
        }

        setTitle("Ingreso de ventas");
        setSize(1080, 740);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        add(crearContenido());
        conectarEventos();
    }

    private JPanel crearContenido() {
        JPanel panel = crearPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 18, 16, 18));

        JLabel lblTitulo = new JLabel("PUNTO DE VENTA", JLabel.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblTitulo.setForeground(Color.WHITE);

        txtNombre = campo();
        btnAgregar = boton("AGREGAR PRODUCTO", COLOR_VERDE, Color.BLACK);

        modeloTabla = new TableVentasModel(ventasPendientes);
        JTable tablaVentas = new JTable(modeloTabla);
        tablaVentas.setPreferredScrollableViewportSize(new Dimension(450, 120));

        agregar(panel, lblTitulo, 0, 0, new Insets(0, 0, 10, 0));
        agregar(panel, etiqueta("Nombre del producto"), 1, 0, MARGEN);
        agregar(panel, txtNombre, 2, 0, MARGEN);
        agregar(panel, crearPanelCamposMedios(), 3, 0, MARGEN);
        agregar(panel, btnAgregar, 4, 0, new Insets(10, 0, 10, 0));
        agregar(panel, new JScrollPane(tablaVentas), 5, 1, SIN_MARGEN);
        agregar(panel, crearPanelTotal(), 6, 0, new Insets(8, 0, 8, 0));
        agregar(panel, crearPanelPago(), 7, 0, SIN_MARGEN);
        agregar(panel, crearPanelBotones(), 8, 0, SIN_MARGEN);
        return panel;
    }

    private JPanel crearPanelCamposMedios() {
        JPanel panel = crearPanel(new GridBagLayout());
        String[] titulos = {"PRECIO", "CANTIDAD", "TOTAL"};
        JTextField[] campos = {txtPrecio = campo(), txtCantidad = campo(), txtTotal = campo()};
        campos[2].setEditable(false);

        for (int i = 0; i < titulos.length; i++) {
            Insets margen = new Insets(0, 0, 0, i < titulos.length - 1 ? 10 : 0);
            panel.add(etiqueta(titulos[i]), posicion(i, 0, margen));
            panel.add(campos[i], posicion(i, 1, margen));
        }
        return panel;
    }

    private JPanel crearPanelTotal() {
        JPanel panel = crearPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        txtTotalVenta = campo();
        txtTotalVenta.setPreferredSize(new Dimension(160, 35));
        txtTotalVenta.setEditable(false);
        txtTotalVenta.setText("$0.00");
        panel.add(etiquetaDestacada("TOTAL DE LA VENTA:"));
        panel.add(txtTotalVenta);
        return panel;
    }

    private JPanel crearPanelPago() {
        JPanel panel = crearPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        comboMetodos = new JComboBox<>(METODOS);
        comboMetodos.setPreferredSize(new Dimension(200, 30));
        comboMetodos.setBackground(Color.WHITE);
        comboMetodos.setForeground(Color.BLACK);
        comboMetodos.setFont(FUENTE_ETIQUETA);
        panel.add(etiquetaDestacada("MÉTODO DE PAGO:"));
        panel.add(comboMetodos);
        return panel;
    }

    private JPanel crearPanelBotones() {
        JPanel panel = crearPanel(new BorderLayout());
        btnGuardar = boton("Registrar Venta", COLOR_VERDE, Color.BLACK);
        JButton btnCancelar = boton("Cancelar", COLOR_ROJO, Color.WHITE);
        btnCancelar.addActionListener(e -> dispose());
        panel.add(btnGuardar, BorderLayout.WEST);
        panel.add(btnCancelar, BorderLayout.EAST);
        return panel;
    }

    private void conectarEventos() {
        DocumentListener listenerCalculo = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { actualizarTotal(); }
            @Override
            public void removeUpdate(DocumentEvent e) { actualizarTotal(); }
            @Override
            public void changedUpdate(DocumentEvent e) { actualizarTotal(); }
        };
        txtPrecio.getDocument().addDocumentListener(listenerCalculo);
        txtCantidad.getDocument().addDocumentListener(listenerCalculo);
        btnGuardar.addActionListener(e -> registrarVenta());
        btnAgregar.addActionListener(e -> agregarProducto());
    }

    private void agregarProducto() {
        String nombre = txtNombre.getText().trim();
        String precioStr = txtPrecio.getText().trim();
        String cantidadStr = txtCantidad.getText().trim();

        if (nombre.isEmpty() || precioStr.isEmpty() || cantidadStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, complete todos los campos.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            modeloTabla.agregar(new Venta(nombre, new BigDecimal(precioStr), Integer.parseInt(cantidadStr)));
            txtNombre.setText("");
            txtPrecio.setText("");
            txtCantidad.setText("");
            txtTotal.setText("");
            txtNombre.requestFocus();
            txtTotalVenta.setText("$" + MetodoPago.formatear(totalVenta()));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El precio y la cantidad deben ser valores numéricos válidos.", "Error de formato", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registrarVenta() {
        if (ventasPendientes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Agregue al menos un producto antes de registrar la venta.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        IMetodoPago metodoPago = pedirMetodoPago(totalVenta());
        if (metodoPago == null) {
            return;
        }

        Venta venta = new Venta("Venta de " + ventasPendientes.size() + " producto(s)", totalVenta(), 1);
        Transaccion transaccion = metodoPago.procesar(venta);
        if (transaccion.getEstado() != EstadoPago.PROCESADO) {
            String motivo = metodoPago.getMensajeError();
            JOptionPane.showMessageDialog(this,
                    motivo != null ? motivo : "El pago fue rechazado.",
                    "Pago rechazado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (dao != null) {
            for (Venta item : ventasPendientes) {
                dao.insertar(item, transaccion.getEstado(), transaccion.getMetodo(), usuario);
            }
        }

        JOptionPane.showMessageDialog(this, metodoPago.comprobante(transaccion), "Venta registrada", JOptionPane.INFORMATION_MESSAGE);
        modeloTabla.limpiar();
        txtTotalVenta.setText("$0.00");
    }

    private BigDecimal totalVenta() {
        BigDecimal total = BigDecimal.ZERO;
        for (Venta venta : ventasPendientes) {
            total = total.add(venta.getTotal());
        }
        return total;
    }

    private IMetodoPago pedirMetodoPago(BigDecimal total) {
        String metodo = (String) comboMetodos.getSelectedItem();
        if (metodo == null) {
            return null;
        }
        if ("Efectivo".equals(metodo)) {
            BigDecimal recibido = pedirMonto("Monto entregado por el cliente:", total);
            return recibido == null ? null : MetodoPago.crear(metodo, recibido);
        }

        String[] preguntas = DATOS_POR_METODO.get(metodo);
        String[] datos = new String[preguntas.length];
        for (int i = 0; i < preguntas.length; i++) {
            String dato = pedirTexto(preguntas[i]);
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
                JOptionPane.showMessageDialog(this, "Ingrese un monto numérico válido.", "Error de formato", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String pedirTexto(String mensaje) {
        return JOptionPane.showInputDialog(this, mensaje);
    }

    private void actualizarTotal() {
        try {
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            txtTotal.setText(cantidad < 0 ? "" : MetodoPago.formatear(new Venta("", precio, cantidad).getTotal()));
        } catch (NumberFormatException e) {
            txtTotal.setText("");
        }
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

    private static GridBagConstraints posicion(int columna, int fila, Insets margen) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = columna;
        gbc.gridy = fila;
        gbc.weightx = 0.33;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = margen;
        return gbc;
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
