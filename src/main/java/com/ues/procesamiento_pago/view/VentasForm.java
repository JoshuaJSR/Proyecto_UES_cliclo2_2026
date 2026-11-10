package com.ues.procesamiento_pago.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
        JScrollPane scrollPane = new JScrollPane(tablaVentas);
        panel.add(scrollPane, gbc);

        gbc.gridy = 6;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 0, 0);
        
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
                double precio = Double.parseDouble(precioStr);
                int cantidad = Integer.parseInt(cantidadStr);
                int item = modeloTabla.getRowCount() + 1;

                modeloTabla.addRow(new Object[]{item, nombre, precio, cantidad, ""});

                txtNombre.setText("");
                txtPrecio.setText("");
                txtCantidad.setText("");
                txtTotal.setText("");
                txtNombre.requestFocus();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "El precio y la cantidad deben ser valores numéricos válidos.", "Error de formato", JOptionPane.ERROR_MESSAGE);
            }
        });
    }  

}