
package com.ues.procesamiento_pago.Strategy.impl;

import java.math.BigDecimal;
import java.util.Scanner;

import com.ues.procesamiento_pago.Strategy.MetodoPago;
public class TransferenciaBancaria extends MetodoPago {
    private String cuentaOrigen;
    private String cuentaFinal;
    
    public TransferenciaBancaria() {
        super("Transferencia Bancaria");
    }

    public TransferenciaBancaria(String cuentaOrigen, String cuentaFinal) {
        super("Transferencia Bancaria");
        this.cuentaOrigen = cuentaOrigen;
        this.cuentaFinal = cuentaFinal;
    }
    
    

    @Override
    protected boolean validarEspecifico(BigDecimal monto) {
        //validar que la informacion cuentas no esten vacias
        if(this.cuentaOrigen == null || this.cuentaOrigen.isBlank()){
            System.out.println("La cuenta origen no puede estar vacia");
            return false;
        }
        if(this.cuentaFinal == null || this.cuentaFinal.isBlank()){
            System.out.println("La cuenta origen no puede estar vacia");
            return false;
        }
        return true;
    }

    @Override
    protected boolean ejecutarCobro(BigDecimal monto) {
        System.out.println("Pago realizado con exito, con monto: $" + monto);
        return true;
    }

   /* @Override
    protected void mostrarComprobante(BigDecimal monto) {
        System.out.println("\n-------COMPROBANTE DE PAGO --------------");
        System.out.println("Metodo de Pago    : " + getNombre());
        System.out.println("Total de la compra : $" + monto);
        System.out.println("Cuenta Origen    : " + cuentaOrigen);
        System.out.println("Cuenta Destino    : " + cuentaFinal);
        System.out.println("----------------------------------------------\n");
    }*/

    @Override
    public String getNombre() {
        return nombre;
    }

    @Override
    public void capturarDatos(Scanner scanner) {
        System.out.println("Introduzca el numero de cuenta origen: ");
        this.cuentaOrigen = scanner.nextLine();
        System.out.println("Introduzca el numero de cuenta destino: ");
        this.cuentaFinal = scanner.nextLine();
    }
    
}
