package com.ues.procesamiento_pago.Strategy.impl;

import java.math.BigDecimal;

import com.ues.procesamiento_pago.Model.Transaccion;
import com.ues.procesamiento_pago.Strategy.MetodoPago;

public class PagoEfectivo extends MetodoPago {

    private BigDecimal montoRecibido;

    public PagoEfectivo() {
        super("Efectivo");
    }

    public PagoEfectivo(BigDecimal montoRecibido) {
        super("Efectivo");
        this.montoRecibido = montoRecibido;
    }

    @Override
    protected boolean validarEspecifico(BigDecimal monto) {
        if (this.montoRecibido == null || this.montoRecibido.compareTo(monto) < 0) {
            mensajeError = "El dinero entregado es insuficiente.";
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
        return this.nombre;
    }

    @Override
    public String comprobante(Transaccion transaccion) {
        return super.comprobante(transaccion)
                + "\nMonto entregado: $" + formatear(montoRecibido)
                + "\nCambio: $" + formatear(calcularCambio(transaccion.getMonto()));
    }

    //Calcula el cambio restando el total de la venta al monto recibido
    public BigDecimal calcularCambio(BigDecimal montoTotal){
        if(montoRecibido == null || montoTotal == null){
            return BigDecimal.ZERO;
        }
        return montoRecibido.subtract(montoTotal);
    }
}
