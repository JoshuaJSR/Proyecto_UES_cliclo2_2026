package com.ues.procesamiento_pago.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.ues.procesamiento_pago.Model.Transaccion;
import com.ues.procesamiento_pago.config.MongoDBConnection;

public class TransaccionDAO {
    private final MongoCollection<Document> coleccion;

    public TransaccionDAO(MongoDBConnection mongoDB) {
        this.coleccion = mongoDB.getDatabase().getCollection("transacciones");
    }

    // guarda la transaccion y devuelve el identificador que genero MongoDB
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

    //cantidad de operaciones e importe agrupados por metodo y estado
    public List<Document> resumenPorMetodoYEstado() {
        List<Bson> etapas = Arrays.asList(
                Aggregates.group(
                        new Document("metodo", "$metodo").append("estado", "$estado"),
                        Accumulators.sum("cantidad", 1),
                        Accumulators.sum("importe", "$monto")),
                Aggregates.sort(new Document("_id.metodo", 1).append("_id.estado", 1)));
        return coleccion.aggregate(etapas).into(new ArrayList<>());
    }
}
