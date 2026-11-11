package com.ues.procesamiento_pago.Strategy.impl;

import java.math.BigDecimal;

import com.ues.procesamiento_pago.Strategy.MetodoPago;

public class PagoTarjeta extends MetodoPago {

    // el numero de la tarjeta debe tener entre 13 y 19 digitos
    private static final String FORMATO_NUMERO = "\\d{13,19}";

    private String numeroTarjeta;
    private String titular;

    public PagoTarjeta() {
        super("Pago con tarjeta");
    }

    public PagoTarjeta(String numeroTarjeta, String titular) {
        super("Pago con tarjeta");
        this.numeroTarjeta = normalizar(numeroTarjeta);
        this.titular = titular;
    }

    @Override
    protected boolean validarEspecifico(BigDecimal monto) {
        if(this.numeroTarjeta == null || this.numeroTarjeta.isBlank()){
            mensajeError = "El numero de la tarjeta esta vacio";
            return false;
        }
        if(!this.numeroTarjeta.matches(FORMATO_NUMERO)){
            mensajeError = "El numero de la tarjeta debe tener entre 13 y 19 digitos, sin letras ni simbolos.";
            return false;
        }
        if(this.titular == null || this.titular.isBlank()){
            mensajeError = "El nombre del titular esta vacio";
            return false;
        }
        return true;
    }

    @Override
    protected boolean ejecutarCobro(BigDecimal monto) {
        return true;
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    // permite escribir el numero con espacios o guiones
    private static String normalizar(String numero) {
        return numero == null ? null : numero.replaceAll("[\\s-]", "");
    }
}
