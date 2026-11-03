package Model;

import java.math.BigDecimal;
import java.util.Date;
import org.bson.Document;

public class Venta {
    private int id;
    private String nombre;
    private BigDecimal precio;
    private int cantidad;
    private BigDecimal total;
    private Date fecha;
    
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

    //Mapea la venta a un Document de MongoDB para guardarlo en la coleccion ventas
    public Document toDocument() {
        return new Document("nombre", nombre)
                .append("precio", precio)
                .append("cantidad", cantidad)
                .append("total", total)
                .append("fecha", fecha);
    }
}
