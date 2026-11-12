package com.ues.procesamiento_pago.dao;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ues.procesamiento_pago.Model.Usuario;
import com.ues.procesamiento_pago.config.MongoDBConnection;

public class UsuarioDAO {
    private final MongoCollection<Document> coleccion;

    public UsuarioDAO(MongoDBConnection mongoDB) {
        this.coleccion = mongoDB.getDatabase().getCollection("usuarios");
    }

    public boolean existe(String nombre) {
        return coleccion.find(Filters.eq("nombre", nombre)).first() != null;
    }

    public void insertar(Usuario usuario) {
        coleccion.insertOne(usuario.toDocument());
    }

    public Usuario buscarPorNombre(String nombre) {
        Document doc = coleccion.find(Filters.eq("nombre", nombre)).first();
        return doc == null ? null : Usuario.fromDocument(doc);
    }
}