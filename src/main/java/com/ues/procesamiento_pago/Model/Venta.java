package com.ues.procesamiento_pago.Model;

import java.math.BigDecimal;
import java.util.Date;

import org.bson.Document;
import org.bson.types.Decimal128;

public final class Venta {
    private int id;
    private final String nombre;
    private final BigDecimal precio;
    private final int cantidad;
    private final BigDecimal total;
    private final Date fecha;
    
    public Venta(String nombre, BigDecimal precio, int cantidad) {
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
        this.total = calcularTotal(cantidad, precio);
        this.fecha = new Date();
    }
    
    //calcular total
    public BigDecimal calcularTotal(int cantidad, BigDecimal precio){
        return precio.multiply(BigDecimal.valueOf(cantidad));
    }

    public int getId() {
        return id;
    }

    // numero de venta que se asigna al registrar la venta
    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public Date getFecha() {
        return fecha;
    }

    //Reconstruye una venta a partir de un documento de la coleccion ventas
    public static Venta fromDocument(Document doc) {
        Venta venta = new Venta(
                doc.getString("nombre"),
                aBigDecimal(doc.get("precio")),
                doc.getInteger("cantidad", 0));
        Integer numero = doc.getInteger("id");
        if (numero != null) {
            venta.setId(numero);
        }
        return venta;
    }

    public static BigDecimal aBigDecimal(Object valor) {
        if (valor instanceof BigDecimal decimal) {
            return decimal;
        }
        if (valor instanceof Decimal128 decimal128) {
            return decimal128.bigDecimalValue();
        }
        if (valor instanceof Number numero) {
            return BigDecimal.valueOf(numero.doubleValue());
        }
        return BigDecimal.ZERO;
    }

    //Mapea la venta a un Document de MongoDB para guardarlo en la coleccion ventas
    public Document toDocument(EstadoPago estado, String metodoPago, String usuario) {
        return new Document("id", id)
                .append("nombre", nombre)
                .append("precio", precio)
                .append("cantidad", cantidad)
                .append("total", total)
                .append("estado", estado.name())
                .append("fecha", fecha)
                .append("metodoPago", metodoPago)
                .append("usuario", usuario);
    }
}
