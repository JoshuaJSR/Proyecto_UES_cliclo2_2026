package com.ues.procesamiento_pago.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Arrays;

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
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import org.bson.Document;

import com.mongodb.MongoException;
import com.ues.procesamiento_pago.Model.EstadoPago;
import com.ues.procesamiento_pago.Model.Venta;
import com.ues.procesamiento_pago.Strategy.MetodoPago;
import com.ues.procesamiento_pago.config.MongoDBConnection;
import com.ues.procesamiento_pago.dao.TransaccionDAO;
import com.ues.procesamiento_pago.dao.VentaDAO;
import com.ues.procesamiento_pago.util.Estilos;

public class ConsultaMovimientosForm extends JFrame {
    private static final String TODOS = "Todos";
    private static final String FORMATO_FECHA = "dd/MM/yyyy";

    private static final String[] METODOS = {
        "Efectivo", "Pago con tarjeta", "Pago Electronico", "Transferencia Bancaria"
    };

    private static final String[] COLUMNAS_TRANSACCIONES = {
        "ID DE LA VENTA", "FECHA", "ESTADO", "MÉTODO DE PAGO", "TOTAL"
    };

    private static final String[] COLUMNAS_DETALLE = {
        "ITEM", "NOMBRE DEL PRODUCTO", "CANTIDAD", "PRECIO UNITARIO", "TOTAL"
    };

    private JSpinner spDesde;
    private JSpinner spHasta;
    private JTextField txtNumeroVenta;
    private JComboBox<String> comboMetodo;
    private JComboBox<String> comboEstado;
    private JButton btnBuscar;

    private DefaultTableModel modeloTablaTransacciones;
    private JTable tablaTransacciones;
    private DefaultTableModel modeloTablaDetalle;
    private JTable tablaDetalle;
    private JLabel lblResumen;

    private final VentaDAO dao;
    private final TransaccionDAO transaccionDAO;
    private List<Document> transacciones = new ArrayList<>();

    private static final Color COLOR_FONDO = Color.DARK_GRAY;
    private static final Color COLOR_TEXTO_SECUNDARIO = new Color(160, 160, 160);
    private static final Color COLOR_VERDE = new Color(60, 220, 130);
    private static final Color COLOR_ROJO = new Color(220, 60, 60);
    private static final Font FUENTE_ETIQUETA = new Font("SansSerif", Font.BOLD, 12);
    private static final Font FUENTE_TITULO_GRID = new Font("SansSerif", Font.BOLD, 13);

    public ConsultaMovimientosForm(MongoDBConnection mongoDB) {
        if (mongoDB != null && mongoDB.getDatabase() != null) {
            this.dao = new VentaDAO(mongoDB);
            this.transaccionDAO = new TransaccionDAO(mongoDB);
        } else {
            this.dao = null;
            this.transaccionDAO = null;
            JOptionPane.showMessageDialog(null, "No hay una conexión activa a MongoDB.");
        }

        setTitle("Consultas de movimientos de venta");
        setSize(1120, 820);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        add(crearContenido());
        conectarEventos();

        if (dao != null && transaccionDAO != null) {
            buscar();
        }
    }

