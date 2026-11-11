package com.ues.procesamiento_pago.view.components;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.ues.procesamiento_pago.Model.Venta;
import com.ues.procesamiento_pago.Strategy.MetodoPago;

public class TableVentasModel extends AbstractTableModel {
    private static final String[] COLUMNAS = {"ITEM", "NOMBRE", "PRECIO", "CANTIDAD", "TOTAL"};

    private final List<Venta> ventas;

    public TableVentasModel(List<Venta> ventas) {
        this.ventas = ventas;
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
    public Object getValueAt(int fila, int columna) {
        Venta venta = ventas.get(fila);
        return switch (columna) {
            case 0 -> fila + 1;
            case 1 -> venta.getNombre();
            case 2 -> "$" + MetodoPago.formatear(venta.getPrecio());
            case 3 -> venta.getCantidad();
            default -> "$" + MetodoPago.formatear(venta.getTotal());
        };
    }

    // metodo para recuperar la venta seleccionada por el indice de la fila
    public Venta getVentaAt(int fila) {
        return ventas.get(fila);
    }

    public void agregar(Venta venta) {
        ventas.add(venta);
        fireTableRowsInserted(ventas.size() - 1, ventas.size() - 1);
    }

    // metodo para actualizar una venta existente en una fila especifica
    public void actualizar(int fila, Venta venta) {
        ventas.set(fila, venta);
        fireTableRowsUpdated(fila, fila);
    }

    public void limpiar() {
        ventas.clear();
        fireTableDataChanged();
    }
}