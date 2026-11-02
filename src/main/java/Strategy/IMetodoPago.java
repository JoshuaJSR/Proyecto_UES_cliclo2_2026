package Strategy;

import Model.Transaccion;
import Model.Venta;
import java.math.BigDecimal;
import java.util.Scanner;

public interface IMetodoPago {
    boolean validar(BigDecimal monto);
    Transaccion procesar(Venta venta);
    String getNombre();
    void capturarDatos(Scanner scanner);
}
