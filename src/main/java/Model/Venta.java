
package Model;

import java.math.BigDecimal;

public class Venta {
    private int id;
    private String nombre;
    private BigDecimal precio;
    private int cantidad;
    private BigDecimal total;
    
    public Venta(String nombre, BigDecimal precio, int cantidad) {
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
        this.total = calcularTotal(cantidad, precio);
    }
    
    //calcular tota
    public BigDecimal calcularTotal(int cantidad, BigDecimal precio){
       return precio.multiply(BigDecimal.valueOf(cantidad));
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
    
    
}
