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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableModel;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.ues.procesamiento_pago.Model.EstadoPago;
import com.ues.procesamiento_pago.Model.Venta;
import com.ues.procesamiento_pago.Strategy.MetodoPago;
import com.ues.procesamiento_pago.config.MongoDBConnection;
import com.ues.procesamiento_pago.dao.VentaDAO;
import com.ues.procesamiento_pago.util.Estilos;
public class ConsultaVentasForm extends JFrame {
    private static final String TODOS = "Todos";
    private static final String FORMATO_FECHA = "dd/MM/yyyy";

    private static final String[] METODOS = {
        "Efectivo", "Pago con tarjeta", "Pago Electronico", "Transferencia Bancaria"
    };

    private static final String[] COLUMNAS = {
        "ID TRANSACCIÓN", "N° VENTA", "PRODUCTO", "PRECIO", "CANTIDAD", "TOTAL",
        "ESTADO", "MÉTODO DE PAGO", "FECHA", "USUARIO"
    };

    private JTextField txtIdTransaccion;
    private JTextField txtNumeroVenta;
    private JComboBox<String> comboMetodo;
    private JComboBox<String> comboEstado;
    private JSpinner spDesde;
    private JSpinner spHasta;
    private JButton btnBuscar;
    private JButton btnLimpiar;

    private DefaultTableModel modeloTabla;
    private JTable tablaVentas;
    private JLabel lblResumen;

    private final VentaDAO dao;

    private static final Color COLOR_FONDO = Color.DARK_GRAY;
    private static final Color COLOR_TEXTO_SECUNDARIO = new Color(160, 160, 160);
    private static final Color COLOR_VERDE = new Color(60, 220, 130);
    private static final Color COLOR_ROJO = new Color(220, 60, 60);
    private static final Font FUENTE_ETIQUETA = new Font("SansSerif", Font.BOLD, 12);
    private static final Insets MARGEN = new Insets(3, 0, 3, 0);
    private static final Insets SIN_MARGEN = new Insets(0, 0, 0, 0);

    public ConsultaVentasForm(MongoDBConnection mongoDB) {
        if (mongoDB != null && mongoDB.getDatabase() != null) {
            this.dao = new VentaDAO(mongoDB);
        } else {
            this.dao = null;
            JOptionPane.showMessageDialog(null, "No hay una conexión activa a MongoDB.");
        }

        setTitle("Consulta de ventas");
        setSize(1200, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        add(crearContenido());
        conectarEventos();

        if (dao != null) {
            buscar();
        }
    }

    private JPanel crearContenido() {
        JPanel panel = crearPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 18, 16, 18));

        JLabel lblTitulo = new JLabel("CONSULTA DE VENTAS", JLabel.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblTitulo.setForeground(Color.WHITE);

        agregar(panel, lblTitulo, 0, 0, new Insets(0, 0, 10, 0));
        agregar(panel, crearPanelFiltros(), 1, 0, MARGEN);
        agregar(panel, crearPanelTabla(), 2, 1, SIN_MARGEN);
        agregar(panel, crearPanelBotones(), 3, 0, new Insets(8, 0, 0, 0));
        return panel;
    }

    private JPanel crearPanelFiltros() {
        JPanel panel = crearPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        txtIdTransaccion = campo();
        txtIdTransaccion.setPreferredSize(new Dimension(240, 32));
        txtNumeroVenta = campo();
        txtNumeroVenta.setPreferredSize(new Dimension(80, 32));
        comboMetodo = combo(METODOS);
        comboEstado = combo(Arrays.stream(EstadoPago.values()).map(Enum::name).toArray(String[]::new));
        spDesde = fecha(primerDiaDelMes());
        spHasta = fecha(fechaHoy());

        String[] etiquetas = {
            "ID TRANSACCIÓN:", "N° DE VENTA:", "MÉTODO DE PAGO:", "ESTADO:", "DESDE:", "HASTA:"
        };
        Component[] campos = {
            txtIdTransaccion, txtNumeroVenta, comboMetodo, comboEstado, spDesde, spHasta
        };

        for (int i = 0; i < etiquetas.length; i++) {
            GridBagConstraints gbcEtiqueta = new GridBagConstraints();
            gbcEtiqueta.gridx = i;
            gbcEtiqueta.gridy = 0;
            gbcEtiqueta.anchor = GridBagConstraints.WEST;
            gbcEtiqueta.insets = new Insets(0, 0, 2, 10);
            panel.add(etiqueta(etiquetas[i]), gbcEtiqueta);

            GridBagConstraints gbcCampo = new GridBagConstraints();
            gbcCampo.gridx = i;
            gbcCampo.gridy = 1;
            gbcCampo.weightx = i == 0 ? 1.0 : 0.0;
            gbcCampo.fill = GridBagConstraints.HORIZONTAL;
            gbcCampo.insets = new Insets(0, 0, 0, 10);
            panel.add(campos[i], gbcCampo);
        }

        JLabel lblAyuda = new JLabel("Deje un filtro vacío o en 'Todos' para no aplicarlo.");
        lblAyuda.setForeground(COLOR_TEXTO_SECUNDARIO);
        lblAyuda.setFont(FUENTE_ETIQUETA);

        GridBagConstraints gbcAyuda = new GridBagConstraints();
        gbcAyuda.gridx = 0;
        gbcAyuda.gridy = 2;
        gbcAyuda.gridwidth = etiquetas.length;
        gbcAyuda.anchor = GridBagConstraints.WEST;
        gbcAyuda.insets = new Insets(6, 0, 0, 0);
        panel.add(lblAyuda, gbcAyuda);
        return panel;
    }

