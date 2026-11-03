
package dao;

import Model.Venta;
import com.mongodb.client.MongoCollection;
import com.ues.procesamiento_pago.config.MongoDBConnection;
import org.bson.Document;

public class VentaDAO {
    private final MongoCollection<Document> coleccion;

    public VentaDAO(MongoDBConnection mongoDB) {
        this.coleccion = mongoDB.getDatabase().getCollection("ventas");
    }

    public void insertar(Venta venta) {
        coleccion.insertOne(venta.toDocument());
    }
}