
package com.ues.procesamiento_pago;

import com.mongodb.MongoException;
import com.ues.procesamiento_pago.config.MongoDBConnection;
import view.LoginForm;


public class Procesamiento_pago {

    public static void main(String[] args) {
        MongoDBConnection mongoDB = new MongoDBConnection("localhost", 27017);
        
        try {
            mongoDB.crearConexion();
        } catch (MongoException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            LoginForm ventana = new LoginForm(mongoDB);
            ventana.setVisible(true);
        });
    }
}