    private JPanel crearContenido() {
        JPanel panel = crearPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 18, 16, 18));

        JLabel lblTitulo = new JLabel("CONSULTAS DE MOVIMIENTOS DE VENTA", JLabel.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblTitulo.setForeground(Color.WHITE);

        agregar(panel, lblTitulo, 0, 0, new Insets(0, 0, 10, 0));
        agregar(panel, crearPanelFiltros(), 1, 0, new Insets(0, 0, 6, 0));
        agregar(panel, crearPanelGrids(), 2, 1, new Insets(0, 0, 0, 0));
        return panel;
    }

    private JPanel crearPanelFiltros() {
        JPanel panel = crearPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        spDesde = fecha(primerDiaDelMes());
        spHasta = fecha(fechaHoy());
        txtNumeroVenta = campo();
        txtNumeroVenta.setPreferredSize(new Dimension(80, 32));
        comboMetodo = combo(METODOS);
        comboEstado = combo(Arrays.stream(EstadoPago.values()).map(Enum::name).toArray(String[]::new));

        String[] etiquetas = {"DESDE:", "HASTA:", "N° DE VENTA:", "MÉTODO DE PAGO:", "ESTADO:"};
        Component[] campos = {spDesde, spHasta, txtNumeroVenta, comboMetodo, comboEstado};

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
            gbcCampo.anchor = GridBagConstraints.WEST;
            gbcCampo.insets = new Insets(0, 0, 0, 10);
            if (i == 0) {
                gbcCampo.weightx = 0.0;
            }
            panel.add(campos[i], gbcCampo);
        }

        btnBuscar = boton("BUSCAR", COLOR_VERDE, Color.BLACK);
        GridBagConstraints gbcBoton = new GridBagConstraints();
        gbcBoton.gridx = etiquetas.length;
        gbcBoton.gridy = 1;
        gbcBoton.anchor = GridBagConstraints.SOUTHWEST;
        gbcBoton.insets = new Insets(0, 4, 0, 0);
        panel.add(btnBuscar, gbcBoton);

        JButton btnCerrar = boton("Cerrar", COLOR_ROJO, Color.WHITE);
        GridBagConstraints gbcCerrar = new GridBagConstraints();
        gbcCerrar.gridx = etiquetas.length + 1;
        gbcCerrar.gridy = 1;
        gbcCerrar.weightx = 1.0;
        gbcCerrar.anchor = GridBagConstraints.EAST;
        panel.add(btnCerrar, gbcCerrar);
        btnCerrar.addActionListener(e -> dispose());

        return panel;
    }

    private JPanel crearPanelGrids() {
        JPanel panel = crearPanel(new GridBagLayout());

        modeloTablaTransacciones = modeloNoEditable(COLUMNAS_TRANSACCIONES);
        tablaTransacciones = tabla(modeloTablaTransacciones, new int[] {160, 200, 130, 160, 150});

        modeloTablaDetalle = modeloNoEditable(COLUMNAS_DETALLE);
        tablaDetalle = tabla(modeloTablaDetalle, new int[] {70, 320, 110, 160, 150});

        JPanel panelTransacciones = panelConTitulo("DATOS DE LA TRANSACCION", tablaTransacciones);
        JPanel panelDetalle = panelConTitulo("DETALLE DE LA TRANSACCION SELECCIONADO", tablaDetalle);

        lblResumen = new JLabel(" ");
        lblResumen.setForeground(COLOR_TEXTO_SECUNDARIO);
        lblResumen.setFont(FUENTE_ETIQUETA);

        GridBagConstraints gbcTransacciones = new GridBagConstraints();
        gbcTransacciones.gridx = 0;
        gbcTransacciones.gridy = 0;
        gbcTransacciones.weightx = 1.0;
        gbcTransacciones.weighty = 0.5;
        gbcTransacciones.fill = GridBagConstraints.BOTH;
        panel.add(panelTransacciones, gbcTransacciones);

        GridBagConstraints gbcResumen = new GridBagConstraints();
        gbcResumen.gridx = 0;
        gbcResumen.gridy = 1;
        gbcResumen.weightx = 1.0;
        gbcResumen.fill = GridBagConstraints.HORIZONTAL;
        gbcResumen.insets = new Insets(4, 4, 4, 0);
        panel.add(lblResumen, gbcResumen);

        GridBagConstraints gbcDetalle = new GridBagConstraints();
        gbcDetalle.gridx = 0;
        gbcDetalle.gridy = 2;
        gbcDetalle.weightx = 1.0;
        gbcDetalle.weighty = 0.5;
        gbcDetalle.fill = GridBagConstraints.BOTH;
        panel.add(panelDetalle, gbcDetalle);

        return panel;
    }

    private void conectarEventos() {
        btnBuscar.addActionListener(e -> buscar());
        txtNumeroVenta.addActionListener(e -> buscar());
        // al seleccionar una transaccion se carga su detalle automaticamente
        tablaTransacciones.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarDetalleSeleccionado();
            }
        });
    }

    private void buscar() {
        if (dao == null || transaccionDAO == null) {
            JOptionPane.showMessageDialog(this, "No hay conexión con la base de datos.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // consultar por rango de fechas
        Date desde = inicioDelDia((Date) spDesde.getValue());
        Date hasta = finDelDia((Date) spHasta.getValue());
        if (desde.after(hasta)) {
            JOptionPane.showMessageDialog(this, "La fecha 'Desde' no puede ser posterior a la fecha 'Hasta'.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // consultar la venta asociada por el numero de venta
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

        String metodo = valorFiltro(comboMetodo);
        String estado = valorFiltro(comboEstado);

        try {
            transacciones = transaccionDAO.listarPorRango(desde, hasta);
        } catch (MongoException ex) {
            JOptionPane.showMessageDialog(this, "Error al consultar las transacciones: " + ex.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // aplicar filtros adicionales en memoria
        List<Document> filtradas = new ArrayList<>();
        for (Document transaccion : transacciones) {
            Integer idVenta = transaccion.getInteger("idVenta");
            if (numeroVenta != null && (idVenta == null || !idVenta.equals(numeroVenta))) {
                continue;
            }
            if (estado != null) {
                String est = transaccion.getString("estado");
                if (!estado.equals(est)) {
                    continue;
                }
            }
            if (metodo != null) {
                String met = transaccion.getString("metodo");
                if (!metodo.equals(met)) {
                    continue;
                }
            }
            filtradas.add(transaccion);
        }
        transacciones = filtradas;

        modeloTablaTransacciones.setRowCount(0);
        modeloTablaDetalle.setRowCount(0);

        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (Document transaccion : transacciones) {
            Integer idVenta = transaccion.getInteger("idVenta");
            Date fecha = transaccion.getDate("fecha");
            modeloTablaTransacciones.addRow(new Object[] {
                idVenta == null ? "-" : idVenta,
                fecha == null ? "-" : formato.format(fecha),
                transaccion.getString("estado"),
                transaccion.getString("metodo"),
                "$" + MetodoPago.formatear(Venta.aBigDecimal(transaccion.get("monto")))
            });
        }

        lblResumen.setText(transacciones.isEmpty()
                ? "No hay transacciones en el período seleccionado."
                : transacciones.size() + " transacción(es) encontradas. Seleccione una para ver su detalle.");

        if (!transacciones.isEmpty()) {
            tablaTransacciones.setRowSelectionInterval(0, 0);
        }
    }

    //al elegir una transaccion se listan los items de su venta
    private void mostrarDetalleSeleccionado() {
        modeloTablaDetalle.setRowCount(0);

        int fila = tablaTransacciones.getSelectedRow();
        if (fila < 0 || fila >= transacciones.size()) {
            return;
        }

        Integer idVenta = transacciones.get(fila).getInteger("idVenta");
        if (idVenta == null) {
            return;
        }

        try {
            List<Document> items = dao.listar(null, idVenta, null, null, null, null);
            int numero = 1;
            for (Document item : items) {
                modeloTablaDetalle.addRow(new Object[] {
                    numero++,
                    item.getString("nombre"),
                    item.getInteger("cantidad", 0),
                    "$" + MetodoPago.formatear(Venta.aBigDecimal(item.get("precio"))),
                    "$" + MetodoPago.formatear(Venta.aBigDecimal(item.get("total")))
                });
            }
        } catch (MongoException ex) {
            JOptionPane.showMessageDialog(this, "Error al consultar el detalle de la venta: " + ex.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static DefaultTableModel modeloNoEditable(String[] columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    private static JTable tabla(DefaultTableModel modelo, int[] anchos) {
        JTable tabla = new JTable(modelo);
        tabla.setBackground(new Color(25, 25, 25));
        tabla.setForeground(Color.WHITE);
        tabla.setGridColor(new Color(60, 60, 60));
        tabla.setSelectionBackground(new Color(45, 120, 85));
        tabla.setSelectionForeground(Color.WHITE);
        tabla.setRowHeight(24);
        tabla.getTableHeader().setBackground(Color.BLACK);
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.getTableHeader().setFont(FUENTE_ETIQUETA);
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        return tabla;
    }

    private static JPanel panelConTitulo(String titulo, JTable tabla) {
        JPanel panel = crearPanel(new BorderLayout());
        TitledBorder borde = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70), 1), titulo);
        borde.setTitleColor(Color.WHITE);
        borde.setTitleFont(FUENTE_TITULO_GRID);
        panel.setBorder(BorderFactory.createCompoundBorder(borde,
                BorderFactory.createEmptyBorder(2, 6, 6, 6)));
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
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

    private String valorFiltro(JComboBox<String> combo) {
        String valor = (String) combo.getSelectedItem();
        return valor == null || TODOS.equals(valor) ? null : valor;
    }

    private static JButton boton(String texto, Color fondo, Color colorTexto) {
        JButton boton = new JButton(texto);
        boton.setBackground(fondo);
        boton.setForeground(colorTexto);
        boton.setFont(FUENTE_ETIQUETA);
        return boton;
    }
}
