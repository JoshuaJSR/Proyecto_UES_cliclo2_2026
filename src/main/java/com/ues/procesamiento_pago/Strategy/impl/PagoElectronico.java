package com.ues.procesamiento_pago.Strategy.impl;

import java.math.BigDecimal;

import com.ues.procesamiento_pago.Strategy.MetodoPago;

public class PagoElectronico extends MetodoPago {
    private String correoCuenta;
    private String plataforma;

    public PagoElectronico() {
        super("Pago Electronico");
    }

    public PagoElectronico(String correCuenta, String plataforma) {
        super("Pago Electronico");
        this.correoCuenta = correCuenta;
        this.plataforma = plataforma;
    }

    @Override
    protected boolean validarEspecifico(BigDecimal monto) {
        if(this.correoCuenta == null || this.correoCuenta.isBlank()){
            mensajeError = "Debe especificar el correo de la cuenta";
            return false;
        }
        if(this.plataforma == null || this.plataforma.isBlank()){
            mensajeError = "Especifique la plataforma de pago";
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
