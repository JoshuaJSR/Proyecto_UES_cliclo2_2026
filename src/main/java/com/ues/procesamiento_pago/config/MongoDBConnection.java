package com.ues.procesamiento_pago.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;

public class MongoDBConnection {
    private final ConnectionString connectionString;
    private MongoClient client;
    private MongoDatabase database;
    
    public MongoDBConnection(String host, int port) {
        String uri = String.format("mongodb://%s:%d/", host, port);
        this.connectionString = new ConnectionString(uri);
        this.client = null;
        this.database = null;
    }
    
    public boolean crearConexion() throws MongoException{
        try {
            MongoClient mongoClient = MongoClients.create(this.connectionString);
            
            this.database = mongoClient.getDatabase("proceso_pagos");
            Bson comando = new BsonDocument("ping", new BsonInt64(1));
            
            Document resultado = this.database.runCommand(comando);
            
            System.out.println("Conexion establecida . Ping: " + resultado.toString());
            
            this.client = mongoClient;
            
            return true;
        } catch (MongoException e) {
            System.out.println("Error: "+ e.getMessage());
            throw e;
        }
    }
    
    public MongoDatabase getDatabase() {
        return this.database;
    }
    
    //metodo para cerrar conexion
    public void cerrarConexion(){
        if(this.client != null){
            this.client.close();
            
            System.out.println("Conexion cerrada");
        }
    }
}
