package com.ues.procesamiento_pago.Strategy;

import java.math.BigDecimal;
import java.util.Scanner;

import com.ues.procesamiento_pago.Model.Transaccion;
import com.ues.procesamiento_pago.Model.Venta;

public interface IMetodoPago {
    boolean validar(BigDecimal monto);
    Transaccion procesar(Venta venta);
    String getNombre();
    void capturarDatos(Scanner scanner);
}
