package com.ues.procesamiento_pago.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // busca una transaccion por su identificador (null si no existe)
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

    // numeros de venta cuyas transacciones caen en el rango de fechas
    public List<Integer> numerosVentaEnRango(Date desde, Date hasta) {
        List<Integer> numeros = new ArrayList<>();
        for (Document transaccion : coleccion.find(filtroFecha(desde, hasta))
                .projection(new Document("idVenta", 1))) {
            Integer numero = transaccion.getInteger("idVenta");
            if (numero != null) {
                numeros.add(numero);
            }
        }
        return numeros;
    }

    // fecha de la transaccion principal de cada venta
    public Map<Integer, Date> fechasPorVenta(List<Integer> numerosVenta) {
        Map<Integer, Date> fechas = new HashMap<>();
        if (numerosVenta == null || numerosVenta.isEmpty()) {
            return fechas;
        }
        Document filtro = new Document("idVenta", new Document("$in", numerosVenta));
        for (Document transaccion : coleccion.find(filtro)
                .projection(new Document("idVenta", 1).append("fecha", 1))) {
            Integer numero = transaccion.getInteger("idVenta");
            Date fecha = transaccion.getDate("fecha");
            if (numero != null && fecha != null) {
                fechas.put(numero, fecha);
            }
        }
        return fechas;
    }

    // transacciones registradas en un rango de fechas
    public List<Document> listarPorRango(Date desde, Date hasta) {
        return coleccion.find(filtroFecha(desde, hasta))
                .sort(new Document("fecha", -1))
                .into(new ArrayList<>());
    }

    // cantidad de operaciones e importe agrupados por metodo y estado
    public List<Document> resumenPorMetodoYEstado() {
        List<Bson> etapas = Arrays.asList(
                Aggregates.group(
                        new Document("metodo", "$metodo").append("estado", "$estado"),
                        Accumulators.sum("cantidad", 1),
                        Accumulators.sum("importe", "$monto")),
                Aggregates.sort(new Document("_id.metodo", 1).append("_id.estado", 1)));
        return coleccion.aggregate(etapas).into(new ArrayList<>());
    }

    private static Bson filtroFecha(Date desde, Date hasta) {
        List<Bson> condiciones = new ArrayList<>();
        if (desde != null) {
            condiciones.add(Filters.gte("fecha", desde));
        }
        if (hasta != null) {
            condiciones.add(Filters.lte("fecha", hasta));
        }
        return condiciones.isEmpty() ? new Document() : Filters.and(condiciones);
    }
}
