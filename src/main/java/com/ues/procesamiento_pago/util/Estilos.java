package com.ues.procesamiento_pago.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.*;

public class Estilos {
    public static final Color COLOR_FONDO = Color.DARK_GRAY;
    public static final Color COLOR_TEXTO_SECUNDARIO = new Color(160, 160, 160);
    public static final Color COLOR_VERDE = new Color(60, 220, 130);
    public static final Color COLOR_ROJO = new Color(220, 60, 60);
    public static final Font FUENTE_ETIQUETA = new Font("SansSerif", Font.BOLD, 12);
    public static final Font FUENTE_DESTACADA = new Font("SansSerif", Font.BOLD, 14);

    public static void estilarCampo(JTextField campo) {
        campo.setPreferredSize(new Dimension(campo.getPreferredSize().width, 35));
        campo.setBackground(new Color(25, 25, 25));
        campo.setForeground(Color.WHITE);
        campo.setCaretColor(Color.WHITE);
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    public static void estilarCampo(JTextField campo, int anchoFijo) {
        estilarCampo(campo);
        campo.setPreferredSize(new Dimension(anchoFijo, 35));
    }

    public static JLabel etiqueta(String texto, boolean destacada) {
        JLabel l = new JLabel(texto);
        l.setForeground(destacada ? Color.WHITE : COLOR_TEXTO_SECUNDARIO);
        l.setFont(destacada ? FUENTE_DESTACADA : FUENTE_ETIQUETA);
        return l;
    }

    public static JButton boton(String texto, Color fondo, Color colorTexto) {
        JButton b = new JButton(texto);
        b.setBackground(fondo);
        b.setForeground(colorTexto);
        b.setFont(FUENTE_ETIQUETA);
        return b;
    }

    public static void estilarTabla(JTable tabla, int[] anchos) {
        tabla.setBackground(new Color(25, 25, 25));
        tabla.setForeground(Color.WHITE);
        tabla.setGridColor(new Color(60, 60, 60));
        tabla.setSelectionBackground(new Color(45, 120, 85));
        tabla.setSelectionForeground(Color.WHITE);
        tabla.setRowHeight(24);
        tabla.getTableHeader().setBackground(Color.BLACK);
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.getTableHeader().setFont(FUENTE_ETIQUETA);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
    }
}