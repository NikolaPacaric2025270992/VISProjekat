/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.controller;

import com.mycompany.vbisapi.model.Oglas;
import com.mycompany.vbisapi.model.RangiraniStudent;
import com.mycompany.vbisapi.service.ExportService;
import com.mycompany.vbisapi.service.FusekiService;
import com.mycompany.vbisapi.service.ImportService;
import com.mycompany.vbisapi.service.OglasService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@CrossOrigin(origins = "http://localhost:5173")
public class OglasController {
    
    @Autowired
    private FusekiService fusekiService;
    
    @Autowired
    private OglasService oglasService;
    
    @Autowired
    private ImportService importService;
    
    @Autowired
    private ExportService exportService;
    
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
    
    @DeleteMapping("/obrisi/{id}")
    public ResponseEntity<?> obrisiOglas(@PathVariable String id) {
        try {
            // Kontroler samo delegira posao Servisu!
            oglasService.obrisiOglas(id); 
            return ResponseEntity.ok("Oglas uspešno obrisan.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Greška pri brisanju: " + e.getMessage());
        }
    }
    
    // IZMENJENO: Putanja je sada /import, a metoda prima i agencijaId
    @PostMapping("/import")
    public ResponseEntity<?> importOglasa(
            @RequestParam("fajl") MultipartFile fajl,
            @RequestParam("agencijaId") String agencijaId) {
        
        try {
            // 1. Validacija i parsiranje (ImportService ostaje isti)
            List<Oglas> noviOglasi = importService.obradiFajlSaOglasima(fajl);
            
            // 2. Čuvanje u bazu (i Arango i Fuseki)
            int brojSacuvanih = 0;
            for (Oglas o : noviOglasi) {
                
                // KLJUČNO: Pregazimo agencijaId iz fajla stvarnim ID-jem ulogovane agencije
                o.setAgencijaId(agencijaId);
                
                // (Opciono) Ako želiš da budeš 100% siguran da ID oglasa neće napraviti konflikt, 
                // možeš ovde dodati i generisanje jedinstvenog ID-ja:
                // o.setId("oglas_" + System.currentTimeMillis() + "_" + brojSacuvanih);
                
                oglasService.postaviOglas(o);
                brojSacuvanih++;
            }
            
            return ResponseEntity.ok("Uspešno validirano i sačuvano " + brojSacuvanih + " oglasa.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Greška pri importu: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}/rang-lista")
    public List<RangiraniStudent> getRangLista(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int stranica,
            @RequestParam(defaultValue = "5") int poStranici) {
            
        return fusekiService.getRangListaStudenata(id, stranica, poStranici);
    }
    
    @GetMapping("/agencija/{agencijaId}")
    public List<Oglas> dobijOglaseAgencije(@PathVariable String agencijaId) {
        return oglasService.nadjiOglaseAgencije(agencijaId);
    }
    
    // NOVO: Ruta za dohvatanje apsolutno svih oglasa (za tržište rada kod Studenta)
    @GetMapping("/svi")
    public ResponseEntity<List<Oglas>> getSviOglasi() {
        try {
            List<Oglas> sviOglasi = oglasService.dobijSveOglase();
            return ResponseEntity.ok(sviOglasi);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // 3. EXPORT: Svi oglasi sa tržišta (Za Studenta)
    @GetMapping("/svi/export")
    public ResponseEntity<byte[]> exportSviOglasi(@RequestParam(defaultValue = "json") String format) {
        try {
            List<Oglas> sviOglasi = oglasService.dobijSveOglase();
            byte[] fajl = format.equalsIgnoreCase("xml") ? exportService.eksportujUXml(sviOglasi) : exportService.eksportujUJson(sviOglasi);
            String ekstenzija = format.equalsIgnoreCase("xml") ? ".xml" : ".json";
            
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"svi_oglasi" + ekstenzija + "\"")
                    .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                    .body(fajl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 4. EXPORT: Idealni kandidati za konkretan oglas (Za Agenciju)
    @GetMapping("/{id}/rang-lista/export")
    public ResponseEntity<byte[]> exportRangListaOglasa(@PathVariable String id, @RequestParam(defaultValue = "json") String format) {
        try {
            List<RangiraniStudent> kandidati = fusekiService.getRangListaStudenata(id, 1, 100);
            byte[] fajl = format.equalsIgnoreCase("xml") ? exportService.eksportujUXml(kandidati) : exportService.eksportujUJson(kandidati);
            String ekstenzija = format.equalsIgnoreCase("xml") ? ".xml" : ".json";
            
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"idealni_kandidati_" + id + ekstenzija + "\"")
                    .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                    .body(fajl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/provera")
    public String provera(){
        return "OglasController je aktivan!";
    }
}
