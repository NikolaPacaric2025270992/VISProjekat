package com.mycompany.vbisapi.springbootapi;

import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ArangoDatabase db;

    @Autowired
    private Jena jena;

    @PutMapping("/status/{username}")
    public ResponseEntity<?> updateStatus(
            @PathVariable("username") String username, 
            @RequestParam("traziPosao") boolean traziPosao) throws Exception {
        
        // 1. Ažuriranje u ArangoDB
        BaseDocument user = db.collection("users").getDocument(username, BaseDocument.class);
        if (user != null) {
            user.addAttribute("traziPosao", traziPosao);
            db.collection("users").updateDocument(username, user);

            // 2. Ažuriranje u Jena TDB
            jena.updateStudentStatus(username, traziPosao);

            return ResponseEntity.ok("Status uspešno ažuriran u oba sistema.");
        }
        return ResponseEntity.status(404).body("Korisnik nije pronađen.");
    }
    
    @PostMapping("/add-exam/{username}")
    public ResponseEntity<?> addExam(
            @PathVariable("username") String username, 
            @RequestParam("predmetId") String predmetId, 
            @RequestParam("ocena") int ocena, 
            @RequestParam("nivo") int nivo) throws Exception {
        
        // Pozivamo Jenu da upiše ispit u TDB
        jena.addExam(username, predmetId, ocena, nivo);
        return ResponseEntity.ok("Ispit dodat u bazu znanja za studenta: " + username);
    }
}