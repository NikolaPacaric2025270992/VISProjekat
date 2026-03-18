/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.controller;

import com.mycompany.vbisapi.model.Agencija;
import com.mycompany.vbisapi.model.LoginDTO;
import com.mycompany.vbisapi.service.AgencijaService;
import com.mycompany.vbisapi.service.FusekiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author nikol
 */
@RestController
@RequestMapping("/api/agencija")
@CrossOrigin(origins = "http://localhost:5173")
public class AgencijaController {
    
    @Autowired
    private AgencijaService agencijaService;
    
    @Autowired
    private FusekiService fusekiService;
    
    @PostMapping("/login")
    public ResponseEntity<?> loginAgencija(@RequestBody LoginDTO loginPodaci) {
        Agencija a = agencijaService.login(loginPodaci.getEmail(), loginPodaci.getLozinka());
        if (a != null) {
            return ResponseEntity.ok(a); 
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Pogrešan email ili lozinka!"); 
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> azurirajAgenciju(@RequestBody Agencija a) {
        try {
            agencijaService.azurirajAgenciju(a);
            fusekiService.obrisiKorisnikaIzRDF(a.getId()); // Brišemo stari čvor
            fusekiService.sacuvajAgencijuURDF(a);          // Upisujemo novi
            return ResponseEntity.ok(a); 
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Greška pri ažuriranju.");
        }
    }
    
    @PostMapping("/registracija")
    public String registruj(@RequestBody Agencija a){
        try{
            agencijaService.registrujAgenciju(a);
            return "Uspeh: Agencija " + a.getNazivAgencije() + " je registrovana!";
        }catch (Exception e){
            return "Greska pri registraciji agencije: " + e.getMessage();
        }
    }
    
    // 4. DELETE (Brisanje naloga)
    @DeleteMapping("/obrisi/{id}")
    public ResponseEntity<?> obrisiAgenciju(@PathVariable String id) {
        try {
            agencijaService.obrisiAgenciju(id); // Briše iz Aranga
            fusekiService.obrisiKorisnikaIzRDF(id); // Briše iz Fusekija
            return ResponseEntity.ok("Nalog agencije uspešno obrisan.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Greška pri brisanju.");
        }
    }
}
