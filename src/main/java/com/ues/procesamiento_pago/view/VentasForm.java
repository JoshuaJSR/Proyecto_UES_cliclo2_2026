package com.ues.procesamiento_pago.view;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.mongodb.MongoException;
import com.ues.procesamiento_pago.Model.EstadoPago;
import com.ues.procesamiento_pago.Model.Transaccion;
import com.ues.procesamiento_pago.Model.Venta;
import com.ues.procesamiento_pago.Strategy.MetodoPago;
import com.ues.procesamiento_pago.config.MongoDBConnection;
import com.ues.procesamiento_pago.dao.TransaccionDAO;
import com.ues.procesamiento_pago.dao.VentaDAO;
import com.ues.procesamiento_pago.util.Estilos;
import com.ues.procesamiento_pago.view.components.TableVentasModel;

import static com.ues.procesamiento_pago.util.Estilos.*;

public class VentasForm extends JFrame {
    private JTextField txtItem, txtNombre, txtPrecio, txtCantidad, txtTotal, txtTotalVenta, txtNumeroVenta;
    private JButton btnAgregar, btnGuardar;
    private TableVentasModel modeloTabla;
    private JTable tablaVentas;

    // Controla si se está editando una fila existente (-1 = nuevo registro)
    private int indiceFilaSeleccionada = -1;
    private int numeroVentaActual = 1;

    private final String usuario;
    private final MongoDBConnection mongoDB;
    private final VentaDAO dao;
    private final TransaccionDAO transaccionDAO;
    private final List<Venta> ventasPendientes = new ArrayList<>();

    private static final String METODO_PENDIENTE = "Sin definir";
    private static final Insets MARGEN = new Insets(3, 0, 3, 0);

