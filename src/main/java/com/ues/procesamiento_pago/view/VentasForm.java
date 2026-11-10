package com.ues.procesamiento_pago.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.ArrayList;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import com.ues.procesamiento_pago.Model.Venta;
import com.ues.procesamiento_pago.config.MongoDBConnection;
import com.ues.procesamiento_pago.dao.VentaDAO;
import com.ues.procesamiento_pago.util.Estilos;

public class VentasForm extends JFrame {
    private JTextField txtNombre;
    private JTextField txtPrecio;
    private JTextField txtCantidad;
    private JTextField txtTotal;
    private JTextField txtTotalVenta;
    private JComboBox<String> comboMetodos;
    private JButton btnAgregar;
    private JButton btnGuardar;
    private JTable tablaVentas;
    private DefaultTableModel modeloTabla;
    
    private MongoDBConnection mongoDB;
    private VentaDAO dao;
    
    private final List<Venta> ventasPendientes = new ArrayList<>();
    
    private Venta ventaItemActual;
    
    private final String[] METODOS = {
        "Efectivo", "Pago con tarjeta", "Pago Electronico", "Transferencia Bancaria"
    };
    
    private static final Color COLOR_FONDO = Color.DARK_GRAY;
    private static final Color COLOR_BORDE_CAMPO = new Color(60, 60, 60);
    private static final Color COLOR_TEXTO_SECUNDARIO = new Color(160, 160, 160);
    private static final Color COLOR_VERDE = new Color(60, 220, 130);

