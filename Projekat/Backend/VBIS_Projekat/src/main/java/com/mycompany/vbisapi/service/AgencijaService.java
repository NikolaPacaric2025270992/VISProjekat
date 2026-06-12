/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.service;

import com.mycompany.vbisapi.model.Agencija;
import com.mycompany.vbisapi.model.Oglas;
import java.util.List;
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

    @Autowired
    private OglasService oglasService;
    
    public Agencija login(String email, String lozinka){
        return arango.loginAgencija(email, lozinka);
    }
    
    public void registrujAgenciju(Agencija a){
        arango.sacuvajAgenciju(a);
        try {
            fuseki.sacuvajAgencijuURDF(a);
        } catch (RuntimeException e) {
            SinhronizacijaHelper.rollbackArangoUpis(
                    "agencije",
                    a.getId(),
                    () -> arango.obrisiAgenciju(a.getId()),
                    e);
            throw e;
        }
        
        System.out.println("AgencijaService: Agencija '" + a.getNazivAgencije() + "' je u oba sistema!");
    }
    
    public void azurirajAgenciju(Agencija a){
        arango.azurirajAgenciju(a);
        fuseki.azurirajAgencijuURDF(a);
    }

    public void promeniLozinku(String email, String staraLozinka, String novaLozinka) {
        if (novaLozinka == null || novaLozinka.length() < 5) {
            throw new IllegalArgumentException("Nova lozinka mora imati bar 5 karaktera.");
        }

        Agencija agencija = arango.loginAgencija(email, staraLozinka);
        if (agencija == null) {
            throw new IllegalArgumentException("Trenutna lozinka nije tačna.");
        }

        arango.promeniLozinkuAgencije(email, novaLozinka);
    }
    
    public void obrisiAgenciju(String id) {
        List<Oglas> oglasi = arango.nadjiOglasePoAgenciji(id);
        for (Oglas oglas : oglasi) {
            oglasService.obrisiOglas(oglas.getId());
        }

        fuseki.obrisiKorisnikaIzRDF(id);
        arango.obrisiAgenciju(id);
        System.out.println("AgencijaService: Agencija " + id + " je obrisana.");
    }
}
