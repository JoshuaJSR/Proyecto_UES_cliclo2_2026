package com.ues.procesamiento_pago.view;

import java.awt.Color;
import java.awt.Dimension;
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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.ues.procesamiento_pago.Model.Venta;
import com.ues.procesamiento_pago.config.MongoDBConnection;
import com.ues.procesamiento_pago.dao.VentaDAO;

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
    
    private Venta ventaItemActual; //Producto que se captra en los campos
    
    //se mapea la seleccion del combo con los metodos de pago que heredan de MetodoPago 
    private final String[] METODOS = {
        "Efectivo", "Pago con tarjeta", "Pago Electronico", "Transferencia Bancaria"
    };
    
    //paleta de colores
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
        
        setResizable(false); //evitar maximizar pantalla
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 18, 16, 18));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(3, 0, 3, 0);
        
        //titulo
        JLabel lblTitulo = new JLabel("PUNTO DE VENTA", JLabel.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblTitulo.setForeground(Color.WHITE);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(lblTitulo, gbc);
        gbc.insets = new Insets(3, 0, 3, 0);
        
        //nombre del producto
        gbc.gridy = 1;
        JLabel lblNombre = new JLabel("Nombre del producto");
        lblNombre.setForeground(COLOR_TEXTO_SECUNDARIO);
        lblNombre.setFont(new Font("SansSerif", Font.BOLD, 12));
        panel.add(lblNombre, gbc);
        
        gbc.gridy = 2;
        txtNombre = new JTextField();
        estilarCampoTexto(txtNombre); 
        panel.add(txtNombre, gbc);
        
        //precio y cantidad
        gbc.gridy = 3;
        gbc.gridx = 0;
        
        gbc.gridx = 1;
        panel.add(new JLabel("CANTIDAD"), gbc);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.5;
        txtPrecio = new JTextField();
        estilarCampoTexto(txtPrecio);
        panel.add(txtPrecio, gbc);
        gbc.gridx = 1;
        txtCantidad = new JTextField();
        estilarCampoTexto(txtCantidad);
        panel.add(txtCantidad, gbc);
        gbc.weightx = 0;
        gbc.gridx = 0;
        
        
        // Añadir el panel al JFrame
        add(panel);    
    }
    private void estilarCampoTexto(JTextField campo) {
        campo.setPreferredSize(new Dimension(campo.getPreferredSize().width, 35));
        campo.setBackground(new Color(25, 25, 25));
        campo.setForeground(Color.WHITE);
        campo.setCaretColor(Color.WHITE);
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDE_CAMPO, 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }
}
