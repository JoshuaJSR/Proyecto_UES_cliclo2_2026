package com.ues.procesamiento_pago.dao;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ues.procesamiento_pago.Model.Transaccion;
import com.ues.procesamiento_pago.config.MongoDBConnection;

public class TransaccionDAO {
    private final MongoCollection<Document> coleccion;

    public TransaccionDAO(MongoDBConnection mongoDB) {
        this.coleccion = mongoDB.getDatabase().getCollection("transacciones");
    }

    // guarda la transaccion y devuelve el identificador que genero la base
    public ObjectId insertar(Transaccion transaccion) {
        Document documento = transaccion.toDocument();
        coleccion.insertOne(documento);
        ObjectId id = documento.getObjectId("_id");
        transaccion.setId(id);
        return id;
    }

    // busca una transaccion por su identificador 
    public Document buscar(ObjectId idTransaccion) {
        if (idTransaccion == null) {
            return null;
        }
        return coleccion.find(Filters.eq("_id", idTransaccion)).first();
    }

    // transaccion principal asociada a una venta
    public Document buscarPorVenta(int numeroVenta) {
        return coleccion.find(Filters.eq("idVenta", numeroVenta))
                .sort(new Document("fecha", -1))
                .first();
    }
}
