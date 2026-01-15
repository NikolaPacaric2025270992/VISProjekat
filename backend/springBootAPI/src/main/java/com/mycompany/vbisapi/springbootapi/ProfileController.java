/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.springbootapi;

import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author nikol
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ArangoDatabase db;

    @Autowired
    private Jena jena;

    @PutMapping("/status/{username}")
    public ResponseEntity<?> updateStatus(@PathVariable String username, @RequestParam boolean traziPosao) throws Exception {
        // 1. Ažuriranje u ArangoDB (za potrebe profila i logina)
        BaseDocument user = db.collection("users").getDocument(username, BaseDocument.class);
        if (user != null) {
            user.addAttribute("traziPosao", traziPosao);
            db.collection("users").updateDocument(username, user);

            // 2. Ažuriranje u Jena TDB (za potrebe rang liste i SPARQL upita)
            jena.updateStudentStatus(username, traziPosao);

            return ResponseEntity.ok("Status uspešno ažuriran u oba sistema.");
        }
        return ResponseEntity.status(404).body("Korisnik nije pronađen.");
    }
    
    @PostMapping("/add-exam/{username}")
    public ResponseEntity<?> addExam(@PathVariable String username, @RequestParam String predmetId, @RequestParam int ocena) throws Exception {
        // Pozivamo Jenu da upiše ispit u TDB
        jena.addExam(username, predmetId, ocena);
        return ResponseEntity.ok("Ispit dodat u bazu znanja za studenta: " + username);
    }
}
