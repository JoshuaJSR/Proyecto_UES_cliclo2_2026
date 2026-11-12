package com.ues.procesamiento_pago.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.ues.procesamiento_pago.Model.EstadoPago;
import com.ues.procesamiento_pago.Model.Transaccion;
import com.ues.procesamiento_pago.config.MongoDBConnection;
import com.ues.procesamiento_pago.dao.TransaccionDAO;

public class TransaccionController {
    private final TransaccionDAO dao;

    public TransaccionController(MongoDBConnection mongoDB) {
        if (mongoDB != null && mongoDB.getDatabase() != null) {
            this.dao = new TransaccionDAO(mongoDB);
        } else {
            this.dao = null;
        }
    }

    public boolean isAvailable() {
        return dao != null;
    }

    public ObjectId insertar(Transaccion transaccion) {
        return dao == null ? null : dao.insertar(transaccion);
    }

    public Document buscar(ObjectId idTransaccion) {
        return dao == null ? null : dao.buscar(idTransaccion);
    }

    public Document buscarPorVenta(int numeroVenta) {
        return dao == null ? null : dao.buscarPorVenta(numeroVenta);
    }

    public List<Integer> numerosVentaEnRango(Date desde, Date hasta) {
        return dao == null ? java.util.Collections.emptyList() : dao.numerosVentaEnRango(desde, hasta);
    }

    public Map<Integer, Date> fechasPorVenta(List<Integer> numerosVenta) {
        return dao == null ? java.util.Collections.emptyMap() : dao.fechasPorVenta(numerosVenta);
    }

    public List<Document> listarPorRango(Date desde, Date hasta) {
        return dao == null ? java.util.Collections.emptyList() : dao.listarPorRango(desde, hasta);
    }

    public List<Document> resumenPorMetodoYEstado() {
        return dao == null ? java.util.Collections.emptyList() : dao.resumenPorMetodoYEstado();
    }

    public List<Document> listar(Integer numeroVenta, String estado, String metodoPago) {
        return dao == null ? java.util.Collections.emptyList() : dao.listar(numeroVenta, estado, metodoPago);
    }

    public boolean actualizar(ObjectId id, EstadoPago estado, String metodoPago) {
        return dao != null && dao.actualizar(id, estado, metodoPago);
    }

    public boolean eliminar(ObjectId id) {
        return dao != null && dao.eliminar(id);
    }

    public boolean eliminarPorVenta(int numeroVenta) {
        return dao != null && dao.eliminarPorVenta(numeroVenta);
    }
}
