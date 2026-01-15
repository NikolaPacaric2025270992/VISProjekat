/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.springbootapi;

import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author nikol
 */
@Repository
public class UserRepository {
    @Autowired
    private ArangoDatabase db;

    public void saveUser(String username, String password, String role, String ontologyUri) {
        BaseDocument user = new BaseDocument();
        user.setKey(username);
        user.addAttribute("password", password); // Ovde bi idealno išao BCrypt
        user.addAttribute("role", role); // "student" ili "agency"
        user.addAttribute("ontologyUri", ontologyUri);
        
        db.collection("users").insertDocument(user);
    }

    public BaseDocument findByUsername(String username) {
        return db.collection("users").getDocument(username, BaseDocument.class);
    }
}
