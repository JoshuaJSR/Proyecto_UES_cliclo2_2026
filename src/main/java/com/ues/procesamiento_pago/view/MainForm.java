package com.ues.procesamiento_pago.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.ues.procesamiento_pago.config.MongoDBConnection;

public class MainForm extends JFrame {
    private MongoDBConnection mongoDB;
    private String nombreUsuario;

    public MainForm(MongoDBConnection mongoDB, String nombreUsuario, String rol) {
        this.mongoDB = mongoDB;
        this.nombreUsuario = nombreUsuario;

        setTitle("Sistema de procesamiento de pagos");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(Color.BLACK);

        JLabel lblBienvenida = new JLabel("Bienvenido, " + nombreUsuario, SwingConstants.CENTER);
        lblBienvenida.setFont(new Font("SansSerif", Font.BOLD, 32));
        lblBienvenida.setForeground(Color.WHITE);

        JLabel lblRol = new JLabel("Rol: " + rol, SwingConstants.CENTER);
        lblRol.setFont(new Font("SansSerif", Font.PLAIN, 16));
        lblRol.setForeground(new Color(160, 160, 160));

        JPanel panelCentro = new JPanel(new BorderLayout(0, 10));
        panelCentro.setBackground(Color.BLACK);
        panelCentro.add(lblBienvenida, BorderLayout.CENTER);
        panelCentro.add(lblRol, BorderLayout.SOUTH);
        panelCentro.setBorder(BorderFactory.createEmptyBorder(0, 0, 200, 0));

        panelPrincipal.add(panelCentro, BorderLayout.CENTER);
        this.add(panelPrincipal);

        JMenuBar menuBar = new JMenuBar();
        menuBar.setOpaque(true);
        menuBar.setBackground(Color.BLACK);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 60, 60)));

        JMenu menuOpciones = crearMenu("Opciones");
        JMenuItem itemCerrarSesion = crearItem("Cerrar sesión");
        JMenuItem itemSalir = crearItem("Salir");
        menuOpciones.add(itemCerrarSesion);
        menuOpciones.add(itemSalir);

        JMenu menuMantenimiento = crearMenu("Mantenimiento");
        JMenuItem itemRegistroUsuarios = crearItem("Registro de usuarios");
        itemRegistroUsuarios.setEnabled("Admin".equals(rol));
        menuMantenimiento.add(itemRegistroUsuarios);
        
        if (!itemRegistroUsuarios.isEnabled()) {
            itemRegistroUsuarios.setForeground(new Color(90, 90, 90));
        }

        JMenu menuMovimientos = crearMenu("Movimientos");
        JMenuItem itemIngresoVentas = crearItem("Ingreso de ventas");
        JMenuItem itemModificacionVentas = crearItem("Modificación de ventas");
        menuMovimientos.add(itemIngresoVentas);
        menuMovimientos.add(itemModificacionVentas);

        JMenu menuConsultas = crearMenu("Consultas");
        JMenuItem itemConsultarVentas = crearItem("Consultar ventas");
        menuConsultas.add(itemConsultarVentas);

        menuBar.add(menuOpciones);
        menuBar.add(menuMantenimiento);
        menuBar.add(menuMovimientos);
        menuBar.add(menuConsultas);

        setJMenuBar(menuBar);

        itemCerrarSesion.addActionListener(e -> {
            new LoginForm(this.mongoDB).setVisible(true);
            this.dispose();
        });

        itemSalir.addActionListener(e -> System.exit(0));

        itemRegistroUsuarios.addActionListener(e -> {
            new RegisterForm(this.mongoDB).setVisible(true);
        });

        itemIngresoVentas.addActionListener(e -> {
            new VentasForm(this.mongoDB, this.nombreUsuario).setVisible(true);
        });

        itemModificacionVentas.addActionListener(e -> {
            new ModificacionVentasForm(this.mongoDB).setVisible(true);
        });

        itemConsultarVentas.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Consultar ventas (pendiente)");
        });
    }

    private JMenu crearMenu(String texto) {
        JMenu menu = new JMenu(texto);
        menu.setFont(new Font("SansSerif", Font.PLAIN, 14));
        menu.setForeground(Color.WHITE);
        menu.setOpaque(true);
        menu.setBackground(Color.BLACK);
        menu.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        menu.getPopupMenu().setBackground(Color.BLACK);
        menu.getPopupMenu().setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));
        return menu;
    }

    private JMenuItem crearItem(String texto) {
        JMenuItem item = new JMenuItem(texto);
        item.setFont(new Font("SansSerif", Font.PLAIN, 13));
        item.setForeground(Color.WHITE);
        item.setOpaque(true);
        item.setBackground(Color.BLACK);
        item.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return item;
    }
}