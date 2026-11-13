package com.ues.procesamiento_pago.view.components;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.ues.procesamiento_pago.Model.Usuario;

public class TableUsuariosModel extends AbstractTableModel {
    private static final String[] COLUMNAS = {"#", "USUARIO", "ROL", "FECHA DE REGISTRO"};

    private final SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private List<Usuario> usuarios = new ArrayList<>();

    @Override
    public int getRowCount() {
        return usuarios.size();
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
        Usuario usuario = usuarios.get(fila);
        return switch (columna) {
            case 0 -> fila + 1;
            case 1 -> usuario.getNombre();
            case 2 -> usuario.getRol();
            default -> usuario.getCreatedAt() == null ? "-" : formatoFecha.format(usuario.getCreatedAt());
        };
    }

    public Usuario getUsuarioAt(int fila) {
        return usuarios.get(fila);
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
        fireTableDataChanged();
    }
}