    private JPanel crearPanelTabla() {
        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaVentas = new JTable(modeloTabla);
        tablaVentas.setBackground(new Color(25, 25, 25));
        tablaVentas.setForeground(Color.WHITE);
        tablaVentas.setGridColor(new Color(60, 60, 60));
        tablaVentas.setSelectionBackground(new Color(45, 120, 85));
        tablaVentas.setSelectionForeground(Color.WHITE);
        tablaVentas.setRowHeight(24);
        tablaVentas.getTableHeader().setBackground(Color.BLACK);
        tablaVentas.getTableHeader().setForeground(Color.WHITE);
        tablaVentas.getTableHeader().setFont(FUENTE_ETIQUETA);
        tablaVentas.setAutoCreateRowSorter(true);

        int[] anchos = {180, 70, 210, 90, 80, 100, 100, 160, 130, 110};
        for (int i = 0; i < anchos.length; i++) {
            tablaVentas.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        JPanel panel = crearPanel(new BorderLayout(0, 4));
        panel.add(new JScrollPane(tablaVentas), BorderLayout.CENTER);

        lblResumen = new JLabel(" ");
        lblResumen.setForeground(COLOR_TEXTO_SECUNDARIO);
        lblResumen.setFont(FUENTE_ETIQUETA);
        panel.add(lblResumen, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelBotones() {
        JPanel panel = crearPanel(new BorderLayout());
        JPanel izquierda = crearPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnBuscar = boton("BUSCAR", COLOR_VERDE, Color.BLACK);
        btnLimpiar = boton("LIMPIAR FILTROS", COLOR_ROJO, Color.WHITE);
        izquierda.add(btnBuscar);
        izquierda.add(btnLimpiar);

        JButton btnCerrar = boton("Cerrar", COLOR_ROJO, Color.WHITE);
        btnCerrar.addActionListener(e -> dispose());

        panel.add(izquierda, BorderLayout.WEST);
        panel.add(btnCerrar, BorderLayout.EAST);
        return panel;
    }

    private void conectarEventos() {
        btnBuscar.addActionListener(e -> buscar());
        btnLimpiar.addActionListener(e -> limpiarFiltros());
        txtIdTransaccion.addActionListener(e -> buscar());
        txtNumeroVenta.addActionListener(e -> buscar());
    }

    private void buscar() {
        if (dao == null) {
            JOptionPane.showMessageDialog(this, "No hay conexión con la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // buscar transaccion por su identificador (_id del documento)
        ObjectId idTransaccion = null;
        String textoId = txtIdTransaccion.getText().trim();
        if (!textoId.isEmpty()) {
            try {
                idTransaccion = new ObjectId(textoId);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this,
                        "El identificador de la transacción no es válido.\nUse el valor de la columna ID TRANSACCIÓN.",
                        "Error de formato", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // consultar la venta asociada (numero de venta)
        Integer numeroVenta = null;
        String textoVenta = txtNumeroVenta.getText().trim();
        if (!textoVenta.isEmpty()) {
            try {
                numeroVenta = Integer.valueOf(textoVenta);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "El número de venta debe ser numérico.",
                        "Error de formato", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // consultar por periodo de fechas
        Date desde = inicioDelDia((Date) spDesde.getValue());
        Date hasta = finDelDia((Date) spHasta.getValue());
        if (desde.after(hasta)) {
            JOptionPane.showMessageDialog(this, "La fecha 'Desde' no puede ser posterior a la fecha 'Hasta'.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 4.2 metodo de pago y estado
        List<Document> ventas = dao.listar(idTransaccion, numeroVenta, valorFiltro(comboEstado),
                valorFiltro(comboMetodo), desde, hasta);
        mostrarResultados(ventas);
    }

    private void mostrarResultados(List<Document> ventas) {
        modeloTabla.setRowCount(0);
        BigDecimal totalGeneral = BigDecimal.ZERO;
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        for (Document venta : ventas) {
            BigDecimal precio = Venta.aBigDecimal(venta.get("precio"));
            BigDecimal total = Venta.aBigDecimal(venta.get("total"));
            totalGeneral = totalGeneral.add(total);

            Object id = venta.get("_id");
            Object fecha = venta.getDate("fecha");
            Integer numero = venta.getInteger("id");

            modeloTabla.addRow(new Object[] {
                id == null ? "-" : id.toString(),
                numero == null ? "-" : numero,
                venta.getString("nombre"),
                "$" + MetodoPago.formatear(precio),
                venta.getInteger("cantidad", 0),
                "$" + MetodoPago.formatear(total),
                venta.getString("estado"),
                venta.getString("metodoPago"),
                fecha instanceof Date ? formatoFecha.format((Date) fecha) : "-",
                venta.getString("usuario")
            });
        }

        lblResumen.setText(ventas.isEmpty()
                ? "No se encontraron transacciones con esos filtros."
                : ventas.size() + " transacción(es) encontradas. Total: $" + MetodoPago.formatear(totalGeneral));
    }

    private void limpiarFiltros() {
        txtIdTransaccion.setText("");
        txtNumeroVenta.setText("");
        comboMetodo.setSelectedIndex(0);
        comboEstado.setSelectedIndex(0);
        spDesde.setValue(primerDiaDelMes());
        spHasta.setValue(fechaHoy());
        buscar();
    }

    private static Date fechaHoy() {
        return inicioDelDia(new Date());
    }

    private static Date primerDiaDelMes() {
        Calendar calendario = Calendar.getInstance();
        calendario.set(Calendar.DAY_OF_MONTH, 1);
        return inicioDelDia(calendario.getTime());
    }

    private static Date inicioDelDia(Date fecha) {
        Calendar calendario = Calendar.getInstance();
        calendario.setTime(fecha);
        calendario.set(Calendar.HOUR_OF_DAY, 0);
        calendario.set(Calendar.MINUTE, 0);
        calendario.set(Calendar.SECOND, 0);
        calendario.set(Calendar.MILLISECOND, 0);
        return calendario.getTime();
    }

    private static Date finDelDia(Date fecha) {
        Calendar calendario = Calendar.getInstance();
        calendario.setTime(fecha);
        calendario.set(Calendar.HOUR_OF_DAY, 23);
        calendario.set(Calendar.MINUTE, 59);
        calendario.set(Calendar.SECOND, 59);
        calendario.set(Calendar.MILLISECOND, 999);
        return calendario.getTime();
    }

    private String valorFiltro(JComboBox<String> combo) {
        String valor = (String) combo.getSelectedItem();
        return valor == null || TODOS.equals(valor) ? null : valor;
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

    private static JTextField campo() {
        JTextField campo = new JTextField();
        Estilos.estilarCampo(campo);
        return campo;
    }

    private static JSpinner fecha(Date valorInicial) {
        JSpinner spinner = new JSpinner(new SpinnerDateModel(valorInicial, null, null, Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, FORMATO_FECHA);
        JTextField texto = editor.getTextField();
        Estilos.estilarCampo(texto);
        texto.setBackground(Color.WHITE);
        texto.setForeground(Color.BLACK);
        spinner.setEditor(editor);
        spinner.setPreferredSize(new Dimension(120, 32));
        spinner.setFont(FUENTE_ETIQUETA);
        return spinner;
    }

    private static JComboBox<String> combo(String[] valores) {
        String[] items = new String[valores.length + 1];
        items[0] = TODOS;
        System.arraycopy(valores, 0, items, 1, valores.length);
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setPreferredSize(new Dimension(180, 32));
        combo.setBackground(Color.WHITE);
        combo.setForeground(Color.BLACK);
        combo.setFont(FUENTE_ETIQUETA);
        return combo;
    }

    private static JButton boton(String texto, Color fondo, Color colorTexto) {
        JButton boton = new JButton(texto);
        boton.setBackground(fondo);
        boton.setForeground(colorTexto);
        boton.setFont(FUENTE_ETIQUETA);
        return boton;
    }
}
