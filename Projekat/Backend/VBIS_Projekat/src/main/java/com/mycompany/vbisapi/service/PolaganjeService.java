/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.service;

import com.mycompany.vbisapi.model.Polaganje;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author nikol
 */
@Service
public class PolaganjeService {
    
    @Autowired
    private ArangoService arango;
    
    @Autowired
    private FusekiService fuseki;
    
    public void dodajPolaganje(Polaganje p){
        validirajNovoPolaganje(p);

        arango.sacuvajPolaganje(p);
        try {
            fuseki.sacuvajPolaganjeURDF(p);
        } catch (RuntimeException e) {
            SinhronizacijaHelper.rollbackArangoUpis(
                    "polaganja",
                    p.getId(),
                    () -> arango.obrisiPolaganje(p.getId()),
                    e);
            throw e;
        }
        System.out.println("PolaganjeService: Polaganje uspesno evidentirano u oba sistema.");
    }
    
    public void obrisiPolaganje(String id) {
        fuseki.obrisiPolaganjeIzRDF(id);
        arango.obrisiPolaganje(id);
        System.out.println("PolaganjeService: Polaganje " + id + " uspesno uklonjeno iz obe baze.");
    }

    public void validirajNovoPolaganje(Polaganje p) {
        if (p.getId() == null || p.getId().isBlank()) {
            throw new IllegalArgumentException("Polaganje mora imati ID.");
        }

        if (p.getStudentId() == null || p.getStudentId().isBlank()) {
            throw new IllegalArgumentException("Polaganje mora imati ID studenta.");
        }

        if (p.getPredmetId() == null || p.getPredmetId().isBlank()) {
            throw new IllegalArgumentException("Polaganje mora imati ID predmeta.");
        }

        if (p.getOcena() < 6 || p.getOcena() > 10) {
            throw new IllegalArgumentException("Ocena mora biti izmedju 6 i 10.");
        }

        if (!arango.postojiPredmet(p.getPredmetId())) {
            throw new IllegalArgumentException("Predmet '" + p.getPredmetId() + "' ne postoji u bazi.");
        }

        if (arango.postojiPolaganjeZaStudentaIPredmet(p.getStudentId(), p.getPredmetId())) {
            throw new IllegalArgumentException("Student vec ima evidentirano polaganje za predmet '" + p.getPredmetId() + "'.");
        }
    }
}
