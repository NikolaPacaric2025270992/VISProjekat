/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.service;

import com.mycompany.vbisapi.model.Predavac;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author nikol
 */
@Service
public class PredavacService {
    
    @Autowired
    private ArangoService arango;
    
    @Autowired
    private FusekiService fuseki;
    
    public void registrujPredavaca(Predavac p){
        arango.sacuvajPredavaca(p);
        fuseki.sacuvajPredavacaURDF(p);
        System.out.println("PredavacService: Predavac " 
                            + p.getIme() + " " 
                            + p.getPrezime() + " je sinhronizovan.");
    }
}
