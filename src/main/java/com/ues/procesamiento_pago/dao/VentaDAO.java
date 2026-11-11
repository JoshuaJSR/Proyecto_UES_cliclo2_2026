
package com.ues.procesamiento_pago.dao;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.ues.procesamiento_pago.Model.EstadoPago;
import com.ues.procesamiento_pago.Model.Venta;
import com.ues.procesamiento_pago.config.MongoDBConnection;

public class VentaDAO {
    private final MongoCollection<Document> coleccion;

    public VentaDAO(MongoDBConnection mongoDB) {
        this.coleccion = mongoDB.getDatabase().getCollection("ventas");
    }

    public void insertar(Venta venta, EstadoPago estado, String metodoPago, String usuario) {
        coleccion.insertOne(venta.toDocument(estado, metodoPago, usuario));
    }

    // calcula el proximo numero de venta, 1 si no hay ventas registradas
    public int siguienteNumeroVenta() {
        Document ultimaVenta = coleccion.find()
                .sort(new Document("id", -1))
                .limit(1)
                .first();
        if (ultimaVenta == null || ultimaVenta.getInteger("id") == null) {
            return 1;
        }
        return ultimaVenta.getInteger("id") + 1;
    }
}