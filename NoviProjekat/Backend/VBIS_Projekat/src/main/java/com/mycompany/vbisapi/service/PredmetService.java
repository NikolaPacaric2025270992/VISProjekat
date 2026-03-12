/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.service;

import com.mycompany.vbisapi.model.Predmet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author nikol
 */
@Service
public class PredmetService {
    
    @Autowired
    private ArangoService arango;
    
    @Autowired
    private FusekiService fuseki;
    
    public void dodajPredmet(Predmet p){
        arango.sacuvajPredmet(p);
        fuseki.sacuvajPredmetURDF(p);
        System.out.println("PredmetService: Predmet '" + p.getNazivPredmeta() + "' je sinhronizovan.");
    }
}
