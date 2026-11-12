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
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.bson.Document;

import com.mongodb.MongoException;
import com.ues.procesamiento_pago.Model.EstadoPago;
import com.ues.procesamiento_pago.Model.Venta;
import com.ues.procesamiento_pago.Strategy.MetodoPago;
import com.ues.procesamiento_pago.config.MongoDBConnection;
import com.ues.procesamiento_pago.dao.TransaccionDAO;

public class ResumenPagosForm extends JFrame {
    private static final String[] METODOS = {
        "Efectivo", "Pago con tarjeta", "Pago Electronico", "Transferencia Bancaria"
    };

    private static final String[] COLUMNAS = {
        "MÉTODO DE PAGO", "TRANSACCIONES", "APROBADAS", "IMPORTE APROBADO",
        "RECHAZADAS", "IMPORTE RECHAZADO", "PENDIENTES"
    };

    private final TransaccionDAO transaccionDAO;

    private DefaultTableModel modeloTabla;
    private JTable tablaResumen;
    private JLabel lblIngresos;
    private JLabel lblDetalle;

    private static final Color COLOR_FONDO = Color.DARK_GRAY;
    private static final Color COLOR_TEXTO_SECUNDARIO = new Color(160, 160, 160);
    private static final Color COLOR_VERDE = new Color(60, 220, 130);
    private static final Color COLOR_ROJO = new Color(220, 60, 60);
    private static final Font FUENTE_ETIQUETA = new Font("SansSerif", Font.BOLD, 12);

    public ResumenPagosForm(MongoDBConnection mongoDB) {
        if (mongoDB != null && mongoDB.getDatabase() != null) {
            this.transaccionDAO = new TransaccionDAO(mongoDB);
        } else {
            this.transaccionDAO = null;
            JOptionPane.showMessageDialog(null, "No hay una conexión activa a MongoDB.");
        }

        setTitle("Resumen de pagos");
        setSize(1020, 410);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        add(crearContenido());

        if (transaccionDAO != null) {
            cargarResumen();
        }
    }

    private JPanel crearContenido() {
        JPanel panel = crearPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel lblTitulo = new JLabel("RESUMEN DE PAGOS", JLabel.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);

        agregar(panel, lblTitulo, 0, 0, new Insets(0, 0, 12, 0));
        agregar(panel, crearPanelTabla(), 1, 0, new Insets(0, 0, 12, 0));
        agregar(panel, crearPanelBotones(), 2, 0, new Insets(0, 0, 0, 0));
        return panel;
    }

