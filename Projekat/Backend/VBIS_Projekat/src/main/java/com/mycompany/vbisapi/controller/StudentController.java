/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.controller;

import com.mycompany.vbisapi.model.LoginDTO;
import com.mycompany.vbisapi.model.Polaganje;
import com.mycompany.vbisapi.model.PreporuceniOglas;
import com.mycompany.vbisapi.model.Student;
import com.mycompany.vbisapi.service.ExportService;
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
    
    @Autowired
    private ExportService exportService;
    
    @PostMapping("/login")
    public ResponseEntity<?> loginStudent(@RequestBody LoginDTO loginPodaci) {
        Student s = studentService.login(loginPodaci.getEmail(), loginPodaci.getLozinka());
        if (s != null) {
            return ResponseEntity.ok(s); 
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Pogrešan email ili lozinka!");
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> azurirajStudenta(@RequestBody Student s) {
        try {
            studentService.azurirajStudenta(s);
            return ResponseEntity.ok(s);
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
            studentService.obrisiStudenta(id);
            fusekiService.obrisiKorisnikaIzRDF(id);
            
            return ResponseEntity.ok("Nalog studenta je uspešno obrisan iz svih baza.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Greška pri brisanju: " + e.getMessage());
        }
    }
    
    @PostMapping("/import-polaganja")
    public ResponseEntity<?> importPolaganja(
            @RequestParam("fajl") MultipartFile fajl,
            @RequestParam("studentId") String studentId) {
        try {
            List<Polaganje> novaPolaganja = importService.obradiFajlSaPolaganjima(fajl);

            int brojSacuvanih = 0;
            for (Polaganje p : novaPolaganja) {
                p.setStudentId(studentId);
                p.setId("polaganje_" + System.currentTimeMillis() + "_" + brojSacuvanih);
                
                polaganjeService.dodajPolaganje(p);
                brojSacuvanih++;
            }
            return ResponseEntity.ok("Uspešno validirano i sačuvano " + brojSacuvanih + " položenih ispita.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Greška pri importu polaganja: " + e.getMessage());
        }
    }
    
    @GetMapping("/{email}/preporuke")
    public List<PreporuceniOglas> getPreporuke(
            @PathVariable String email,
            @RequestParam(defaultValue = "1") int stranica,
            @RequestParam(defaultValue = "5") int poStranici) {
            
        return fusekiService.getPreporukeZaStudenta(email, stranica, poStranici);
    }
    
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
    
    @GetMapping("/aktivni/export")
    public ResponseEntity<byte[]> exportAktivniStudenti(@RequestParam(defaultValue = "json") String format) {
        try {
            List<Student> aktivni = studentService.nadjiAktivneStudente();
            byte[] fajl = format.equalsIgnoreCase("xml") ? exportService.eksportujUXml(aktivni) : exportService.eksportujUJson(aktivni);
            String ekstenzija = format.equalsIgnoreCase("xml") ? ".xml" : ".json";
            
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"aktivni_studenti" + ekstenzija + "\"")
                    .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                    .body(fajl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{email}/preporuke/export")
    public ResponseEntity<byte[]> exportPreporukeStudenta(@PathVariable String email, @RequestParam(defaultValue = "json") String format) {
        try {
            List<PreporuceniOglas> preporuke = fusekiService.getPreporukeZaStudenta(email, 1, 100);
            byte[] fajl = format.equalsIgnoreCase("xml") ? exportService.eksportujUXml(preporuke) : exportService.eksportujUJson(preporuke);
            String ekstenzija = format.equalsIgnoreCase("xml") ? ".xml" : ".json";
            
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"preporuceni_oglasi" + ekstenzija + "\"")
                    .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                    .body(fajl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/provera")
    public String provera(){
        return "StudentController je aktivan i spreman!";
    }
}
