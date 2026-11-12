
package com.ues.procesamiento_pago.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UpdateOptions;
import com.ues.procesamiento_pago.Model.EstadoPago;
import com.ues.procesamiento_pago.Model.Venta;
import com.ues.procesamiento_pago.config.MongoDBConnection;

public class VentaDAO {
    // coleccion donde se guarda el ultimo numero de venta entregado
    private static final String COLECCION_CONTADORES = "contadores";
    private static final String CONTADOR_NUMERO_VENTA = "numeroVenta";

    private final MongoCollection<Document> coleccion;
    private final MongoCollection<Document> contadores;
    private final TransaccionDAO transaccionDAO;

    public VentaDAO(MongoDBConnection mongoDB) {
        this.coleccion = mongoDB.getDatabase().getCollection("ventas");
        this.contadores = mongoDB.getDatabase().getCollection(COLECCION_CONTADORES);
        this.transaccionDAO = new TransaccionDAO(mongoDB);
    }

    public void insertar(Venta venta, EstadoPago estado, String metodoPago, String usuario) {
        coleccion.insertOne(venta.toDocument(estado, metodoPago, usuario));
    }

    // proximo numero de venta sin reservarlo, solo sirve para mostrarlo en pantalla
    public int siguienteNumeroVenta() {
        Document contador = contadores.find(Filters.eq("_id", CONTADOR_NUMERO_VENTA)).first();
        int reservado = contador == null ? 0 : contador.getInteger("seq", 0);
        return Math.max(reservado, ultimoNumeroUsado()) + 1;
    }

    public int reservarNumeroVenta() {
        asegurarContador();
        Document contador = contadores.findOneAndUpdate(
                Filters.eq("_id", CONTADOR_NUMERO_VENTA),
                new Document("$inc", new Document("seq", 1)),
                new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER));
        Integer secuencia = contador == null ? null : contador.getInteger("seq");
        return secuencia == null ? 1 : secuencia;
    }

    private void asegurarContador() {
        if (contadores.find(Filters.eq("_id", CONTADOR_NUMERO_VENTA)).first() != null) {
            return;
        }
        contadores.updateOne(Filters.eq("_id", CONTADOR_NUMERO_VENTA),
                new Document("$max", new Document("seq", ultimoNumeroUsado())),
                new UpdateOptions().upsert(true));
    }

    private int ultimoNumeroUsado() {
        Document ultimaVenta = coleccion.find()
                .sort(new Document("id", -1))
                .limit(1)
                .first();
        if (ultimaVenta == null || ultimaVenta.getInteger("id") == null) {
            return 0;
        }
        return ultimaVenta.getInteger("id");
    }

    // lista las ventas filtradas por numero de venta, estado y metodo de pago 
    public List<Document> listar(Integer numeroVenta, String estado, String metodoPago) {
        return listar(null, numeroVenta, estado, metodoPago, null, null);
    }

    
    public List<Document> listar(ObjectId idTransaccion, Integer numeroVenta, String estado,
            String metodoPago, Date desde, Date hasta) {
        List<Bson> condiciones = new ArrayList<>();
        if (idTransaccion != null) {
            condiciones.add(Filters.eq("_id", idTransaccion));
        }
        if (numeroVenta != null) {
            condiciones.add(Filters.eq("id", numeroVenta));
        }
        if (estado != null && !estado.isEmpty()) {
            condiciones.add(Filters.eq("estado", estado));
        }
        if (metodoPago != null && !metodoPago.isEmpty()) {
            condiciones.add(Filters.eq("metodoPago", metodoPago));
        }
        if (desde != null || hasta != null) {
            condiciones.add(Filters.in("id", transaccionDAO.numerosVentaEnRango(desde, hasta)));
        }
        Bson filtro = condiciones.isEmpty() ? new Document() : Filters.and(condiciones);
        List<Document> ventas = coleccion.find(filtro)
                .sort(new Document("id", 1))
                .into(new ArrayList<>());
        completarFechas(ventas);
        return ventas;
    }

    private void completarFechas(List<Document> ventas) {
        List<Integer> numeros = new ArrayList<>();
        for (Document venta : ventas) {
            Integer numero = venta.getInteger("id");
            if (numero != null && !numeros.contains(numero)) {
                numeros.add(numero);
            }
        }
        Map<Integer, Date> fechas = transaccionDAO.fechasPorVenta(numeros);
        for (Document venta : ventas) {
            Date fecha = fechas.get(venta.getInteger("id"));
            if (fecha != null) {
                venta.put("fecha", fecha);
            }
        }
    }

    // actualiza los datos de un item de venta ya registrado
    public boolean actualizar(ObjectId id, Venta venta, String estado, String metodoPago) {
        Document cambios = new Document("nombre", venta.getNombre())
                .append("precio", venta.getPrecio())
                .append("cantidad", venta.getCantidad())
                .append("total", venta.getTotal())
                .append("estado", estado)
                .append("metodoPago", metodoPago);
        return coleccion.updateOne(Filters.eq("_id", id), new Document("$set", cambios))
                .getMatchedCount() > 0;
    }

    // elimina todos los items de una venta 
    public boolean eliminarVenta(int numeroVenta) {
        return coleccion.deleteMany(Filters.eq("id", numeroVenta)).getDeletedCount() > 0;
    }

    // aplica el estado y el metodo de pago a todos los items de una venta
    public boolean cambiarEstado(int numeroVenta, EstadoPago estado, String metodoPago) {
        Document cambios = new Document("estado", estado.name())
                .append("metodoPago", metodoPago);
        return coleccion.updateMany(Filters.eq("id", numeroVenta), new Document("$set", cambios))
                .getMatchedCount() > 0;
    }
}
