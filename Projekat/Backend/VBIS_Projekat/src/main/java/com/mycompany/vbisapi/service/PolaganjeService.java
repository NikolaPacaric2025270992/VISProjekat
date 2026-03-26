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
        arango.sacuvajPolaganje(p);
        fuseki.sacuvajPolaganjeURDF(p);
        System.out.println("PolaganjeService: Polaganje uspesno evidentirano u oba sistema.");
    }
    
    public void obrisiPolaganje(String id) {
        arango.obrisiPolaganje(id);
        fuseki.obrisiPolaganjeIzRDF(id);
        System.out.println("PolaganjeService: Polaganje " + id + " uspesno uklonjeno iz obe baze.");
    }
}