    public VentasForm(MongoDBConnection mongoDB){
        this.mongoDB = mongoDB;

        if (this.mongoDB != null && this.mongoDB.getDatabase() != null) {
            this.dao = new VentaDAO(this.mongoDB);
        } else {
            JOptionPane.showMessageDialog(this, "No hay una conexión activa a MongoDB.");
        }
        
        setTitle("Ingreso de ventas");
        setSize(1080, 740);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 18, 16, 18));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(3, 0, 3, 0);
        
        JLabel lblTitulo = new JLabel("PUNTO DE VENTA", JLabel.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblTitulo.setForeground(Color.WHITE);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(lblTitulo, gbc);
        gbc.insets = new Insets(3, 0, 3, 0);
        
        gbc.gridy = 1;
        JLabel lblNombre = new JLabel("Nombre del producto");
        lblNombre.setForeground(COLOR_TEXTO_SECUNDARIO);
        lblNombre.setFont(new Font("SansSerif", Font.BOLD, 12));
        panel.add(lblNombre, gbc);
        
        gbc.gridy = 2;
        txtNombre = new JTextField();
        Estilos.estilarCampo(txtNombre); 
        panel.add(txtNombre, gbc);
        
        gbc.gridy = 3;
        JPanel panelCamposMedios = new JPanel(new GridBagLayout());
        panelCamposMedios.setBackground(COLOR_FONDO);
        GridBagConstraints gbcMedios = new GridBagConstraints();
        gbcMedios.fill = GridBagConstraints.HORIZONTAL;
        gbcMedios.weightx = 0.33;
        gbcMedios.insets = new Insets(0, 0, 0, 10);

        gbcMedios.gridx = 0;
        gbcMedios.gridy = 0;
        JLabel lblPrecio = new JLabel("PRECIO");
        lblPrecio.setForeground(COLOR_TEXTO_SECUNDARIO);
        lblPrecio.setFont(new Font("SansSerif", Font.BOLD, 12));
        panelCamposMedios.add(lblPrecio, gbcMedios);

        gbcMedios.gridx = 1;
        JLabel lblCantidad = new JLabel("CANTIDAD");
        lblCantidad.setForeground(COLOR_TEXTO_SECUNDARIO);
        lblCantidad.setFont(new Font("SansSerif", Font.BOLD, 12));
        panelCamposMedios.add(lblCantidad, gbcMedios);

        gbcMedios.gridx = 2;
        gbcMedios.insets = new Insets(0, 0, 0, 0);
        JLabel lblTotal = new JLabel("TOTAL");
        lblTotal.setForeground(COLOR_TEXTO_SECUNDARIO);
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 12));
        panelCamposMedios.add(lblTotal, gbcMedios);

        gbcMedios.gridy = 1;
        gbcMedios.insets = new Insets(3, 0, 0, 10);
        
        gbcMedios.gridx = 0;
        txtPrecio = new JTextField();
        Estilos.estilarCampo(txtPrecio);
        panelCamposMedios.add(txtPrecio, gbcMedios);

        gbcMedios.gridx = 1;
        txtCantidad = new JTextField();
        Estilos.estilarCampo(txtCantidad);
        panelCamposMedios.add(txtCantidad, gbcMedios);

        gbcMedios.gridx = 2;
        gbcMedios.insets = new Insets(3, 0, 0, 0);
        txtTotal = new JTextField();
        Estilos.estilarCampo(txtTotal);
        txtTotal.setEditable(false);
        panelCamposMedios.add(txtTotal, gbcMedios);

        panel.add(panelCamposMedios, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(10, 0, 10, 0);
        btnAgregar = new JButton("AGREGAR PRODUCTO");
        btnAgregar.setBackground(COLOR_VERDE);
        btnAgregar.setForeground(Color.BLACK);
        btnAgregar.setFont(new Font("SansSerif", Font.BOLD, 12));
        panel.add(btnAgregar, gbc);

        gbc.gridy = 5;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);
        String[] columnas = {"ITEM", "NOMBRE", "PRECIO", "CANTIDAD", "TOTAL"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        tablaVentas = new JTable(modeloTabla);
        tablaVentas.setPreferredScrollableViewportSize(new Dimension(450, 120));
        JScrollPane scrollPane = new JScrollPane(tablaVentas);
        panel.add(scrollPane, gbc);

        // Panel Total de la Venta (Corregido con ancho explícito en panel secundario)
        gbc.gridy = 6;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(8, 0, 8, 0);
        
        JPanel panelTotalVenta = new JPanel(new GridBagLayout());
        panelTotalVenta.setBackground(COLOR_FONDO);
        GridBagConstraints gbcTotal = new GridBagConstraints();
        gbcTotal.gridy = 0;
        
        gbcTotal.gridx = 0;
        gbcTotal.insets = new Insets(0, 0, 0, 10);
        JLabel lblTotalVenta = new JLabel("TOTAL DE LA VENTA:");
        lblTotalVenta.setForeground(Color.WHITE);
        lblTotalVenta.setFont(new Font("SansSerif", Font.BOLD, 14));
        panelTotalVenta.add(lblTotalVenta, gbcTotal);

        gbcTotal.gridx = 1;
        gbcTotal.insets = new Insets(0, 0, 0, 0);
        txtTotalVenta = new JTextField();
        Estilos.estilarCampo(txtTotalVenta, 160);
        txtTotalVenta.setEditable(false);
        txtTotalVenta.setText("$0.00");
        panelTotalVenta.add(txtTotalVenta, gbcTotal);

        panel.add(panelTotalVenta, gbc);

        // Panel Botones Inferiores
        gbc.gridy = 7;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 0, 0);
        
        JPanel panelBotones = new JPanel(new GridBagLayout());
        panelBotones.setBackground(COLOR_FONDO);
        GridBagConstraints gbcBotones = new GridBagConstraints();
        gbcBotones.gridy = 0;

        btnGuardar = new JButton("Registrar Venta");
        btnGuardar.setBackground(COLOR_VERDE);
        btnGuardar.setForeground(Color.BLACK);
        btnGuardar.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbcBotones.gridx = 0;
        gbcBotones.anchor = GridBagConstraints.WEST;
        panelBotones.add(btnGuardar, gbcBotones);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setBackground(new Color(220, 60, 60));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbcBotones.gridx = 1;
        gbcBotones.anchor = GridBagConstraints.EAST;
        gbcBotones.weightx = 1.0;
        panelBotones.add(btnCancelar, gbcBotones);

        panel.add(panelBotones, gbc);
        add(panel);    

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

        btnCancelar.addActionListener(e -> dispose());
        btnAgregar.addActionListener(e -> {
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
                
                Venta ventaTemp = new Venta(nombre, precio, cantidad);
                ventasPendientes.add(ventaTemp);

                int item = modeloTabla.getRowCount() + 1;
                modeloTabla.addRow(new Object[]{item, ventaTemp.getNombre(), "$" + ventaTemp.getPrecio(), ventaTemp.getCantidad(), "$" + ventaTemp.getTotal()});

                txtNombre.setText("");
                txtPrecio.setText("");
                txtCantidad.setText("");
                txtTotal.setText("");
                txtNombre.requestFocus();

                // Cálculo directo del total de la venta
                BigDecimal sumaTotal = BigDecimal.ZERO;
                for (Venta v : ventasPendientes) {
                    sumaTotal = sumaTotal.add(v.getTotal());
                }
                txtTotalVenta.setText("$" + sumaTotal.toString());

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "El precio y la cantidad deben ser valores numéricos válidos.", "Error de formato", JOptionPane.ERROR_MESSAGE);
            }
        });
    }  

    private void actualizarTotal() {
        try {
            String precioStr = txtPrecio.getText().trim();
            String cantidadStr = txtCantidad.getText().trim();

            if (precioStr.isEmpty() || cantidadStr.isEmpty()) {
                txtTotal.setText("");
                return;
            }

            BigDecimal precio = new BigDecimal(precioStr);
            int cantidad = Integer.parseInt(cantidadStr);

            if (cantidad < 0) {
                txtTotal.setText("");
                return;
            }

            Venta tempVenta = new Venta("", precio, cantidad);
            BigDecimal totalCalculado = tempVenta.calcularTotal(cantidad, precio);
            txtTotal.setText(totalCalculado.toString());

        } catch (NumberFormatException e) {
            txtTotal.setText("");
        }
    }
}