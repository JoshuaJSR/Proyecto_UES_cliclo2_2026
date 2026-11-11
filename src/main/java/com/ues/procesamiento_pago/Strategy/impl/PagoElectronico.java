package com.ues.procesamiento_pago.Strategy.impl;

import java.math.BigDecimal;

import com.ues.procesamiento_pago.Strategy.MetodoPago;

public class PagoElectronico extends MetodoPago {
    // formato del correo
    private static final String FORMATO_CORREO = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$";

    private String correoCuenta;
    private String plataforma;

    public PagoElectronico() {
        super("Pago Electronico");
    }

    public PagoElectronico(String correCuenta, String plataforma) {
        super("Pago Electronico");
        this.correoCuenta = correCuenta == null ? null : correCuenta.trim();
        this.plataforma = plataforma;
    }

    @Override
    protected boolean validarEspecifico(BigDecimal monto) {
        if(this.correoCuenta == null || this.correoCuenta.isBlank()){
            mensajeError = "Debe especificar el correo de la cuenta";
            return false;
        }
        if(!this.correoCuenta.matches(FORMATO_CORREO)){
            mensajeError = "El correo de la cuenta no tiene un formato valido (ejemplo: usuario@dominio.com).";
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
