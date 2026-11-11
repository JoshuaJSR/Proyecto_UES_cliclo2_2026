
package com.ues.procesamiento_pago.dao;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
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

    // calcula el proximo numero de venta: 1 si no hay ventas registradas
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

    // lista las ventas filtradas por numero de venta, estado y metodo de pago (null = sin filtro)
    public List<Document> listar(Integer numeroVenta, String estado, String metodoPago) {
        List<Bson> condiciones = new ArrayList<>();
        if (numeroVenta != null) {
            condiciones.add(Filters.eq("id", numeroVenta));
        }
        if (estado != null && !estado.isEmpty()) {
            condiciones.add(Filters.eq("estado", estado));
        }
        if (metodoPago != null && !metodoPago.isEmpty()) {
            condiciones.add(Filters.eq("metodoPago", metodoPago));
        }
        Bson filtro = condiciones.isEmpty() ? new Document() : Filters.and(condiciones);
        return coleccion.find(filtro)
                .sort(new Document("id", 1).append("fecha", 1))
                .into(new ArrayList<>());
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
}