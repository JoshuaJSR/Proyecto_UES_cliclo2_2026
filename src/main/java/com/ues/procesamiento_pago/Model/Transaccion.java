package com.ues.procesamiento_pago.Model;

import java.math.BigDecimal;
import java.util.Date;

import org.bson.Document;
import org.bson.types.ObjectId;

public class Transaccion {
    private ObjectId id;
    private int idVenta;
    private String metodo;
    private BigDecimal monto;
    private Date fecha;
    private EstadoPago estado;
    private String mensaje;

    public Transaccion(int idVenta, String metodo, BigDecimal monto, EstadoPago estado) {
        this.idVenta = idVenta;
        this.metodo = metodo;
        this.monto = monto;
        this.fecha = new Date();
        this.estado = estado;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public String getMetodo() {
        return metodo;
    }

    public void setMetodo(String metodo) {
        this.metodo = metodo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public void setEstado(EstadoPago estado) {
        this.estado = estado;
    }

    public String getMensaje() {
        return mensaje;
    }

    // motivo por el que la transaccion quedo rechazada o pendiente
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    // Mapea la transaccion a un Document de la coleccion transacciones
    public Document toDocument() {
        Document documento = new Document("idVenta", getIdVenta())
                .append("metodo", getMetodo())
                .append("monto", getMonto())
                .append("fecha", getFecha())
                .append("estado", getEstado() == null ? null : getEstado().name());
        if (getMensaje() != null) {
            documento.append("mensaje", getMensaje());
        }
        return documento;
    }
}
