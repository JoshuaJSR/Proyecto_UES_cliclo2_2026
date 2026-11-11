package com.ues.procesamiento_pago.Strategy.impl;

import java.math.BigDecimal;

import com.ues.procesamiento_pago.Strategy.MetodoPago;

public class TransferenciaBancaria extends MetodoPago {

    // las cuentas deben ser numericas, entre 6 y 24 digitos
    private static final String FORMATO_CUENTA = "\\d{6,24}";

    private String cuentaOrigen;
    private String cuentaFinal;

    public TransferenciaBancaria() {
        super("Transferencia Bancaria");
    }

    public TransferenciaBancaria(String cuentaOrigen, String cuentaFinal) {
        super("Transferencia Bancaria");
        this.cuentaOrigen = normalizar(cuentaOrigen);
        this.cuentaFinal = normalizar(cuentaFinal);
    }

    @Override
    protected boolean validarEspecifico(BigDecimal monto) {
        if(this.cuentaOrigen == null || this.cuentaOrigen.isBlank()){
            mensajeError = "La cuenta origen no puede estar vacia";
            return false;
        }
        if(!this.cuentaOrigen.matches(FORMATO_CUENTA)){
            mensajeError = "La cuenta origen debe tener entre 6 y 24 digitos, sin letras ni simbolos.";
            return false;
        }
        if(this.cuentaFinal == null || this.cuentaFinal.isBlank()){
            mensajeError = "La cuenta destino no puede estar vacia";
            return false;
        }
        if(!this.cuentaFinal.matches(FORMATO_CUENTA)){
            mensajeError = "La cuenta destino debe tener entre 6 y 24 digitos, sin letras ni simbolos.";
            return false;
        }
        if(this.cuentaOrigen.equals(this.cuentaFinal)){
            mensajeError = "La cuenta origen y la cuenta destino deben ser diferentes.";
            return false;
        }
        return true;
    }

    @Override
    protected boolean ejecutarCobro(BigDecimal monto) {
        //System.out.println("Pago realizado con exito, con monto: $" + monto);
        return true;
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    // permite escribir la cuenta con espacios o guiones
    private static String normalizar(String cuenta) {
        return cuenta == null ? null : cuenta.replaceAll("[\\s-]", "");
    }
}
