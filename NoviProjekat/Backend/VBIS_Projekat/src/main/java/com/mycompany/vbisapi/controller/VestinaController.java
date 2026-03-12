/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.controller;

import com.mycompany.vbisapi.model.Vestina;
import com.mycompany.vbisapi.service.VestinaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author nikol
 */
@RestController
@RequestMapping("/api/vestine")
public class VestinaController {
    
    @Autowired
    private VestinaService vestinaService;
    
    @PostMapping("/dodaj")
    public String dodajVestinu(@RequestBody Vestina v){
        try {
            vestinaService.dodajVestinu(v);
            return "Uspeh: Vestina '" + v.getNaziv() + "' je uspesno dodata u sistem!";
        }catch (Exception e){
            return "Greska pri dodavanju vestine: " + e.getMessage();
        }
    }
}
