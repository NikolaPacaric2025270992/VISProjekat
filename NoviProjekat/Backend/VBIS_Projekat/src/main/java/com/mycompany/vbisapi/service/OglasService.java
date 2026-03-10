/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.service;

import com.mycompany.vbisapi.model.Oglas;
import com.mycompany.vbisapi.model.Vestina;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author nikol
 */
@Service
public class OglasService {
    
    @Autowired
    private ArangoService arango;
    
    @Autowired
    private FusekiService fuseki;
    
    public void postaviOglas(Oglas o){
        if (o.getZahtevaneVestine() != null){
            for (Vestina v : o.getZahtevaneVestine()){
                fuseki.sacuvajVestinuURDF(v);
                System.out.println("osigurana vestina: " + v.getNaziv());
            }
        }
        
        arango.sacuvajOglas(o);
        fuseki.sacuvajOglasURDF(o);
        
        System.out.println("OglasService: Oglas '" + o.getNaslov() + "' je potpuno sinhronizovan.");
    }       
}
