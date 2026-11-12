package com.ues.procesamiento_pago.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.ues.procesamiento_pago.Model.EstadoPago;
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

    // buscar una transaccion por su identificador
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

    // ultima transaccion registrada para cada venta 
    public Map<Integer, Document> ultimaTransaccionPorVenta(List<Integer> numerosVenta) {
        Map<Integer, Document> resultado = new LinkedHashMap<>();
        if (numerosVenta == null || numerosVenta.isEmpty()) {
            return resultado;
        }
        for (Document t : coleccion.find(Filters.in("idVenta", numerosVenta))
                .sort(new Document("fecha", -1))) {
            Integer n = t.getInteger("idVenta");
            if (n != null && !resultado.containsKey(n)) {
                resultado.put(n, t);
            }
        }
        return resultado;
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

    // lista transacciones filtradas por numero de venta, estado y metodo
    public List<Document> listar(Integer numeroVenta, String estado, String metodoPago) {
        List<Bson> condiciones = new ArrayList<>();
        if (numeroVenta != null) {
            condiciones.add(Filters.eq("idVenta", numeroVenta));
        }
        if (estado != null && !estado.isEmpty()) {
            condiciones.add(Filters.eq("estado", estado));
        }
        if (metodoPago != null && !metodoPago.isEmpty()) {
            condiciones.add(Filters.eq("metodo", metodoPago));
        }
        Bson filtro = condiciones.isEmpty() ? new Document() : Filters.and(condiciones);
        return coleccion.find(filtro)
                .sort(new Document("fecha", -1))
                .into(new ArrayList<>());
    }

    // actualizar estado y metodo de pago de una transaccion
    public boolean actualizar(ObjectId id, EstadoPago estado, String metodoPago) {
        if (id == null) {
            return false;
        }
        Document cambios = new Document("estado", estado == null ? null : estado.name())
                .append("metodo", metodoPago);
        return coleccion.updateOne(Filters.eq("_id", id), new Document("$set", cambios))
                .getMatchedCount() > 0;
    }

    // guardar el resultado del cobro, estado, metodo, importe, fecha del pago y motivo
    public boolean registrarResultado(ObjectId id, Transaccion transaccion) {
        if (id == null || transaccion == null) {
            return false;
        }
        Document cambios = new Document("estado", transaccion.getEstado() == null ? null : transaccion.getEstado().name())
                .append("metodo", transaccion.getMetodo())
                .append("monto", transaccion.getMonto())
                .append("fecha", transaccion.getFecha());
        Document actualizacion = new Document();
        if (transaccion.getMensaje() != null) {
            cambios.append("mensaje", transaccion.getMensaje());
        } else {

            actualizacion.append("$unset", new Document("mensaje", ""));
        }
        actualizacion.append("$set", cambios);
        return coleccion.updateOne(Filters.eq("_id", id), actualizacion).getMatchedCount() > 0;
    }

    // eliminar una transaccion por su identificador
    public boolean eliminar(ObjectId id) {
        if (id == null) {
            return false;
        }
        return coleccion.deleteOne(Filters.eq("_id", id)).getDeletedCount() > 0;
    }

    // eliminar todas las transacciones asociadas a una venta
    public boolean eliminarPorVenta(int numeroVenta) {
        return coleccion.deleteMany(Filters.eq("idVenta", numeroVenta)).getDeletedCount() > 0;
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
