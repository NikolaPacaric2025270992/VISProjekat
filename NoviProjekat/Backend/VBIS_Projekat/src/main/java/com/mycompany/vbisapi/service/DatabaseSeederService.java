/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.service;

import com.mycompany.vbisapi.model.Agencija;
import com.mycompany.vbisapi.model.Oglas;
import com.mycompany.vbisapi.model.Polaganje;
import com.mycompany.vbisapi.model.Predavac;
import com.mycompany.vbisapi.model.Predmet;
import com.mycompany.vbisapi.model.Student;
import com.mycompany.vbisapi.model.Vestina;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 *
 * @author nikol
 */
@Service
public class DatabaseSeederService {
    
    @Autowired
    private ArangoService arango;
    
    @Autowired
    private FusekiService fuseki;
    
    @EventListener(ApplicationReadyEvent.class)
    public void pokreniSinhronizaciju(){
        System.out.println("=== POKRETANJE SINHRONIZACIJE: Arango -> Fuseki ===");
        
        fuseki.ocistiSve();
        System.out.println("Fuseki baza je ociscnjena.");
        
        List<Vestina> sveVestine = arango.sveVestine();
        List<Predmet> sviPredmeti = arango.sviPredmeti();
        List<Predavac> sviPredavaci = arango.sviPredavaci();
        
        List<Student> sviStudenti = arango.sviStudenti();
        List<Agencija> sveAgencije = arango.sveAgencije();
        List<Polaganje> svaPolaganja = arango.svaPolaganja();
        List<Oglas> sviOglasi = arango.sviOglasi();
        
        if (sveVestine.isEmpty() || sviPredmeti.isEmpty() || sviPredavaci.isEmpty()){
            throw new IllegalStateException(
                "\n\n[KRITICNA GRESKA] ArangoDB nema sve potrebne podatke! \n" +
                "Trenutno stanje: \n" +
                "- Vestine: " + sveVestine.size() + "\n" +
                "- Predmeti: " + sviPredmeti.size() + "\n" +
                "- Predavaci: " + sviPredavaci.size() + "\n" +
                "Sve tri kolekcije MORAJU biti popunjene da bi ontologija radila. \n" +
                "Ubaci JSON fajlove u ArangoDB i pokusaj ponovo!\n\n"
            );
        }
        
        System.out.println("Pronadjeni podaci u ArangoDB. Zapocinjem upis u Fuseki...");
        
        // Prvo infrastruktura
        sveVestine.forEach(v -> fuseki.sacuvajVestinuURDF(v));
        sviPredavaci.forEach(pr -> fuseki.sacuvajPredavacaURDF(pr));
        sviPredmeti.forEach(p -> fuseki.sacuvajPredmetURDF(p));
        
        // Zatim korisnici
        sviStudenti.forEach(s -> fuseki.sacuvajStudentaURDF(s));
        sveAgencije.forEach(a -> fuseki.sacuvajAgencijuURDF(a));
        
        // I na kraju dinamički podaci (Ispiti i Oglasi)
        svaPolaganja.forEach(pol -> fuseki.sacuvajPolaganjeURDF(pol));
        sviOglasi.forEach(o -> fuseki.sacuvajOglasURDF(o));
        
        System.out.println("Sinhronizacija je uspesno zavrsena!");
        System.out.println("SISTEM SPREMAN ZA RAD!");
    }
}
