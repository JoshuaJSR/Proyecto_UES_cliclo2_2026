package com.ues.procesamiento_pago.util;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JTextField;

public class Estilos{
    private static final Color COLOR_BORDE_CAMPO = new Color(60, 60, 60);

    public static void estilarCampo(JTextField campo) {
        campo.setPreferredSize(new Dimension(campo.getPreferredSize().width, 35));
        campo.setBackground(new Color(25, 25, 25));
        campo.setForeground(Color.WHITE);
        campo.setCaretColor(Color.WHITE);
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDE_CAMPO, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    public static void estilarCampo(JTextField campo, int anchoFijo) {
        estilarCampo(campo);
        campo.setPreferredSize(new Dimension(anchoFijo, 35));
    }
}