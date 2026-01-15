/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.springbootapi;

import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@RestController
public class GeneralController {

    @Autowired
    private Jena jena;

    @GetMapping("/recommendations/{username}")
    public ResponseEntity<List<String>> getAds(@PathVariable String username) {
        return ResponseEntity.ok(jena.getRecommendedAds(username));
    }

    @GetMapping("/api/ads/all")
    public ResponseEntity<List<Map<String, String>>> getAllAds() {
        return ResponseEntity.ok(jena.getAllAds());
    }

    @GetMapping("/export/ontology")
    public ResponseEntity<String> exportOntology() {
        Model model = jena.getModel();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        model.write(baos, "RDF/XML-ABBREV");
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=ontologija_export.rdf")
                .body(baos.toString());
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