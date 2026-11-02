
package Strategy;
import Model.EstadoPago;
import Model.Transaccion;
import Model.Venta;
import java.math.BigDecimal;
import java.util.Scanner;


public abstract class MetodoPago implements IMetodoPago{
    protected String nombre;

    public MetodoPago(String nombre) {
        this.nombre = nombre;
    }
    
    @Override
    public boolean validar(BigDecimal monto) {
        if(monto == null || monto.compareTo(BigDecimal.ZERO) <= 0){ //validar que no sea 0
            return false;
        }
        return validarEspecifico(monto);
    }

    @Override
    public Transaccion procesar(Venta venta) {
        System.out.println("\n----Procesando pago con " + nombre + " ------");
        if (!validar(venta.getTotal())) {
            System.out.println(" Error en la validacion del pago.");
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
            System.out.println(" El cobro no pudo completarse.");
            return new Transaccion(venta.getId(), 
                    nombre, venta.getTotal(), 
                    EstadoPago.RECHAZADO);
        }
    }
 
    //metodos abstractos
    protected abstract boolean validarEspecifico(BigDecimal monto);
    protected abstract boolean ejecutarCobro(BigDecimal monto);
   // protected abstract void mostrarComprobante(BigDecimal monto);
    
}