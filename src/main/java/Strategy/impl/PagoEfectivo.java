
package Strategy.impl;

import Strategy.MetodoPago;
import java.math.BigDecimal;
import java.util.Scanner;


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
       //validar si el monto entregado es igual,mayor o menor que el total  
       if (this.montoRecibido == null || this.montoRecibido.compareTo(monto) < 0) {
            System.out.println("El dinero entregado es insuficiente.");
            return false;
        }
        return true;
    }

    @Override
    protected boolean ejecutarCobro(BigDecimal monto) {
        BigDecimal cambio = calcularCambio(monto);
        
        System.out.println("Monto recibido: $" + montoRecibido + 
                "\nCambio a entregar: $" + cambio);
        return true;
    }

   /* @Override
    protected void mostrarComprobante(BigDecimal monto) {
        BigDecimal cambio = calcularCambio(monto);
        
        System.out.println("\n-------COMPROBANTE DE PAGO --------------");
        System.out.println("Metodo de Pago    : " + getNombre());
        System.out.println("Total de la compra : $" + monto);
        System.out.println("Monto Entregado    : $" + this.montoRecibido);
        System.out.println("Cambio Devuelto    : $" + cambio);
        System.out.println("----------------------------------------------\n");
    }*/

    @Override
    public String getNombre() {
        return this.nombre;
    }
    
    //Calcula el cambio restando el total de la venta al monto recibido
    public BigDecimal calcularCambio(BigDecimal montoTotal){
        try {
            if(montoRecibido == null || montoTotal == null){
                throw new IllegalArgumentException("Los montos no pueden ser nulos.");
            }
           return montoRecibido.subtract(montoTotal);
        } catch (Exception e) {
            System.err.println("Error en los datos de entrada: " + e.getMessage());
            return BigDecimal.ZERO;
        }
        
    }

    @Override
    public void capturarDatos(Scanner scanner) {
        System.out.print("Monto entregado por el cliente: $");
        this.montoRecibido = scanner.nextBigDecimal();
        scanner.nextLine();
    }
}
