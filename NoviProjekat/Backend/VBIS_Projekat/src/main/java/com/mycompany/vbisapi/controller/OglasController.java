/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.controller;

import com.mycompany.vbisapi.model.Oglas;
import com.mycompany.vbisapi.model.RangiraniStudent;
import com.mycompany.vbisapi.service.FusekiService;
import com.mycompany.vbisapi.service.ImportService;
import com.mycompany.vbisapi.service.OglasService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author nikol
 */
@RestController
@RequestMapping("/api/oglasi")
public class OglasController {
    
    @Autowired
    private FusekiService fusekiService;
    
    @Autowired
    private OglasService oglasService;
    
    @Autowired
    private ImportService importService;
    
    
    @PostMapping("/postavi")
    public String postavioglas(@RequestBody Oglas o){
    try{
            oglasService.postaviOglas(o);
            return "Uspeh: Oglas '" + o.getNaslov() + "' je uspesno objavljen u oba sistema!";
        } catch (Exception e){
            e.printStackTrace();
            return "Greska pri postavljanju oglasa: " + e.getMessage();
        }
    }
    
    @PostMapping("/upload")
    public ResponseEntity<?> uploadOglasa(@RequestParam("fajl") MultipartFile fajl) {
        try {
            // 1. Validacija i parsiranje
            List<Oglas> noviOglasi = importService.obradiFajlSaOglasima(fajl);
            
            // 2. Čuvanje u bazu (i Arango i Fuseki)
            int brojSacuvanih = 0;
            for (Oglas o : noviOglasi) {
                oglasService.postaviOglas(o);
                brojSacuvanih++;
            }
            
            return ResponseEntity.ok("Uspešno validirano i sačuvano " + brojSacuvanih + " oglasa.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Greška pri uploadu: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}/rang-lista")
    public List<RangiraniStudent> getRangLista(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int stranica,
            @RequestParam(defaultValue = "5") int poStranici) {
            
        return fusekiService.getRangListaStudenata(id, stranica, poStranici);
    }
    
    @GetMapping("/provera")
    public String provera(){
        return "OglasController je aktivan!";
    }
}
