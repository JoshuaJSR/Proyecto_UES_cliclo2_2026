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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
    private JTextField txtItem;
    private JTextField txtNombre;
    private JTextField txtPrecio;
    private JTextField txtCantidad;
    private JTextField txtTotal;
    private JTextField txtTotalVenta;
    private JTextField txtNumeroVenta;
    private JComboBox<String> comboMetodos;
    private JButton btnAgregar;
    private JButton btnGuardar;
    private TableVentasModel modeloTabla;
    private JTable tablaVentas;

    // variable para controlar si estamos editando una fila existente (-1 = nuevo)
    private int indiceFilaSeleccionada = -1;

    // numero de venta que se asignara a los items de la venta que se esta registrando
    private int numeroVentaActual = 1;

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
        this.numeroVentaActual = dao != null ? dao.siguienteNumeroVenta() : 1;

        setTitle("Ingreso de ventas");
        setSize(1080, 740);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        add(crearContenido());
        conectarEventos();
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                txtNombre.requestFocusInWindow();
            }
        });
    }

    private JPanel crearContenido() {
        JPanel panel = crearPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 18, 16, 18));

        JLabel lblTitulo = new JLabel("PUNTO DE VENTA", JLabel.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblTitulo.setForeground(Color.WHITE);

        txtItem = campo();
        txtItem.setEditable(false);
        txtItem.setText("1");
        txtNombre = campo();
        btnAgregar = boton("AGREGAR PRODUCTO", COLOR_VERDE, Color.BLACK);

        modeloTabla = new TableVentasModel(ventasPendientes);
        tablaVentas = new JTable(modeloTabla);
        tablaVentas.setPreferredScrollableViewportSize(new Dimension(450, 120));
        
        JPanel panelNombre = crearPanel(new GridBagLayout());
        GridBagConstraints gbcNombre = new GridBagConstraints();
        gbcNombre.insets = new Insets(0, 0, 0, 8);
        gbcNombre.fill = GridBagConstraints.HORIZONTAL;

        txtItem.setPreferredSize(new Dimension(60, 30));
        gbcNombre.gridx = 0;
        gbcNombre.weightx = 0.0;
        panelNombre.add(txtItem, gbcNombre);

        gbcNombre.gridx = 1;
        gbcNombre.weightx = 1.0;
        gbcNombre.insets = new Insets(0, 0, 0, 0);
        panelNombre.add(txtNombre, gbcNombre);

        agregar(panel, lblTitulo, 0, 0, new Insets(0, 0, 10, 0));
        agregar(panel, etiqueta("Nombre del producto"), 1, 0, MARGEN);
        agregar(panel, panelNombre, 2, 0, MARGEN);
        agregar(panel, crearPanelCamposMedios(), 3, 0, MARGEN);
        agregar(panel, btnAgregar, 4, 0, new Insets(10, 0, 10, 0));
        agregar(panel, new JScrollPane(tablaVentas), 5, 1, SIN_MARGEN);
        agregar(panel, crearPanelNumeroVenta(), 6, 0, new Insets(8, 0, 0, 0));
        agregar(panel, crearPanelTotal(), 7, 0, new Insets(4, 0, 8, 0));
        agregar(panel, crearPanelPago(), 8, 0, SIN_MARGEN);
        agregar(panel, crearPanelBotones(), 9, 0, SIN_MARGEN);
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

    private JPanel crearPanelNumeroVenta() {
        JPanel panel = crearPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        txtNumeroVenta = campo();
        txtNumeroVenta.setPreferredSize(new Dimension(100, 35));
        txtNumeroVenta.setEditable(false);
        txtNumeroVenta.setText(String.valueOf(numeroVentaActual));
        panel.add(etiquetaDestacada("NÚMERO DE VENTA:"));
        panel.add(txtNumeroVenta);
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
        btnAgregar.addActionListener(e -> agregarOActualizarProducto());

        // listener para cuando se selecciona una fila de la tabla y pasar los datos a los txt
        tablaVentas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaVentas.getSelectedRow() != -1) {
                int fila = tablaVentas.getSelectedRow();
                indiceFilaSeleccionada = fila;
                
                // recuperamos la venta desde el modelo de la tabla
                Venta v = modeloTabla.getVentaAt(fila);
                
                txtItem.setText(String.valueOf(fila + 1));
                txtNombre.setText(v.getNombre());
                txtPrecio.setText(v.getPrecio().toPlainString());
                txtCantidad.setText(String.valueOf(v.getCantidad()));
                
                // cambiamos texto del boton para indicar que estamos actualizando
                btnAgregar.setText("ACTUALIZAR PRODUCTO");
            }
        });
    }

    // metodo unificado para agregar nuevo o actualizar el seleccionado
    private void agregarOActualizarProducto() {
        String nombre = txtNombre.getText().trim();
        String precioStr = txtPrecio.getText().trim();
        String cantidadStr = txtCantidad.getText().trim();

        if (nombre.isEmpty() || precioStr.isEmpty() || cantidadStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, complete todos los campos.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            BigDecimal precio = new BigDecimal(precioStr);
            int cantidad = Integer.parseInt(cantidadStr);

            if (indiceFilaSeleccionada == -1) {
                // agregamos nuevo registro si no hay ninguno seleccionado
                modeloTabla.agregar(new Venta(nombre, precio, cantidad));
            } else {
                // actualizamos el registro existente en la posicion seleccionada
                modeloTabla.actualizar(indiceFilaSeleccionada, new Venta(nombre, precio, cantidad));
                indiceFilaSeleccionada = -1;
                btnAgregar.setText("AGREGAR PRODUCTO");
                tablaVentas.clearSelection();
            }

            txtNombre.setText("");
            txtPrecio.setText("");
            txtCantidad.setText("");
            txtTotal.setText("");
            
            txtItem.setText(String.valueOf(ventasPendientes.size() + 1));
            
            txtNombre.requestFocusInWindow();
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

        int numeroRegistrado = numeroVentaActual;
        String metodoNombre = (String) comboMetodos.getSelectedItem();
        IMetodoPago metodoPago = pedirMetodoPago(totalVenta());

        EstadoPago estado;
        Transaccion transaccion = null;
        if (metodoPago == null) {
            //si no se capturaron los datos del pago,la venta puede guardarse como pendiente
            int opcion = JOptionPane.showConfirmDialog(this,
                    "No se completaron los datos del método de pago.\n¿Desea guardar la venta N° "
                            + numeroRegistrado + " con estado PENDIENTE?",
                    "Venta pendiente", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (opcion != JOptionPane.YES_OPTION) {
                return;
            }
            estado = EstadoPago.PENDIENTE;
        } else {
            Venta resumen = new Venta("Venta de " + ventasPendientes.size() + " producto(s)", totalVenta(), 1);
            resumen.setId(numeroRegistrado);
            transaccion = metodoPago.procesar(resumen);
            estado = transaccion.getEstado();
        }

        if (dao == null) {
            JOptionPane.showMessageDialog(this, "No hay conexión con la base de datos: la venta no se guardó.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // la venta se guarda siempre, con el estado que resulto de la validacion:
        // PROCESADO, RECHAZADO (datos invalidos) o PENDIENTE (datos no capturados)
        for (Venta item : ventasPendientes) {
            item.setId(numeroRegistrado);
            dao.insertar(item, estado, metodoNombre, usuario);
        }
        numeroVentaActual = dao.siguienteNumeroVenta();
        txtNumeroVenta.setText(String.valueOf(numeroVentaActual));

        if (estado == EstadoPago.PROCESADO && metodoPago != null && transaccion != null) {
            JOptionPane.showMessageDialog(this, metodoPago.comprobante(transaccion), "Venta registrada", JOptionPane.INFORMATION_MESSAGE);
        } else {
            String motivo = metodoPago != null ? metodoPago.getMensajeError() : null;
            JOptionPane.showMessageDialog(this,
                    "La venta N° " + numeroRegistrado + " se guardó con estado " + estado + ".\n"
                            + (motivo != null ? motivo : "Pago no confirmado."),
                    "Venta registrada con estado " + estado, JOptionPane.WARNING_MESSAGE);
        }
        modeloTabla.limpiar();
        indiceFilaSeleccionada = -1;
        btnAgregar.setText("AGREGAR PRODUCTO");
        txtItem.setText("1");
        txtTotalVenta.setText("$0.00");
        
        txtNombre.requestFocusInWindow();
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