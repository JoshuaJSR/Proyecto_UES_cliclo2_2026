package com.ues.procesamiento_pago.controller;

import com.ues.procesamiento_pago.Model.Usuario;
import com.ues.procesamiento_pago.config.MongoDBConnection;
import com.ues.procesamiento_pago.dao.UsuarioDAO;

public class UsuarioController {
    private final UsuarioDAO dao;

    public UsuarioController(MongoDBConnection mongoDB) {
        if (mongoDB != null && mongoDB.getDatabase() != null) {
            this.dao = new UsuarioDAO(mongoDB);
        } else {
            this.dao = null;
        }
    }

    public boolean isAvailable() {
        return dao != null;
    }

    public Usuario buscarPorNombre(String nombre) {
        return dao == null ? null : dao.buscarPorNombre(nombre);
    }

    public boolean existe(String nombre) {
        return dao != null && dao.existe(nombre);
    }

    public void insertar(Usuario usuario) {
        if (dao != null) {
            dao.insertar(usuario);
        }
    }
}
