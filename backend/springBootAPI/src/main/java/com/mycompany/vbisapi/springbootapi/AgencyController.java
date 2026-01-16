/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.springbootapi;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author nikol
 */
@RestController
@RequestMapping("/api/agency")
public class AgencyController {

    @Autowired
    private Jena jena;

    // Agencija postavlja oglas
    @PostMapping("/add-job")
    public ResponseEntity<?> addJob(@RequestBody Map<String, Object> data) throws Exception {
        String oglasId = (String) data.get("id");
        String naslov = (String) data.get("naslov");
        String agencijaUsername = (String) data.get("agencijaUsername");

        // KASTUJEMO u listu mapa (jer svaka veština sada ima nivo i prioritet)
        List<Map<String, Object>> vestine = (List<Map<String, Object>>) data.get("vestine");

        jena.addOglas(oglasId, naslov, agencijaUsername, vestine);
        return ResponseEntity.ok("Oglas uspešno postavljen sa detaljnim zahtevima veština.");
    }

    // Rang lista za odabrani oglas
    @GetMapping("/rank-students/{oglasId}")
    public ResponseEntity<List<Map<String, String>>> rankStudents(@PathVariable String oglasId) {
        return ResponseEntity.ok(jena.getRankedStudentsForOglas(oglasId));
    }
    
    // Spisak svih studenata koji traže posao
    @GetMapping("/students-searching")
    public ResponseEntity<List<Map<String, String>>> getStudentsSearching() {
        return ResponseEntity.ok(jena.getStudentsLookingForWork());
    }
    
    @GetMapping("/api/debug/dump")
    public String dumpDatabase() {
        jena.debugPrintAllTriples();
        return "Podaci su ispisani u Java konzoli (NetBeans terminal).";
    }
}