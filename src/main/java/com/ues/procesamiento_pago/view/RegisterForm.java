package com.ues.procesamiento_pago.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.mindrot.jbcrypt.BCrypt;

import com.mongodb.MongoException;
import com.ues.procesamiento_pago.Model.Usuario;
import com.ues.procesamiento_pago.config.MongoDBConnection;
import com.ues.procesamiento_pago.dao.UsuarioDAO;
import com.ues.procesamiento_pago.util.Estilos;
import com.ues.procesamiento_pago.view.components.TableUsuariosModel;

public class RegisterForm extends JFrame {

    private static final String TODOS = "Todos";
    private static final String[] ROLES = {"Admin", "Vendedor"};
    private static final Color FONDO = Color.BLACK;
    private static final Color BORDE = new Color(70, 70, 70);

    private final UsuarioDAO dao;
    private List<Usuario> usuarios = new ArrayList<>();
    private String usuarioSeleccionado;

    private JTextField txtNombre;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbRol;
    private JTextField txtBuscarNombre;
    private JComboBox<String> cmbFiltroRol;
    private JLabel lblModo, lblAyudaPassword, lblResumen;
    private JTable tablaUsuarios;
    private TableUsuariosModel modeloUsuarios;
    private JButton btnRegistrar, btnActualizar, btnEliminar, btnLimpiar, btnBuscar;

    public RegisterForm(MongoDBConnection mongoDB) {
        this.dao = mongoDB != null && mongoDB.getDatabase() != null ? new UsuarioDAO(mongoDB) : null;
        if (dao == null) {
            JOptionPane.showMessageDialog(null, "Advertencia: No hay una conexión activa a MongoDB.");
        }

        setTitle("Mantenimiento de usuarios");
        setSize(1080, 700);
        setMinimumSize(new Dimension(900, 560));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        add(crearContenido());
        conectarEventos();
        if (dao != null) {
            cargarUsuarios();
        }
    }


    private JPanel crearContenido() {
        JPanel panel = panel(new BorderLayout(0, 14));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel titulo = Estilos.etiqueta("MANTENIMIENTO DE USUARIOS", true);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        JLabel subtitulo = Estilos.etiqueta("Registre, consulte, edite y elimine las cuentas que acceden al sistema.", false);
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel encabezado = panel(new BorderLayout(0, 4));
        encabezado.add(titulo, BorderLayout.NORTH);
        encabezado.add(subtitulo, BorderLayout.SOUTH);
        panel.add(encabezado, BorderLayout.NORTH);

        JPanel cuerpo = panel(new BorderLayout(16, 0));
        cuerpo.add(crearFormulario(), BorderLayout.WEST);
        cuerpo.add(crearListado(), BorderLayout.CENTER);
        panel.add(cuerpo, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearFormulario() {
        JPanel card = panel(new GridBagLayout());
        card.setPreferredSize(new Dimension(340, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(40, 40, 40), 1),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)));

        JPanel cabecera = panel(new BorderLayout(0, 4));
        cabecera.add(Estilos.etiqueta("DATOS DEL USUARIO", true), BorderLayout.NORTH);
        lblModo = Estilos.etiqueta("Nuevo usuario", false);
        cabecera.add(lblModo, BorderLayout.SOUTH);
        agregar(card, cabecera, 0, 16);

        txtNombre = Estilos.crearCampo(280);
        txtPassword = new JPasswordField();
        Estilos.estilarCampo(txtPassword, 280);
        cmbRol = combo(ROLES, 280);
        lblAyudaPassword = Estilos.etiqueta("Se cifra con BCrypt al guardar.", false);

        agregar(card, Estilos.etiqueta("USUARIO", false), 1, 4);
        agregar(card, txtNombre, 2, 14);
        agregar(card, Estilos.etiqueta("CONTRASEÑA", false), 3, 4);
        agregar(card, txtPassword, 4, 4);
        agregar(card, lblAyudaPassword, 5, 14);
        agregar(card, Estilos.etiqueta("ROL", false), 6, 4);
        agregar(card, cmbRol, 7, 20);

        btnRegistrar = boton("REGISTRAR", Color.WHITE, Color.BLACK);
        btnActualizar = boton("ACTUALIZAR", Estilos.COLOR_VERDE, Color.BLACK);
        btnEliminar = boton("ELIMINAR", Estilos.COLOR_ROJO, Color.WHITE);
        btnLimpiar = boton("LIMPIAR", new Color(45, 45, 45), Color.WHITE);

        JPanel acciones = panel(new GridLayout(2, 2, 8, 8));
        acciones.add(btnRegistrar);
        acciones.add(btnLimpiar);
        acciones.add(btnActualizar);
        acciones.add(btnEliminar);
        agregar(card, acciones, 8, 0);

        GridBagConstraints relleno = new GridBagConstraints();
        relleno.gridy = 9;
        relleno.weighty = 1.0;
        relleno.fill = GridBagConstraints.BOTH;
        card.add(panel(new BorderLayout()), relleno);
        return card;
    }

