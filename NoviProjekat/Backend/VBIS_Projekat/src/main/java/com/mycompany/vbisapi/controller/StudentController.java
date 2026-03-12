/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.controller;

import com.mycompany.vbisapi.model.LoginDTO;
import com.mycompany.vbisapi.model.PreporuceniOglas;
import com.mycompany.vbisapi.model.Student;
import com.mycompany.vbisapi.service.FusekiService;
import com.mycompany.vbisapi.service.StudentService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author nikol
 */
@RestController
@RequestMapping("/api/studenti")
public class StudentController {
    
    @Autowired
    private FusekiService fusekiService;
    
    @Autowired
    private StudentService studentService;
    
    @PostMapping("/login")
    public ResponseEntity<?> loginStudent(@RequestBody LoginDTO loginPodaci) {
        Student s = studentService.login(loginPodaci.getEmail(), loginPodaci.getLozinka());
        if (s != null) {
            return ResponseEntity.ok(s); // Vraća kompletnog studenta (200 OK)
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Pogrešan email ili lozinka!"); // 401 Error
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> azurirajStudenta(@RequestBody Student s) {
        try {
            studentService.azurirajStudenta(s);
            return ResponseEntity.ok(s); // Vraćamo ažuriranog studenta nazad
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Greška pri ažuriranju.");
        }
    }
    
    @PostMapping("/registracija")
    public String registracijaStudenta(@RequestBody Student s){
        try {
            studentService.registrujStudenta(s);
            return "Uspeh: Student " + s.getIme() + " " + s.getPrezime() + " je registrovan u oba sistema!";
        } catch (Exception e){
            return "Greska pri registraciji: " + e.getMessage();
        }
    }
    
    @GetMapping("/{email}/preporuke")
    public List<PreporuceniOglas> getPreporuke(@PathVariable String email) {
        return fusekiService.getPreporukeZaStudenta(email);
    }
    
    @GetMapping("/provera")
    public String provera(){
        return "StudentController je aktivan i spreman!";
    }
}