    private JPanel crearPanelTabla() {
        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaResumen = new JTable(modeloTabla) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int fila, int columna) {
                Component componente = super.prepareRenderer(renderer, fila, columna);
                if (fila == getRowCount() - 1) {
                    componente.setFont(componente.getFont().deriveFont(Font.BOLD));
                    componente.setBackground(new Color(45, 45, 45));
                } else {
                    componente.setFont(componente.getFont().deriveFont(Font.PLAIN));
                }
                return componente;
            }
        };
        
        tablaResumen.setBackground(new Color(25, 25, 25));
        tablaResumen.setForeground(Color.WHITE);
        tablaResumen.setGridColor(new Color(60, 60, 60));
        tablaResumen.setSelectionBackground(new Color(45, 120, 85));
        tablaResumen.setSelectionForeground(Color.WHITE);
        tablaResumen.setRowHeight(28);
        tablaResumen.getTableHeader().setBackground(Color.BLACK);
        tablaResumen.getTableHeader().setForeground(Color.WHITE);
        tablaResumen.getTableHeader().setFont(FUENTE_ETIQUETA);
        tablaResumen.getTableHeader().setReorderingAllowed(false);
        tablaResumen.getTableHeader().setResizingAllowed(false);

        DefaultTableCellRenderer tcrDerecha = new DefaultTableCellRenderer();
        tcrDerecha.setHorizontalAlignment(JLabel.RIGHT);
        for (int i = 1; i < COLUMNAS.length; i++) {
            tablaResumen.getColumnModel().getColumn(i).setCellRenderer(tcrDerecha);
        }

        int[] anchos = {210, 115, 105, 140, 110, 140, 105};
        for (int i = 0; i < anchos.length; i++) {
            TableColumn columna = tablaResumen.getColumnModel().getColumn(i);
            columna.setMinWidth(anchos[i]);
            columna.setMaxWidth(anchos[i]);
            columna.setPreferredWidth(anchos[i]);
        }

        int alturaFijaTabla = tablaResumen.getTableHeader().getPreferredSize().height + (tablaResumen.getRowHeight() * 5) + 3;
        
        JScrollPane scrollPane = new JScrollPane(tablaResumen);
        scrollPane.setPreferredSize(new Dimension(935, alturaFijaTabla));
        scrollPane.setMinimumSize(new Dimension(935, alturaFijaTabla));
        scrollPane.setMaximumSize(new Dimension(935, alturaFijaTabla));

        JPanel panel = crearPanel(new BorderLayout(0, 6));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel panelResumen = crearPanel(new BorderLayout(0, 2));
        lblIngresos = new JLabel(" ");
        lblIngresos.setForeground(COLOR_VERDE);
        lblIngresos.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblDetalle = new JLabel(" ");
        lblDetalle.setForeground(COLOR_TEXTO_SECUNDARIO);
        lblDetalle.setFont(FUENTE_ETIQUETA);
        panelResumen.add(lblIngresos, BorderLayout.NORTH);
        panelResumen.add(lblDetalle, BorderLayout.SOUTH);
        panel.add(panelResumen, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel crearPanelBotones() {
        JPanel panel = crearPanel(new BorderLayout());
        JPanel izquierda = crearPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton btnActualizar = boton("ACTUALIZAR", COLOR_VERDE, Color.BLACK);
        btnActualizar.addActionListener(e -> cargarResumen());
        izquierda.add(btnActualizar);

        JButton btnCerrar = boton("Cerrar", COLOR_ROJO, Color.WHITE);
        btnCerrar.addActionListener(e -> dispose());

        panel.add(izquierda, BorderLayout.WEST);
        panel.add(btnCerrar, BorderLayout.EAST);
        return panel;
    }

    private void cargarResumen() {
        if (transaccionDAO == null) {
            JOptionPane.showMessageDialog(this, "No hay conexión con la base de datos.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Map<String, Acumulado> acumulados = new LinkedHashMap<>();
        for (String metodo : METODOS) {
            acumulados.put(metodo, new Acumulado());
        }

        try {
            for (Document fila : transaccionDAO.resumenPorMetodoYEstado()) {
                Document grupo = (Document) fila.get("_id");
                String metodo = grupo == null || grupo.getString("metodo") == null
                        ? "Sin método" : grupo.getString("metodo");
                String estado = grupo == null ? null : grupo.getString("estado");
                int cantidad = fila.getInteger("cantidad", 0);
                Object importe = fila.get("importe");
                BigDecimal monto = importe == null ? BigDecimal.ZERO : Venta.aBigDecimal(importe);

                Acumulado acumulado = acumulados.computeIfAbsent(metodo, clave -> new Acumulado());
                if (EstadoPago.PROCESADO.name().equals(estado)) {
                    acumulado.procesadas += cantidad;
                    acumulado.importeAprobado = acumulado.importeAprobado.add(monto);
                } else if (EstadoPago.RECHAZADO.name().equals(estado)) {
                    acumulado.rechazadas += cantidad;
                    acumulado.importeRechazado = acumulado.importeRechazado.add(monto);
                } else {
                    acumulado.pendientes += cantidad;
                }
            }
        } catch (MongoException ex) {
            JOptionPane.showMessageDialog(this, "Error al consultar la base de datos: " + ex.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        mostrarResumen(acumulados);
    }

    private void mostrarResumen(Map<String, Acumulado> acumulados) {
        modeloTabla.setRowCount(0);

        BigDecimal aprobado = BigDecimal.ZERO;
        BigDecimal rechazado = BigDecimal.ZERO;
        int transacciones = 0;
        int procesadas = 0;
        int rechazadas = 0;
        int pendientes = 0;

        for (Map.Entry<String, Acumulado> entrada : acumulados.entrySet()) {
            Acumulado acumulado = entrada.getValue();
            transacciones += acumulado.total();
            procesadas += acumulado.procesadas;
            rechazadas += acumulado.rechazadas;
            pendientes += acumulado.pendientes;
            aprobado = aprobado.add(acumulado.importeAprobado);
            rechazado = rechazado.add(acumulado.importeRechazado);

            modeloTabla.addRow(new Object[] {
                entrada.getKey(),
                acumulado.total(),
                acumulado.procesadas,
                "$" + MetodoPago.formatear(acumulado.importeAprobado),
                acumulado.rechazadas,
                "$" + MetodoPago.formatear(acumulado.importeRechazado),
                acumulado.pendientes
            });
        }

        modeloTabla.addRow(new Object[] {
            "TOTAL",
            transacciones,
            procesadas,
            "$" + MetodoPago.formatear(aprobado),
            rechazadas,
            "$" + MetodoPago.formatear(rechazado),
            pendientes
        });

        lblIngresos.setText("Ingresos aprobados: $" + MetodoPago.formatear(aprobado));
        lblDetalle.setText(transacciones == 0
                ? "No hay transacciones registradas."
                : procesadas + " aprobadas, " + rechazadas + " rechazadas y "
                        + pendientes + " pendientes. Importe rechazado: $" + MetodoPago.formatear(rechazado) + ".");
    }

    private static final class Acumulado {
        private int procesadas;
        private int rechazadas;
        private int pendientes;
        private BigDecimal importeAprobado = BigDecimal.ZERO;
        private BigDecimal importeRechazado = BigDecimal.ZERO;

        private int total() {
            return procesadas + rechazadas + pendientes;
        }
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

    private static JButton boton(String texto, Color fondo, Color colorTexto) {
        JButton boton = new JButton(texto);
        boton.setBackground(fondo);
        boton.setForeground(colorTexto);
        boton.setFont(FUENTE_ETIQUETA);
        return boton;
    }
}