    public VentasForm(MongoDBConnection mongoDB, String usuario) {
        this.usuario = usuario;
        this.mongoDB = mongoDB;
        this.dao = (mongoDB != null && mongoDB.getDatabase() != null) ? new VentaDAO(mongoDB) : null;
        this.transaccionDAO = dao != null ? new TransaccionDAO(mongoDB) : null;
        this.numeroVentaActual = dao != null ? dao.siguienteNumeroVenta() : 1;

        if (dao == null) {
            JOptionPane.showMessageDialog(null, "No hay una conexión activa a MongoDB.");
        }

        setTitle("Registro de ventas");
        setSize(1080, 740);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        add(crearContenido());
        conectarEventos();
        
        // Enfocar el campo de nombre al abrir la ventana
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

        JLabel lblTitulo = etiqueta("PUNTO DE VENTA", true);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 26));

        txtItem = Estilos.crearCampo(60);
        txtItem.setEditable(false);
        txtItem.setText("1");
        txtNombre = Estilos.crearCampo(0);
        btnAgregar = boton("AGREGAR PRODUCTO", COLOR_VERDE, Color.BLACK);

        modeloTabla = new TableVentasModel(ventasPendientes);
        tablaVentas = new JTable(modeloTabla);
        tablaVentas.setPreferredScrollableViewportSize(new Dimension(450, 120));
        
        JPanel panelNombre = crearPanel(new GridBagLayout());
        panelNombre.add(txtItem, new GridBagConstraints(0, 0, 1, 1, 0.0, 0, 10, 2, new Insets(0, 0, 0, 8), 0, 0));
        panelNombre.add(txtNombre, new GridBagConstraints(1, 0, 1, 1, 1.0, 0, 10, 2, new Insets(0, 0, 0, 0), 0, 0));

        agregar(panel, lblTitulo, 0, 0, new Insets(0, 0, 10, 0));
        agregar(panel, etiqueta("Nombre del producto", false), 1, 0, MARGEN);
        agregar(panel, panelNombre, 2, 0, MARGEN);
        agregar(panel, crearPanelCamposMedios(), 3, 0, MARGEN);
        agregar(panel, btnAgregar, 4, 0, new Insets(10, 0, 10, 0));
        agregar(panel, new JScrollPane(tablaVentas), 5, 1, new Insets(0, 0, 0, 0));
        agregar(panel, crearPanelNumeroVenta(), 6, 0, new Insets(8, 0, 0, 0));
        agregar(panel, crearPanelTotal(), 7, 0, new Insets(4, 0, 8, 0));
        agregar(panel, crearPanelBotones(), 9, 0, new Insets(0, 0, 0, 0));
        return panel;
    }

    private JPanel crearPanelCamposMedios() {
        JPanel panel = crearPanel(new GridBagLayout());
        String[] titulos = {"PRECIO", "CANTIDAD", "TOTAL"};
        JTextField[] campos = {txtPrecio = Estilos.crearCampo(0), txtCantidad = Estilos.crearCampo(0), txtTotal = Estilos.crearCampo(0)};
        campos[2].setEditable(false);

        for (int i = 0; i < 3; i++) {
            Insets margen = new Insets(0, 0, 0, i < 2 ? 10 : 0);
            panel.add(etiqueta(titulos[i], false), new GridBagConstraints(i, 0, 1, 1, 0.33, 0, 10, 2, margen, 0, 0));
            panel.add(campos[i], new GridBagConstraints(i, 1, 1, 1, 0.33, 0, 10, 2, margen, 0, 0));
        }
        return panel;
    }

    private JPanel crearPanelNumeroVenta() {
        JPanel panel = crearPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        txtNumeroVenta = Estilos.crearCampo(100);
        txtNumeroVenta.setEditable(false);
        txtNumeroVenta.setText(String.valueOf(numeroVentaActual));
        panel.add(etiqueta("NÚMERO DE VENTA:", true));
        panel.add(txtNumeroVenta);
        return panel;
    }

    private JPanel crearPanelTotal() {
        JPanel panel = crearPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        txtTotalVenta = Estilos.crearCampo(160);
        txtTotalVenta.setEditable(false);
        txtTotalVenta.setText("$0.00");
        panel.add(etiqueta("TOTAL DE LA VENTA:", true));
        panel.add(txtTotalVenta);
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

        // Pasar los datos de la fila seleccionada a los campos para edición
        tablaVentas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaVentas.getSelectedRow() != -1) {
                int fila = tablaVentas.getSelectedRow();
                indiceFilaSeleccionada = fila;
                Venta v = modeloTabla.getVentaAt(fila);
                
                txtItem.setText(String.valueOf(fila + 1));
                txtNombre.setText(v.getNombre());
                txtPrecio.setText(v.getPrecio().toPlainString());
                txtCantidad.setText(String.valueOf(v.getCantidad()));
                btnAgregar.setText("ACTUALIZAR PRODUCTO");
            }
        });
    }

    private void agregarOActualizarProducto() {
        String nombre = txtNombre.getText().trim();
        String precioStr = txtPrecio.getText().trim();
        String cantidadStr = txtCantidad.getText().trim();

        if (nombre.isEmpty() || precioStr.isEmpty() || cantidadStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete todos los campos.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            BigDecimal precio = new BigDecimal(precioStr);
            int cantidad = Integer.parseInt(cantidadStr);

            if (precio.compareTo(BigDecimal.ZERO) <= 0 || cantidad <= 0) {
                JOptionPane.showMessageDialog(this, "El precio y la cantidad deben ser mayores a cero.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (indiceFilaSeleccionada == -1) {
                modeloTabla.agregar(new Venta(nombre, precio, cantidad));
            } else {
                modeloTabla.actualizar(indiceFilaSeleccionada, new Venta(nombre, precio, cantidad));
                indiceFilaSeleccionada = -1;
                btnAgregar.setText("AGREGAR PRODUCTO");
                tablaVentas.clearSelection();
            }

            // Limpieza de campos de entrada tras la acción
            txtNombre.setText("");
            txtPrecio.setText("");
            txtCantidad.setText("");
            txtTotal.setText("");
            txtItem.setText(String.valueOf(ventasPendientes.size() + 1));
            txtNombre.requestFocusInWindow();
            txtTotalVenta.setText("$" + MetodoPago.formatear(totalVenta()));
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valores numéricos inválidos.", "Error de formato", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registrarVenta() {
        if (ventasPendientes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Agregue al menos un producto.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (dao == null || transaccionDAO == null) {
            JOptionPane.showMessageDialog(this, "Sin conexión a la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int numeroRegistrado = dao.reservarNumeroVenta();
            txtNumeroVenta.setText(String.valueOf(numeroRegistrado));
            BigDecimal totalRegistrado = totalVenta();

            Transaccion transaccion = new Transaccion(numeroRegistrado, METODO_PENDIENTE, totalRegistrado, EstadoPago.PENDIENTE);
            for (Venta item : ventasPendientes) {
                item.setId(numeroRegistrado);
                dao.insertar(item, EstadoPago.PENDIENTE, METODO_PENDIENTE, usuario);
            }
            transaccionDAO.insertar(transaccion);

            numeroVentaActual = dao.siguienteNumeroVenta();
            txtNumeroVenta.setText(String.valueOf(numeroVentaActual));

            int opcion = JOptionPane.showConfirmDialog(this,
                    "Venta N° " + numeroRegistrado + " registrada. ¿Desea realizar el pago?",
                    "Venta registrada", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

            // Reiniciar formulario para una nueva venta
            modeloTabla.limpiar();
            indiceFilaSeleccionada = -1;
            btnAgregar.setText("AGREGAR PRODUCTO");
            txtItem.setText("1");
            txtTotalVenta.setText("$0.00");
            txtNombre.requestFocusInWindow();

            if (opcion == JOptionPane.YES_OPTION && mongoDB != null) {
                new PagoVentaPendienteForm(mongoDB, numeroRegistrado).setVisible(true);
            }
        } catch (MongoException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private BigDecimal totalVenta() {
        return ventasPendientes.stream().map(Venta::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
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

    private static JPanel crearPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(COLOR_FONDO);
        return panel;
    }

    private static void agregar(JPanel panel, Component comp, int fila, double pesoVertical, Insets margen) {
        panel.add(comp, new GridBagConstraints(0, fila, 1, 1, 1.0, pesoVertical,
                GridBagConstraints.CENTER, pesoVertical > 0 ? GridBagConstraints.BOTH : GridBagConstraints.HORIZONTAL, margen, 0, 0));
    }
}