package com.ues.procesamiento_pago.Strategy;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.ues.procesamiento_pago.Model.EstadoPago;
import com.ues.procesamiento_pago.Model.Transaccion;
import com.ues.procesamiento_pago.Model.Venta;
import com.ues.procesamiento_pago.Strategy.impl.PagoEfectivo;
import com.ues.procesamiento_pago.Strategy.impl.PagoElectronico;
import com.ues.procesamiento_pago.Strategy.impl.PagoTarjeta;
import com.ues.procesamiento_pago.Strategy.impl.TransferenciaBancaria;

public abstract class MetodoPago implements IMetodoPago{
    protected String nombre;
    protected String mensajeError;
    public MetodoPago(String nombre) {
        this.nombre = nombre;
    }
    
    public static IMetodoPago crear(String tipo, BigDecimal montoRecibido, String... datos) {
        if (tipo == null) {
            return null;
        }
        if ("Efectivo".equals(tipo)) {
            return montoRecibido == null ? null : new PagoEfectivo(montoRecibido);
        }
        if (datos == null || datos.length < 2) {
            return null;
        }
        return switch (tipo) {
            case "Pago con tarjeta" -> new PagoTarjeta(datos[0], datos[1]);
            case "Pago Electronico" -> new PagoElectronico(datos[0], datos[1]);
            default -> new TransferenciaBancaria(datos[0], datos[1]);
        };
    }   

    @Override
    public boolean validar(BigDecimal monto) {
        if(monto == null || monto.compareTo(BigDecimal.ZERO) <= 0){ //validar que no sea 0
            mensajeError = "Monto inválido. Debe ser mayor que cero.";
            return false;
        }
        return validarEspecifico(monto);
    }

    @Override
    public Transaccion procesar(Venta venta) {
        mensajeError = null;
        if (!validar(venta.getTotal())) {
            return new Transaccion(venta.getId(), 
                    nombre, 
                    venta.getTotal(), 
                    EstadoPago.RECHAZADO);
        }

        if (ejecutarCobro(venta.getTotal())) {
            return new Transaccion(venta.getId(), 
                    nombre, venta.getTotal(), 
                    EstadoPago.PROCESADO);
        } else {
            if (mensajeError == null) {
                mensajeError = "El cobro no pudo completarse.";
            }
            return new Transaccion(venta.getId(), 
                    nombre, venta.getTotal(), 
                    EstadoPago.RECHAZADO);
        }
    
    }
    
    @Override
    public String getMensajeError() {
        return mensajeError;
    }

    @Override
    public String comprobante(Transaccion transaccion) {
        StringBuilder texto = new StringBuilder();
        texto.append("Método: ").append(transaccion.getMetodo()).append("\n");
        texto.append("Monto: $").append(formatear(transaccion.getMonto())).append("\n");
        texto.append("Estado: ").append(transaccion.getEstado());
        return texto.toString();
    }

    public static String formatear(BigDecimal monto) {
        return monto.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    //metodos abstractos
    protected abstract boolean validarEspecifico(BigDecimal monto);
    protected abstract boolean ejecutarCobro(BigDecimal monto);
   // protected abstract void mostrarComprobante(BigDecimal monto);
    
}