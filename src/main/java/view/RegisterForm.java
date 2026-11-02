package view;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.ues.procesamiento_pago.config.MongoDBConnection;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.bson.Document;

public class RegisterForm extends JFrame {
    private JTextField txtNombre;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbRol;    
    private JButton btnRegistrar;
    
    private MongoDBConnection mongoDB;
    private MongoCollection<Document> usuarios;
    
    public RegisterForm(MongoDBConnection mongoDB) {
        this.mongoDB = mongoDB;
        
        if (this.mongoDB != null && this.mongoDB.getDatabase() != null) {
            MongoDatabase db = this.mongoDB.getDatabase();
            this.usuarios = db.getCollection("usuarios");
        } else {
            JOptionPane.showMessageDialog(this, "Advertencia: No hay una conexión activa a MongoDB.");
        }
        
        setTitle("Registro de usuarios");
        setSize(420, 500);
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
        
        JLabel lblTitulo = new JLabel("REGISTRO");
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
        
        JLabel lblRol = new JLabel("ROL");
        lblRol.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblRol.setForeground(new Color(160, 160, 160));
        gbc.gridy = 5;
        cardPanel.add(lblRol, gbc);
        
        cmbRol = new JComboBox<>(new String[]{"Admin", "Vendedor"});
        cmbRol.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cmbRol.setPreferredSize(new Dimension(280, 40));
        cmbRol.setBackground(Color.BLACK);
        cmbRol.setForeground(Color.WHITE);
        cmbRol.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));
        gbc.gridy = 6;
        cardPanel.add(cmbRol, gbc);
        
        btnRegistrar = new JButton("REGISTRAR");
        btnRegistrar.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnRegistrar.setForeground(Color.BLACK);
        btnRegistrar.setBackground(Color.WHITE);
        btnRegistrar.setFocusPainted(false);
        btnRegistrar.setBorderPainted(false);
        btnRegistrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegistrar.setPreferredSize(new Dimension(280, 42));
        
        gbc.gridy = 7;
        gbc.insets = new Insets(25, 0, 0, 0);
        cardPanel.add(btnRegistrar, gbc);
        
        mainPanel.add(cardPanel);
        this.add(mainPanel);
        
        btnRegistrar.addActionListener(e -> {
            String nombre = txtNombre.getText().trim();
            String password = new String(txtPassword.getPassword());
            String rol = cmbRol.getSelectedItem().toString();
            //filtro para ver si el nombre de usuario ya esta en uso
            Document existente = this.usuarios.find(Filters.eq("nombre", nombre)).first();
            if (existente != null) {
                JOptionPane.showMessageDialog(this, "El nombre de usuario ya está registrado.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (nombre.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Rellene todos los campos", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (this.usuarios == null) {
                JOptionPane.showMessageDialog(this, "No hay conexión con la colección de usuarios.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                Document newUsuario = new Document();
                newUsuario.append("nombre", nombre);
                newUsuario.append("password", password);
                newUsuario.append("rol", rol);
                newUsuario.append("createdAt", new Date());

                this.usuarios.insertOne(newUsuario);
                
                JOptionPane.showMessageDialog(this, "¡Usuario registrado exitosamente!");
                txtNombre.setText("");
                txtPassword.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al registrar Usuario: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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