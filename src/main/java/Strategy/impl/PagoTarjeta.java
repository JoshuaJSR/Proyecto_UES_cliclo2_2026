
package Strategy.impl;

import Strategy.MetodoPago;
import java.math.BigDecimal;
import java.util.Scanner;

public class PagoTarjeta extends MetodoPago {

    private String numeroTarjeta;
    private String titular;
    
    public PagoTarjeta() {
        super("Pago con tarjeta");
    }
    public PagoTarjeta(String numeroTarjeta, String titular) {
        super("Pago con tarjeta");
        this.numeroTarjeta = numeroTarjeta;
        this.titular = titular;
    }

    @Override
    protected boolean validarEspecifico(BigDecimal monto) {
        if(this.numeroTarjeta == null || this.numeroTarjeta.isBlank()){
            System.out.println("El numero de la tarjeta esta vacio");
            return false;
        }
        if(this.titular == null || this.titular.isBlank()){
            System.out.println("El nombre del titular esta vacio");
            return false;
        }
        return true;
    }

    @Override
    protected boolean ejecutarCobro(BigDecimal monto) {
        System.out.println("Monto cobrado: $"+monto);
        return true;
    }

  /*  @Override
    protected void mostrarComprobante(BigDecimal monto) {
        
        System.out.println("\n-------COMPROBANTE DE PAGO --------------");
        System.out.println("Metodo de Pago    : " + getNombre());
        System.out.println("Total de la compra : $" + monto);
        System.out.println("----------------------------------------------\n");
    }*/

    @Override
    public String getNombre() {
        return nombre;
    }

    @Override
    public void capturarDatos(Scanner scanner) {
        System.out.println("Introduzca el numero de tarjeta:");
        this.numeroTarjeta = scanner.nextLine();
        System.out.println("Introduzca el nombre del titular:");
        this.titular = scanner.nextLine();
    }
    
}