    private JPanel crearListado() {
        JPanel panel = panel(new BorderLayout(0, 10));
        panel.add(crearFiltros(), BorderLayout.NORTH);

        modeloUsuarios = new TableUsuariosModel();
        tablaUsuarios = new JTable(modeloUsuarios);
        Estilos.estilarTabla(tablaUsuarios, new int[] {50, 260, 140, 220});
        tablaUsuarios.setRowHeight(26);
        tablaUsuarios.setFillsViewportHeight(true);

        JPanel contenedor = panel(new BorderLayout());
        contenedor.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDE, 1), "USUARIOS REGISTRADOS",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                Estilos.FUENTE_ETIQUETA, Color.WHITE));
        contenedor.add(new JScrollPane(tablaUsuarios));
        panel.add(contenedor, BorderLayout.CENTER);

        lblResumen = Estilos.etiqueta(" ", false);
        panel.add(lblResumen, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearFiltros() {
        JPanel panel = panel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));

        txtBuscarNombre = Estilos.crearCampo(200);
        cmbFiltroRol = combo(new String[] {TODOS, ROLES[0], ROLES[1]}, 130);
        btnBuscar = boton("BUSCAR", Estilos.COLOR_VERDE, Color.BLACK);
        btnBuscar.setPreferredSize(new Dimension(100, 35));

        panel.add(etiquetado("USUARIO:", txtBuscarNombre));
        panel.add(etiquetado("ROL:", cmbFiltroRol));
        panel.add(btnBuscar);
        return panel;
    }

    private void conectarEventos() {
        btnRegistrar.addActionListener(e -> registrar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnBuscar.addActionListener(e -> {
            if (dao != null) {
                cargarUsuarios();
            }
        });

        txtBuscarNombre.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { aplicarFiltro(); }
            @Override
            public void removeUpdate(DocumentEvent e) { aplicarFiltro(); }
            @Override 
            public void changedUpdate(DocumentEvent e) { aplicarFiltro(); }
        });
        cmbFiltroRol.addActionListener(e -> aplicarFiltro());

        tablaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaUsuarios.getSelectedRow() >= 0) {
                cargarUsuarioEnFormulario(tablaUsuarios.getSelectedRow());
            }
        });
    }


    private void cargarUsuarios() {
        try {
            usuarios = dao.listar();
            aplicarFiltro();
        } catch (MongoException ex) {
            avisar("Error al consultar los usuarios: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aplicarFiltro() {
        String texto = txtBuscarNombre.getText().trim().toLowerCase();
        String rol = (String) cmbFiltroRol.getSelectedItem();

        List<Usuario> filtrados = new ArrayList<>();
        for (Usuario usuario : usuarios) {
            String nombre = usuario.getNombre() == null ? "" : usuario.getNombre().toLowerCase();
            boolean coincideNombre = nombre.contains(texto);
            boolean coincideRol = rol == null || TODOS.equals(rol) || rol.equals(usuario.getRol());
            if (coincideNombre && coincideRol) {
                filtrados.add(usuario);
            }
        }

        tablaUsuarios.clearSelection();
        modeloUsuarios.setUsuarios(filtrados);
        lblResumen.setText(filtrados.isEmpty()
                ? "No se encontraron usuarios con los filtros aplicados."
                : filtrados.size() + " usuario(s) encontrados. Haga clic en una fila para cargar sus datos.");
    }

    private void registrar() {
        if (sinConexion()) return;

        String nombre = txtNombre.getText().trim();
        String password = new String(txtPassword.getPassword());
        if (nombre.isEmpty() || password.isEmpty()) {
            avisar("Rellene todos los campos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (dao.existe(nombre)) {
                avisar("El nombre de usuario ya está registrado.", JOptionPane.WARNING_MESSAGE);
                return;
            }
            dao.insertar(new Usuario(nombre, BCrypt.hashpw(password, BCrypt.gensalt()), rolSeleccionado()));
            avisar("¡Usuario registrado exitosamente!", JOptionPane.INFORMATION_MESSAGE);
            refrescar(nombre);
        } catch (MongoException ex) {
            avisar("Error al registrar el usuario: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizar() {
        String original = seleccionRequerida("editarlo");
        if (original == null) return;

        String nombreNuevo = txtNombre.getText().trim();
        String password = new String(txtPassword.getPassword());
        if (nombreNuevo.isEmpty()) {
            avisar("El nombre de usuario es obligatorio", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (!nombreNuevo.equals(original) && dao.existe(nombreNuevo)) {
                avisar("Ya existe otro usuario con ese nombre.", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Usuario actual = dao.buscarPorNombre(original);
            if (actual == null) {
                avisar("El usuario seleccionado ya no existe en la base de datos.", JOptionPane.WARNING_MESSAGE);
                limpiarFormulario();
                cargarUsuarios();
                return;
            }

            // si la contraseña se deja vacía se conserva la actual
            String hash = password.isEmpty() ? actual.getPassword() : BCrypt.hashpw(password, BCrypt.gensalt());
            dao.actualizar(original, new Usuario(nombreNuevo, hash, rolSeleccionado()));
            avisar("Usuario actualizado correctamente.", JOptionPane.INFORMATION_MESSAGE);
            refrescar(nombreNuevo);
        } catch (MongoException ex) {
            avisar("Error al actualizar el usuario: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminar() {
        String original = seleccionRequerida("eliminarlo");
        if (original == null) return;

        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Eliminar al usuario \"" + original + "\"?\nEsta acción no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opcion != JOptionPane.YES_OPTION) return;

        try {
            dao.eliminar(original);
            avisar("Usuario eliminado correctamente.", JOptionPane.INFORMATION_MESSAGE);
            limpiarFormulario();
            cargarUsuarios();
        } catch (MongoException ex) {
            avisar("Error al eliminar el usuario: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }


    private void cargarUsuarioEnFormulario(int fila) {
        Usuario usuario = modeloUsuarios.getUsuarioAt(fila);
        usuarioSeleccionado = usuario.getNombre();
        txtNombre.setText(usuario.getNombre());
        txtPassword.setText("");
        cmbRol.setSelectedItem(usuario.getRol());
        lblModo.setText("Editando: " + usuario.getNombre());
        lblAyudaPassword.setText("Vacía = conservar la contraseña actual.");
    }

    private void seleccionarUsuario(String nombre) {
        for (int i = 0; i < modeloUsuarios.getRowCount(); i++) {
            if (nombre.equals(modeloUsuarios.getUsuarioAt(i).getNombre())) {
                tablaUsuarios.setRowSelectionInterval(i, i);
                tablaUsuarios.scrollRectToVisible(tablaUsuarios.getCellRect(i, 0, true));
                return;
            }
        }
    }

    private void limpiarFormulario() {
        usuarioSeleccionado = null;
        txtNombre.setText("");
        txtPassword.setText("");
        cmbRol.setSelectedIndex(0);
        tablaUsuarios.clearSelection();
        lblModo.setText("Nuevo usuario");
        lblAyudaPassword.setText("Se cifra con BCrypt al guardar.");
    }

    private boolean sinConexion() {
        if (dao != null) return false;
        avisar("No hay conexión con la base de datos.", JOptionPane.ERROR_MESSAGE);
        return true;
    }

    private String seleccionRequerida(String accion) {
        if (sinConexion()) return null;
        if (usuarioSeleccionado == null) {
            avisar("Seleccione un usuario del listado para " + accion + ".", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return usuarioSeleccionado;
    }

    private void refrescar(String nombre) {
        limpiarFormulario();
        cargarUsuarios();
        seleccionarUsuario(nombre);
    }

    private String rolSeleccionado() {
        String rol = (String) cmbRol.getSelectedItem();
        return rol == null ? ROLES[1] : rol;
    }


    private void avisar(String mensaje, int tipo) {
        String titulo = switch (tipo) {
            case JOptionPane.ERROR_MESSAGE -> "Error";
            case JOptionPane.WARNING_MESSAGE -> "Advertencia";
            default -> "Información";
        };
        JOptionPane.showMessageDialog(this, mensaje, titulo, tipo);
    }

    private static JPanel panel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(FONDO);
        return panel;
    }

    private static JButton boton(String texto, Color fondo, Color colorTexto) {
        JButton boton = Estilos.boton(texto, fondo, colorTexto);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setPreferredSize(new Dimension(130, 38));
        return boton;
    }

    private static JComboBox<String> combo(String[] items, int ancho) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setPreferredSize(new Dimension(ancho, 35));
        combo.setBackground(FONDO);
        combo.setForeground(Color.WHITE);
        combo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return combo;
    }

    private static JPanel etiquetado(String etiqueta, Component campo) {
        JPanel panel = panel(new BorderLayout(0, 2));
        panel.add(Estilos.etiqueta(etiqueta, false), BorderLayout.NORTH);
        panel.add(campo, BorderLayout.CENTER);
        return panel;
    }

    private static void agregar(JPanel panel, Component componente, int fila, int margenInferior) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, margenInferior, 0);
        panel.add(componente, gbc);
    }
}
