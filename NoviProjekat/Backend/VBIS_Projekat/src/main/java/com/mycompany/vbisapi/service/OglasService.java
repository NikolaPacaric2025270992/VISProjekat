/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.service;

import com.mycompany.vbisapi.model.Oglas;
import com.mycompany.vbisapi.model.OglasVestina;
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
        // Prolazimo kroz listu asocijativnih objekata OglasVestina
        if (o.getZahtevaneVestine() != null){
            for (OglasVestina ov : o.getZahtevaneVestine()){ 
                // Izvlačimo samu veštinu iz asocijativnog objekta
                Vestina v = ov.getVestina(); 
                
                // Osiguravamo da veština postoji u obe baze pre nego što vežemo oglas za nju
                fuseki.sacuvajVestinuURDF(v);
                arango.sacuvajVestinu(v); 
                
                System.out.println("Osigurana vestina: " + v.getNaziv());
            }
        }
        
        // Čuvamo oglas u ArangoDB (sa ugnježdenim zahtevima)
        arango.sacuvajOglas(o);
        
        // Čuvamo oglas u Fuseki (sa N-ary relacijama)
        fuseki.sacuvajOglasURDF(o);
        
        System.out.println("OglasService: Oglas '" + 
                            o.getNaslov() + "' je potpuno sinhronizovan.");
    }      
}
