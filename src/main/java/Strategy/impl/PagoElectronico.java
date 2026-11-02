
package Strategy.impl;

import Strategy.MetodoPago;
import java.math.BigDecimal;
import java.util.Scanner;

public class PagoElectronico extends MetodoPago {
    private String correoCuenta;
    private String plataforma;
    
    public PagoElectronico() {
        super("Pago Electronico");
    }
    public PagoElectronico(String correCuenta, String plataforma) {
        super("Pago Electronico");
        this.correoCuenta = correCuenta;
        this.plataforma = plataforma;
    }

    @Override
    protected boolean validarEspecifico(BigDecimal monto) {
        if(this.correoCuenta == null || this.correoCuenta.isBlank()){
            System.out.println("Debe especificar el correo de la cuenta");
            return false;
        }
        if(this.plataforma == null || this.plataforma.isBlank()){
            System.out.println("Especifique la plataforma de pago");
            return false;
        }
        return true;
    }

    @Override
    protected boolean ejecutarCobro(BigDecimal monto) {
        System.out.println("Monto cobrado: $"+monto);
        return true;
    }

    /*@Override
    protected void mostrarComprobante(BigDecimal monto) {
        System.out.println("\n-------COMPROBANTE DE PAGO --------------");
        System.out.println("Metodo de Pago    : " + getNombre());
        System.out.println("Total de la compra : $" + monto);
        System.out.println("Plataforma de pago: "+ plataforma);
        System.out.println("correo: " + correoCuenta);
        System.out.println("----------------------------------------------\n");
    }*/

    @Override
    public String getNombre() {
        return nombre;
    }

    @Override
    public void capturarDatos(Scanner scanner) {
        System.out.println("Introduzaca la plataforma de pago: ");
        this.plataforma = scanner.nextLine();
        System.out.println("Introduzaca el correo de la plataforma: ");
        this.correoCuenta = scanner.nextLine();
    }
    
}
