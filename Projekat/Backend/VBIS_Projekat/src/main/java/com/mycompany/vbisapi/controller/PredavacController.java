/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.controller;

import com.mycompany.vbisapi.model.Predavac;
import com.mycompany.vbisapi.service.PredavacService;
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
@RequestMapping("/api/predavaci")
@CrossOrigin(origins = "http://localhost:5173")
public class PredavacController {
    
    @Autowired
    private PredavacService predavacService;
    
    @PostMapping("/registracija")
    public String registrujPredavaca(@RequestBody Predavac p){
        try{
            predavacService.registrujPredavaca(p);
            return "Uspeh: Predavac " + p.getIme() + " " + p.getPrezime() + " je uspesno registrovan!";
        }catch (Exception e){
            return "greska pri registraciji predavaca: " + e.getMessage();
        }
    }
}
