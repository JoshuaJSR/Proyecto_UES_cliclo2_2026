
package com.ues.procesamiento_pago.Strategy.impl;

import java.math.BigDecimal;

import com.ues.procesamiento_pago.Strategy.MetodoPago;
public class TransferenciaBancaria extends MetodoPago {
    private String cuentaOrigen;
    private String cuentaFinal;
    
    public TransferenciaBancaria() {
        super("Transferencia Bancaria");
    }

    public TransferenciaBancaria(String cuentaOrigen, String cuentaFinal) {
        super("Transferencia Bancaria");
        this.cuentaOrigen = cuentaOrigen;
        this.cuentaFinal = cuentaFinal;
    }
    
    

    @Override
    protected boolean validarEspecifico(BigDecimal monto) {
        if(this.cuentaOrigen == null || this.cuentaOrigen.isBlank()){
            mensajeError = "La cuenta origen no puede estar vacia";
            return false;
        }
        if(this.cuentaFinal == null || this.cuentaFinal.isBlank()){
            mensajeError = "La cuenta destino no puede estar vacia";
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
    
}
