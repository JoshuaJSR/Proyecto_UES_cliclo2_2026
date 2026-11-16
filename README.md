# Aplicación de Procesamiento de Pagos

## Descripción general
Esta es una aplicación Java para el procesamiento de pagos y ventas. Utiliza MongoDB como base de datos y una arquitectura con patrones de estrategia para los métodos de pago.

## Requisitos previos

- Java Development Kit (JDK) 11 o superior
- Apache Maven 3.6 o superior
- MongoDB instalado y en ejecución

## Instalación

1. Clonar el repositorio o asegurar de tener el código fuente en:
   `src/main/java/com/ues/procesamiento_pago/`

2. Compilar el proyecto con Maven:
   ```
   mvn clean install
   ```

3. Verificar que MongoDB esté en ejecución en el puerto predeterminado (27017).
   La aplicación se conecta a `localhost:27017` sin requerir usuario o contraseña.

## Configuración de la base de datos

- El nombre de la base de datos es: **proceso_pagos**
- Esta configuración está definida en la clase `MongoDBConnection.java` y no requiere modificaciones adicionales para un entorno de desarrollo local.
- La aplicación se conecta a `mongodb://localhost:27017` sin autenticación.

## Ejecución

1. Ejecutar la aplicación mediante Maven:
   ```
   mvn spring-boot:run 
   ```
   O bien, ejecutar la clase principal `Procesamiento_pago` directamente desde el IDE o mediante:
   ```
   java -jar target/procesamiento-pago-*.jar
   ```

2. La aplicación iniciará y estará disponible en el puerto configurado (generalmente 8080).

## Estructura básica

- Modelos: `Venta`, `Transaccion`
- DAOs: `VentaDAO`, `TransaccionDAO`, `UsuarioDAO`
- Controladores: `VentaController`, `TransaccionController`, `UsuarioController`
- Estrategias de pago: `MetodoPago`, `IMetodoPago`
- Vistas/forms: `MainForm`, `VentasForm`, `PagoVentaPendienteForm`, entre otras

## Notas importantes

- No se requiere configurar usuario ni contraseña para la conexión MongoDB en el entorno local predeterminado.
- Si es necesario cambiar el host o puerto de MongoDB, modificar la instanciación de `MongoDBConnection` en la clase `Procesamiento_pago.java`.
- La base de datos `proceso_pagos` se creará automáticamente en MongoDB al primera conexión exitosa.