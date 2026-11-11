package com.ues.procesamiento_pago.Strategy.impl;

import java.math.BigDecimal;

import com.ues.procesamiento_pago.Strategy.MetodoPago;

public class PagoTarjeta extends MetodoPago {

    private String numeroTarjeta;
    private String titular;

    public PagoTarjeta() {
        super("Pago con tarjeta");
    }

    public PagoTarjeta(String numeroTarjeta, String titular) {
        super("Pago con tarjeta");
        this.numeroTarjeta = numeroTarjeta;
        this.titular = titular;
    }

    @Override
    protected boolean validarEspecifico(BigDecimal monto) {
        if(this.numeroTarjeta == null || this.numeroTarjeta.isBlank()){
            mensajeError = "El numero de la tarjeta esta vacio";
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
}
