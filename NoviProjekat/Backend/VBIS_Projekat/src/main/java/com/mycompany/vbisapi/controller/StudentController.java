/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.controller;

import com.mycompany.vbisapi.model.LoginDTO;
import com.mycompany.vbisapi.model.Polaganje;
import com.mycompany.vbisapi.model.PreporuceniOglas;
import com.mycompany.vbisapi.model.Student;
import com.mycompany.vbisapi.service.FusekiService;
import com.mycompany.vbisapi.service.ImportService;
import com.mycompany.vbisapi.service.PolaganjeService;
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
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author nikol
 */
@RestController
@RequestMapping("/api/studenti")
@CrossOrigin(origins = "http://localhost:5173")
public class StudentController {
    
    @Autowired
    private FusekiService fusekiService;
    
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private ImportService importService;
    
    @Autowired
    private PolaganjeService polaganjeService;
    
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
    
    @DeleteMapping("/obrisi/{id}")
    public ResponseEntity<?> obrisiStudenta(@PathVariable String id) {
        try {
            // 1. Brišemo iz ArangoDB
            studentService.obrisiStudenta(id); // Pretpostavljam da ćeš u StudentService pozvati arango.obrisiStudenta(id)
            
            // 2. Brišemo iz Fusekija
            fusekiService.obrisiKorisnikaIzRDF(id);
            
            return ResponseEntity.ok("Nalog studenta je uspešno obrisan iz svih baza.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Greška pri brisanju: " + e.getMessage());
        }
    }
    
    @PostMapping("/upload-polaganja")
    public ResponseEntity<?> uploadPolaganja(@RequestParam("fajl") MultipartFile fajl) {
        try {
            // 1. Validacija i parsiranje
            List<Polaganje> novaPolaganja = importService.obradiFajlSaPolaganjima(fajl);
            
            // 2. Čuvanje u bazu (Arango i Fuseki)
            int brojSacuvanih = 0;
            for (Polaganje p : novaPolaganja) {
                polaganjeService.dodajPolaganje(p);
                brojSacuvanih++;
            }
            
            return ResponseEntity.ok("Uspešno validirano i sačuvano " + brojSacuvanih + " položenih ispita.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Greška pri uploadu polaganja: " + e.getMessage());
        }
    }
    
    @GetMapping("/{email}/preporuke")
    public List<PreporuceniOglas> getPreporuke(
            @PathVariable String email,
            @RequestParam(defaultValue = "1") int stranica,
            @RequestParam(defaultValue = "5") int poStranici) {
            
        return fusekiService.getPreporukeZaStudenta(email, stranica, poStranici);
    }
    
    // NOVO: Ruta za dohvatanje baze talenata (svih studenata koji traže posao)
    @GetMapping("/aktivni")
    public ResponseEntity<List<Student>> getAktivniStudenti() {
        try {
            List<Student> aktivniStudenti = studentService.nadjiAktivneStudente();
            return ResponseEntity.ok(aktivniStudenti);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/provera")
    public String provera(){
        return "StudentController je aktivan i spreman!";
    }
}
