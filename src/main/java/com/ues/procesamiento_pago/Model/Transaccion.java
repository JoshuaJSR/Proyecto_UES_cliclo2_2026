
package com.ues.procesamiento_pago.Model;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class Transaccion {
    private int id;
    private int idVenta;
    private String metodo;
    private BigDecimal monto;
    private LocalDateTime fecha;
    private EstadoPago estado;

    public Transaccion(int idVenta, String metodo, BigDecimal monto, EstadoPago estado) {
        this.idVenta = idVenta;
        this.metodo = metodo;
        this.monto = monto;
        this.fecha = LocalDateTime.now();
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public String getMetodo() {
        return metodo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public LocalDateTime getFehca() {
        return fecha;
    }

    public EstadoPago getEstado() {
        return estado;
    }
}
