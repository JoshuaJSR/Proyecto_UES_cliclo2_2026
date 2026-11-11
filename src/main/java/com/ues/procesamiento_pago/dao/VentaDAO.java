
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
import com.ues.procesamiento_pago.Model.EstadoPago;
import com.ues.procesamiento_pago.Model.Venta;
import com.ues.procesamiento_pago.config.MongoDBConnection;

public class VentaDAO {
    private final MongoCollection<Document> coleccion;
    private final TransaccionDAO transaccionDAO;

    public VentaDAO(MongoDBConnection mongoDB) {
        this.coleccion = mongoDB.getDatabase().getCollection("ventas");
        this.transaccionDAO = new TransaccionDAO(mongoDB);
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
        // la fecha se guarda en la transaccion: el rango se resuelve con sus ventas
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

    // la fecha vive en la transaccion, se agrega a cada item para mostrarla
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
