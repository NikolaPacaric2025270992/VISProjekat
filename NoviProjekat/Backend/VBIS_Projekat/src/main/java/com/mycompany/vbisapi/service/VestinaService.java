/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.service;

import com.mycompany.vbisapi.model.Vestina;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author nikol
 */
@Service
public class VestinaService {
    
    @Autowired
    private ArangoService arango;
    
    @Autowired
    private FusekiService fuseki;
    
    public void dodajVestinu(Vestina v){
        arango.sacuvajVestinu(v);
        fuseki.sacuvajVestinuURDF(v);
        System.out.println("VestinaService: Vestina '" + 
                            v.getNaziv() + "' je sinhronizovana.");
    }
}
