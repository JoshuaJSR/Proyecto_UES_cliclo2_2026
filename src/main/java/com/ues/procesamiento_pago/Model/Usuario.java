
package com.ues.procesamiento_pago.Model;

import java.util.Date;

import org.bson.Document;

public class Usuario {
   private String nombre;
   private String password;
   private String rol;
   private Date createdAt;
   
    public Usuario(String nombre, String password, String rol) {
        this.nombre = nombre;
        this.password = password;
        this.rol = rol;
        this.createdAt = new Date();
    }
    
    public String getNombre() { return nombre; }
    public String getPassword() { return password; }
    public String getRol() { return rol; }
    public Date getCreatedAt() { return createdAt; }
    
    
    //Dertermina si tienen permisos de administrados
    public boolean esAdmin() {
        return "Admin".equals(rol);
    }
    
    
    //Mapea la entidad a un Document de MongoDB para guardarlo en la BD
    public Document toDocument() {
        return new Document("nombre", nombre)
                .append("password", password)
                .append("rol", rol)
                .append("createdAt", createdAt);
    }
    //Reconstruye el objeto Usuario desde un documento de Mongo
    public static Usuario fromDocument(Document doc) {
        Usuario u = new Usuario(
                doc.getString("nombre"),
                doc.getString("password"),
                doc.getString("rol"));
        u.createdAt = doc.getDate("createdAt");
        return u;
    }
}
