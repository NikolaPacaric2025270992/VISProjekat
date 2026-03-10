/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.service;

import com.mycompany.vbisapi.model.Agencija;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author nikol
 */
@Service
public class AgencijaService {
    
    @Autowired
    private ArangoService arango;
    
    @Autowired
    private FusekiService fuseki;
    
    public void registrujAgenciju(Agencija a){
        arango.sacuvajAgenciju(a);
        fuseki.sacuvajAgencijuURDF(a);
        
        System.out.println("AgencijaService: Agencija '" + a.getNazivAgencije() + "' je u oba sistema!");
    }
}
