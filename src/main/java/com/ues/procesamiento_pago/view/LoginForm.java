package com.ues.procesamiento_pago.view;

import com.ues.procesamiento_pago.Model.Usuario;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.ues.procesamiento_pago.config.MongoDBConnection;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.bson.Document;

import at.favre.lib.crypto.bcrypt.BCrypt;

import com.mongodb.client.model.Filters;
import com.ues.procesamiento_pago.dao.UsuarioDAO;



public class LoginForm extends JFrame {
    private JTextField txtNombre;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    private MongoDBConnection mongoDB;
    private MongoCollection<Document> usuarios;
    private UsuarioDAO dao;
    
    public LoginForm(MongoDBConnection mongoDB) {
        this.mongoDB = mongoDB;
        
        if (this.mongoDB != null && this.mongoDB.getDatabase() != null) {
            this.dao = new UsuarioDAO(this.mongoDB);
        } else 
        {
        JOptionPane.showMessageDialog(this, "Advertencia: No hay una conexión activa a MongoDB.");
        }

        setTitle("Inicio de sesión");
        setSize(420, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.BLACK);

        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setBackground(Color.BLACK);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(40, 40, 40), 1),
            BorderFactory.createEmptyBorder(35, 35, 35, 35)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel lblTitulo = new JLabel("INICIO DE SESIÓN");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 25, 0);
        cardPanel.add(lblTitulo, gbc);

        gbc.insets = new Insets(8, 0, 4, 0);

        JLabel lblNombre = new JLabel("USUARIO");
        lblNombre.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblNombre.setForeground(new Color(160, 160, 160));
        gbc.gridy = 1;
        cardPanel.add(lblNombre, gbc);

        txtNombre = new JTextField();
        estilarCampo(txtNombre);
        gbc.gridy = 2;
        cardPanel.add(txtNombre, gbc);

        JLabel lblPassword = new JLabel("CONTRASEÑA");
        lblPassword.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblPassword.setForeground(new Color(160, 160, 160));
        gbc.gridy = 3;
        cardPanel.add(lblPassword, gbc);

        txtPassword = new JPasswordField();
        estilarCampo(txtPassword);
        gbc.gridy = 4;
        cardPanel.add(txtPassword, gbc);

        btnLogin = new JButton("INICIAR SESIÓN");
        btnLogin.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnLogin.setForeground(Color.BLACK);
        btnLogin.setBackground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setPreferredSize(new Dimension(280, 42));

        gbc.gridy = 5;
        gbc.insets = new Insets(25, 0, 0, 0);
        cardPanel.add(btnLogin, gbc);

        mainPanel.add(cardPanel);
        this.add(mainPanel);
        
        btnLogin.addActionListener(e -> {
            String nombre = txtNombre.getText().trim();
            String password = new String(txtPassword.getPassword());
            if (nombre.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Rellene todos los campos", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (this.dao == null) {
                JOptionPane.showMessageDialog(this, "No hay conexión con la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                Usuario usuario = dao.buscarPorNombre(nombre);
                if (usuario == null) {
                    JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), usuario.getPassword());
                if (result.verified) {
                    JFrame mainForm = (JFrame) Class.forName("com.ues.procesamiento_pago.view.MainForm")
                        .getConstructor(MongoDBConnection.class, String.class, String.class)
                        .newInstance(this.mongoDB, usuario.getNombre(), usuario.getRol());
                    mainForm.setVisible(true);
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al iniciar sesión: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
        
    private void estilarCampo(JTextField campo) {
        campo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campo.setPreferredSize(new Dimension(280, 40));
        campo.setBackground(Color.BLACK);
        campo.setForeground(Color.WHITE);
        campo.setCaretColor(Color.WHITE);
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60), 1),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
    }
}