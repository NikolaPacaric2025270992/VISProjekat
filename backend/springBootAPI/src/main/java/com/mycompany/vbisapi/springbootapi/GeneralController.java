/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.springbootapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
public class GeneralController {

    @Autowired
    private Jena jena;
    
    @Autowired
    private ArangoDatabase db;

    @GetMapping("/recommendations/{username}")
    public ResponseEntity<List<String>> getAds(@PathVariable String username) {
        return ResponseEntity.ok(jena.getRecommendedAds(username));
    }

    @GetMapping("/api/ads/all")
    public ResponseEntity<List<Map<String, String>>> getAllAds() {
        return ResponseEntity.ok(jena.getAllAds());
    }

    @GetMapping("/export/ontology")
    @CrossOrigin("*")
    public ResponseEntity<String> exportOntology() {
        try {
            if (jena.getModel() == null) {
                return ResponseEntity.status(500).body("Sistem baze znanja (Jena) nije spreman.");
            }

            String rdfData = jena.getOntologyExport();

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=ontologija_export.rdf")
                    .header("Content-Type", "application/rdf+xml")
                    .body(rdfData);
        } catch (Exception e) {
            // Ispis greske ako ima da vidim sta se tacno desava
            e.printStackTrace(); 
            return ResponseEntity.status(500).body("Greška pri generisanju RDF fajla: " + e.getMessage());
        }
    }
    
    @GetMapping("/export/users")
    public ResponseEntity<List<Map<String, Object>>> exportUsers() {
        try {
            List<Map<String, Object>> usersJson = new ArrayList<>();

            // Izvršavam upit i dobijam kursor
            com.arangodb.ArangoCursor<BaseDocument> cursor = db.query("FOR u IN users RETURN u", BaseDocument.class);

            // Prolazim kroz kursor i punim listu mapa
            cursor.forEachRemaining(doc -> {
                Map<String, Object> map = new HashMap<>(doc.getProperties());
                map.put("username", doc.getKey()); // Dodajemo _key kao username
                usersJson.add(map);
            });

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=korisnici_export.json")
                    .header("Content-Type", "application/json")
                    .body(usersJson);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/import/ontology")
    public ResponseEntity<String> importOntology(@RequestParam("file") MultipartFile file) {
        try {
            boolean success = jena.importData(file.getInputStream());
            if (success) return ResponseEntity.ok("Uspešno uvezeno.");
            else return ResponseEntity.status(400).body("Nevalidni podaci.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Greška.");
        }
    }
}