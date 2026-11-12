package com.ues.procesamiento_pago.controller;

import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.ues.procesamiento_pago.Model.EstadoPago;
import com.ues.procesamiento_pago.Model.Venta;
import com.ues.procesamiento_pago.config.MongoDBConnection;
import com.ues.procesamiento_pago.dao.VentaDAO;

public class VentaController {
    private final VentaDAO dao;

    public VentaController(MongoDBConnection mongoDB) {
        if (mongoDB != null && mongoDB.getDatabase() != null) {
            this.dao = new VentaDAO(mongoDB);
        } else {
            this.dao = null;
        }
    }

    public boolean isAvailable() {
        return dao != null;
    }

    public int siguienteNumeroVenta() {
        return dao == null ? 1 : dao.siguienteNumeroVenta();
    }

    public List<Document> listar(Integer numeroVenta, String estado, String metodoPago) {
        return dao == null ? java.util.Collections.emptyList() : dao.listar(numeroVenta, estado, metodoPago);
    }

    public List<Document> listar(ObjectId idTransaccion, Integer numeroVenta, String estado,
            String metodoPago, Date desde, Date hasta) {
        return dao == null ? java.util.Collections.emptyList()
                : dao.listar(idTransaccion, numeroVenta, estado, metodoPago, desde, hasta);
    }

    public void insertar(Venta venta, EstadoPago estado, String metodoPago, String usuario) {
        if (dao != null) {
            dao.insertar(venta, estado, metodoPago, usuario);
        }
    }

    public boolean actualizar(ObjectId id, Venta venta, String estado, String metodoPago) {
        return dao != null && dao.actualizar(id, venta, estado, metodoPago);
    }

    public boolean eliminarVenta(int numeroVenta) {
        return dao != null && dao.eliminarVenta(numeroVenta);
    }

    public boolean cambiarEstado(int numeroVenta, EstadoPago estado, String metodoPago) {
        return dao != null && dao.cambiarEstado(numeroVenta, estado, metodoPago);
    }
}
