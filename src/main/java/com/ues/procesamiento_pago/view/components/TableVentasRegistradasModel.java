package com.ues.procesamiento_pago.view.components;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.bson.Document;

import com.ues.procesamiento_pago.Model.Venta;
import com.ues.procesamiento_pago.Strategy.MetodoPago;

public class TableVentasRegistradasModel extends AbstractTableModel {
    private static final String[] COLUMNAS = {
        "N° VENTA", "PRODUCTO", "PRECIO", "CANTIDAD", "TOTAL", "ESTADO", "MÉTODO DE PAGO", "FECHA", "USUARIO"
    };
    private static final SimpleDateFormat FORMATO_FECHA = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private List<Document> ventas = new ArrayList<>();

    public void setVentas(List<Document> ventas) {
        this.ventas = ventas == null ? new ArrayList<>() : ventas;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return ventas.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNAS.length;
    }

    @Override
    public String getColumnName(int columna) {
        return COLUMNAS[columna];
    }

    @Override
    public boolean isCellEditable(int fila, int columna) {
        return false;
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        Document documento = ventas.get(fila);
        Venta venta = Venta.fromDocument(documento);
        return switch (columna) {
            case 0 -> documento.getInteger("id") == null ? "-" : documento.getInteger("id");
            case 1 -> venta.getNombre();
            case 2 -> "$" + MetodoPago.formatear(venta.getPrecio());
            case 3 -> venta.getCantidad();
            case 4 -> "$" + MetodoPago.formatear(venta.getTotal());
            case 5 -> documento.getString("estado");
            case 6 -> documento.getString("metodoPago");
            case 7 -> {
                Date fecha = documento.getDate("fecha");
                yield fecha == null ? "" : FORMATO_FECHA.format(fecha);
            }
            default -> documento.getString("usuario");
        };
    }

    // documento completo del item de la fila indicada
    public Document getVentaAt(int fila) {
        return ventas.get(fila);
    }
}
