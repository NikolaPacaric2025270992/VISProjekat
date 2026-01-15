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
    public ResponseEntity<?> register(@RequestBody Map<String, String> userData) throws Exception {
        String username = userData.get("username");
        String password = userData.get("password");
        String type = userData.get("type"); // "student" ili "agency"
        String realName = userData.get("realName");

        // 1. Spasavanje u ArangoDB
        BaseDocument userDoc = new BaseDocument();
        userDoc.setKey(username);
        userDoc.addAttribute("password", password); // Napomena: U realnosti koristi BCrypt!
        userDoc.addAttribute("type", type);
        userDoc.addAttribute("traziPosao", true);
        userDoc.addAttribute("ontologyUri", "http://www.vbis.org/ontology#" + username);
        
        db.collection("users").insertDocument(userDoc);

        // 2. Spasavanje u OWL preko Jena klase
        if ("student".equals(type)) {
            jena.addStudent(username, realName);
        } else {
            // Dodaj sličnu metodu za agenciju u Jena klasu
            jena.addAgency(username, realName);
        }

        return ResponseEntity.ok("Korisnik registrovan!");
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        // ArangoDB koristi getDocument i vraća null ako dokument ne postoji
        BaseDocument user = db.collection("users").getDocument(username, BaseDocument.class);

        if (user != null) {
            // Izvlačimo lozinku iz svojstava dokumenta
            String dbPassword = (String) user.getProperties().get("password");

            if (dbPassword != null && dbPassword.equals(password)) {
                // Pravimo odgovor za frontend
                Map<String, Object> response = new HashMap<>();
                response.put("username", user.getKey()); // Ovo je _key (username)
                response.put("type", user.getProperties().get("type")); // "student" ili "agency"
                response.put("ontologyUri", user.getProperties().get("ontologyUri"));

                // Ako je student, dodajemo i status traženja posla
                if (user.getProperties().containsKey("traziPosao")) {
                    response.put("traziPosao", user.getProperties().get("traziPosao"));
                }

                return ResponseEntity.ok(response);
            }
        }

        // Ako korisnik ne postoji ili je lozinka pogrešna
        return ResponseEntity.status(401).body("Pogrešno korisničko ime ili lozinka.");
    }
}
