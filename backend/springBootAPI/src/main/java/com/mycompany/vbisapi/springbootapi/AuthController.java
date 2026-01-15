package com.mycompany.vbisapi.springbootapi;

import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private ArangoDatabase db;

    @Autowired
    private Jena jena;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> userData) {
        String username = userData.get("username");
        String password = userData.get("password");
        String type = userData.get("type"); // "student" ili "agency"
        String realName = userData.get("realName");

        // 1. Spasavanje u ArangoDB
        BaseDocument userDoc = new BaseDocument();
        userDoc.setKey(username);
        userDoc.addAttribute("password", password); // Napomena: U realnosti koristi BCrypt!
        userDoc.addAttribute("type", type);
        userDoc.addAttribute("ontologyUri", "http://www.vbis.org/ontology#" + username);
        
        db.collection("users").insertDocument(userDoc);

        // 2. Spasavanje u OWL preko Jena klase
        if ("student".equals(type)) {
            jena.addStudent(username, realName);
        } else {
            // Dodaj sličnu metodu za agenciju u Jena klasu
        }

        return ResponseEntity.ok("Korisnik registrovan!");
    }
}
