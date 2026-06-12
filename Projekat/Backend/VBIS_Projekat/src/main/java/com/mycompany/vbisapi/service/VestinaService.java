/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.service;

import com.mycompany.vbisapi.model.Vestina;
import java.util.List;
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
        try {
            fuseki.sacuvajVestinuURDF(v);
        } catch (RuntimeException e) {
            SinhronizacijaHelper.rollbackArangoUpis(
                    "vestine",
                    v.getId(),
                    () -> arango.obrisiVestinu(v.getId()),
                    e);
            throw e;
        }
        System.out.println("VestinaService: Vestina '" + 
                            v.getNaziv() + "' je sinhronizovana.");
    }
    
    public List<Vestina> sveVestine() {
        return arango.sveVestine();
    }
}
