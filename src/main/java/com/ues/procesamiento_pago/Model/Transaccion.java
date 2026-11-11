package com.ues.procesamiento_pago.Model;

import java.math.BigDecimal;
import java.util.Date;

import org.bson.Document;
import org.bson.types.ObjectId;

public class Transaccion {
    private ObjectId id;
    private final int idVenta;
    private final String metodo;
    private final BigDecimal monto;
    private final Date fecha;
    private final EstadoPago estado;
    private String mensaje;

    public Transaccion(int idVenta, String metodo, BigDecimal monto, EstadoPago estado) {
        this.idVenta = idVenta;
        this.metodo = metodo;
        this.monto = monto;
        this.fecha = new Date();
        this.estado = estado;
    }

    // identificador que asigna MongoDB al guardar la transaccion
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    // venta a la que corresponde la transaccion
    public int getIdVenta() {
        return idVenta;
    }

    public String getMetodo() {
        return metodo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public Date getFecha() {
        return fecha;
    }

    public EstadoPago getEstado() {
        return estado;
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
        Document documento = new Document("idVenta", idVenta)
                .append("metodo", metodo)
                .append("monto", monto)
                .append("fecha", fecha)
                .append("estado", estado == null ? null : estado.name());
        if (mensaje != null) {
            documento.append("mensaje", mensaje);
        }
        return documento;
    }
}
