/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.service;

import com.mycompany.vbisapi.model.Oglas;
import java.util.List;
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
        
        // Čuvamo oglas u ArangoDB (sa ugnježdenim zahtevima)
        arango.sacuvajOglas(o);
        
        // Čuvamo oglas u Fuseki (sa N-ary relacijama)
        fuseki.sacuvajOglasURDF(o);
        
        System.out.println("OglasService: Oglas '" + 
                            o.getNaslov() + "' je potpuno sinhronizovan.");
    }
    
    public List<Oglas> nadjiOglaseAgencije(String agencijaId) {
        return arango.nadjiOglasePoAgenciji(agencijaId);
    }
}
