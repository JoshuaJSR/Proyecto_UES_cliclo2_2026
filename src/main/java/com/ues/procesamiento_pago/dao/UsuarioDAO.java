package com.ues.procesamiento_pago.dao;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
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
    public List<Usuario> listar() {
        List<Usuario> usuarios = new ArrayList<>();
        for (Document doc : coleccion.find().sort(Sorts.ascending("nombre"))) {
            usuarios.add(Usuario.fromDocument(doc));
        }
        return usuarios;
    }

    public void actualizar(String nombreOriginal, Usuario usuario) {
        coleccion.updateOne(
                Filters.eq("nombre", nombreOriginal),
                Updates.combine(
                        Updates.set("nombre", usuario.getNombre()),
                        Updates.set("password", usuario.getPassword()),
                        Updates.set("rol", usuario.getRol())
                )
        );
        
    }
        public void eliminar(String nombre) {
        coleccion.deleteOne(Filters.eq("nombre", nombre));
    }

}