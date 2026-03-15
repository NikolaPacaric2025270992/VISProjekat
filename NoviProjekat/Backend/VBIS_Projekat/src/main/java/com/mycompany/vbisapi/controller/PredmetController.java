/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.controller;

import com.mycompany.vbisapi.model.Predmet;
import com.mycompany.vbisapi.service.ArangoService;
import com.mycompany.vbisapi.service.PredmetService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author nikol
 */
@RestController
@RequestMapping("/api/predmeti")
@CrossOrigin(origins = "http://localhost:5173")
public class PredmetController {
    
    @Autowired
    private PredmetService predmetService;
    
    @Autowired
    private ArangoService arangoService;
    
    @GetMapping
    public List<Predmet> dobaviSvePredmete(){
        System.out.println("React trazi listu predmeta...");
        return arangoService.sviPredmeti();
    }
    
    @PostMapping("/dodaj")
    public String dodajPredmet(@RequestBody Predmet p){
        try {
            predmetService.dodajPredmet(p);
            return "Uspeh: Predmet '" + p.getNazivPredmeta() + "' je uspesno kreiran!";
        }catch (Exception e){
            return "Greska pri kreiranju predmeta: " + e.getMessage();
        }
    }
}
