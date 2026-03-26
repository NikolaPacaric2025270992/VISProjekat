/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.controller;

import com.mycompany.vbisapi.model.Polaganje;
import com.mycompany.vbisapi.service.ArangoService;
import com.mycompany.vbisapi.service.PolaganjeService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/polaganja")
@CrossOrigin(origins = "http://localhost:5173")
public class PolaganjeController {
    
    @Autowired
    private PolaganjeService polaganjeService;
    
    @Autowired
    private ArangoService arangoService;
    
    @PostMapping("/dodaj")
    public String dodajPolaganje(@RequestBody Polaganje p){
        try {
            polaganjeService.dodajPolaganje(p);
            return "Uspeh: Polaganje za studenta " + p.getStudentId() + " je evidentirano!";
        } catch (Exception e){
            return "Greska pri evidentiranju polaganja: " + e.getMessage();
        }
    }
    
    @GetMapping("/student/{studentId}")
    public List<Polaganje> getPolaganjaStudenta(@PathVariable String studentId) {
        return arangoService.nadjiPolaganjaStudenta(studentId);
    }
    
    @DeleteMapping("/obrisi/{id}")
    public ResponseEntity<?> obrisiPolaganje(@PathVariable String id) {
        try {
            polaganjeService.obrisiPolaganje(id);
            return ResponseEntity.ok("Polaganje uspešno obrisano iz sistema.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Greška: " + e.getMessage());
        }
    }
}
