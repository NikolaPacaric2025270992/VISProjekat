/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.service;

import com.mycompany.vbisapi.model.*;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.springframework.stereotype.Service;

@Service
public class FusekiService {
    
    private static final String FUSEKI_URL = "http://localhost:3030/vbis_dataset/update";
    private static final String NS = "http://www.vbis-projekat.rs/model#";
    
    // Dodajemo standardni RDF prefiks kao konstantu
    private static final String RDF_PREFIX = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ";
    private static final String MY_PREFIX = "PREFIX : <" + NS + "> ";

    public void sacuvajStudentaURDF(Student s){
        String studentID = s.getEmail().replace("@", "_").replace(".", "_");
        String query = MY_PREFIX + RDF_PREFIX + 
                       "INSERT DATA { :" + studentID + " rdf:type :Student ; " +
                       ":imaIme \"" + s.getIme() + "\" ; :imaPrezime \"" + s.getPrezime() + "\" . }";
        izvrsiUpdate(query);
        System.out.println("Student " + s.getIme() + " uspesno poslat u Fuseki!");
    }

    public void sacuvajAgencijuURDF(Agencija a) {
        String query = MY_PREFIX + RDF_PREFIX + 
                       "INSERT DATA { :" + a.getId() + " rdf:type :Agencija ; :imaNaziv \"" + a.getNazivAgencije() + "\" . }";
        izvrsiUpdate(query);
        System.out.println("Agencija " + a.getNazivAgencije() + " poslata u Fuseki!");
    }

    public void sacuvajPredmetURDF(Predmet p) {
        String query = MY_PREFIX + RDF_PREFIX + 
                       "INSERT DATA { :" + p.getId() + " rdf:type :Predmet ; :imaNazivPredmeta \"" + p.getNazivPredmeta() + "\" ; " +
                        ":prenosiVestinu :" + p.getVestinaId().getId() + " .}";
        izvrsiUpdate(query);
        System.out.println("Predmet " + p.getNazivPredmeta() + " poslat u Fuseki!");
    }

    public void sacuvajPolaganjeURDF(Polaganje pol) {
        String query = MY_PREFIX + RDF_PREFIX + 
                       "INSERT DATA { :" + pol.getId() + " rdf:type :Polaganje ; " +
                       ":imaStudenta :" + pol.getStudentId() + " ; " +
                       ":imaPredmet :" + pol.getPredmetId() + " ; " +
                       ":imaOcenu " + pol.getOcena() + " . }";
        izvrsiUpdate(query);
        System.out.println("Polaganje povezano u Fuseki grafu!");
    }
    
    public void sacuvajOglasURDF(Oglas o) {
        String prioritetID = o.getPrioritet().toString().substring(0, 1).toUpperCase() 
                           + o.getPrioritet().toString().substring(1).toLowerCase();
        
        String nivoID = o.getZahtevaniNivo().toString().substring(0, 1).toUpperCase() 
                      + o.getZahtevaniNivo().toString().substring(1).toLowerCase();

        String query = MY_PREFIX + RDF_PREFIX + 
                       "INSERT DATA { :" + o.getId() + " rdf:type :Oglas ; " +
                       ":imaNaslov \"" + o.getNaslov() + "\" ; " +
                       ":imaPrioritet :" + prioritetID + " ; :zahtevaNivo :" + nivoID + " ; " +
                       ":traziVestinu :" + o.getVestinaId().getId() + " . }";
        izvrsiUpdate(query);
        System.out.println("Oglas '" + o.getNaslov() + "' uspešno sinhronizovan!");
    }
    
    public void sacuvajVestinuURDF(Vestina v) {
    String query = MY_PREFIX + RDF_PREFIX + 
                       "INSERT DATA { :" + v.getId() + " rdf:type :Vestina ; :imaNaziv \"" + v.getNaziv() + "\" . }";
    izvrsiUpdate(query);
    }

    private void izvrsiUpdate(String sparql) {
        try {
            UpdateRequest request = UpdateFactory.create(sparql);
            UpdateExecutionFactory.createRemote(request, FUSEKI_URL).execute();
        } catch (Exception e) {
            System.err.println("Greska u Fuseki komunikaciji: " + e.getMessage());
        }
    }
}