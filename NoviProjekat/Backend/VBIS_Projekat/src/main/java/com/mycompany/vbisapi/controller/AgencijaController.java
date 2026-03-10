/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.controller;

import com.mycompany.vbisapi.model.Agencija;
import com.mycompany.vbisapi.service.AgencijaService;
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
@RequestMapping("/api/agencija")
public class AgencijaController {
    
    @Autowired
    private AgencijaService agencijaService;
    
    @PostMapping("/registracija")
    public String registruj(@RequestBody Agencija a){
        try{
            agencijaService.registrujAgenciju(a);
            return "Uspeh: Agencija " + a.getNazivAgencije() + " je registrovana!";
        }catch (Exception e){
            return "Greska pri registraciji agencije: " + e.getMessage();
        }
    }
}
