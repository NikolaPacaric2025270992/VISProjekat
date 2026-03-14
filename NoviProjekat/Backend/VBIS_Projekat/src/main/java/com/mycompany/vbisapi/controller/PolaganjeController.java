/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.controller;

import com.mycompany.vbisapi.model.Polaganje;
import com.mycompany.vbisapi.service.PolaganjeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
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
    
    @PostMapping("/dodaj")
    public String dodajPolaganje(@RequestBody Polaganje p){
        try {
            polaganjeService.dodajPolaganje(p);
            return "Uspeh: Polaganje za studenta " + p.getStudentId() + " je evidentirano!";
        } catch (Exception e){
            return "Greska pri evidentiranju polaganja: " + e.getMessage();
        }
    }
    
}
