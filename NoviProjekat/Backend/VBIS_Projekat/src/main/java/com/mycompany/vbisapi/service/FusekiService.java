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
        // Formatiranje nivoa za RDF (npr. "Napredni", "Srednji")
        String nivoIndiv = p.getNivoKojiNudi().toString().substring(0, 1).toUpperCase() 
                         + p.getNivoKojiNudi().toString().substring(1).toLowerCase();

        String query = MY_PREFIX + RDF_PREFIX + 
                       "INSERT DATA { " +
                       ":" + p.getId() + " rdf:type :Predmet ; " +
                       ":imaNaziv \"" + p.getNazivPredmeta() + "\" ; " +
                       ":prenosiVestinu :" + p.getVestina().getId() + " ; " +
                       ":nudiNivo :" + nivoIndiv + " . \n" +

                       // Veza: Predavac predaje Predmet
                       ":" + p.getPredavacId() + " :predaje :" + p.getId() + " . " +
                       "}";

        izvrsiUpdate(query);
        System.out.println("Predmet " + p.getNazivPredmeta() + " povezan u grafu sa nivoom i predavačem!");
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
    
    public void sacuvajPredavacaURDF(Predavac pr) {
        String query = MY_PREFIX + RDF_PREFIX + 
                       "INSERT DATA { :" + pr.getId() + " rdf:type :Predavac ; " +
                       ":imaIme \"" + pr.getIme() + "\" ; " +
                       ":imaPrezime \"" + pr.getPrezime() + "\" ; " +
                       ":imaTitulu \"" + pr.getTitula() + "\" . }";
        izvrsiUpdate(query);
        System.out.println("Predavač " + pr.getIme() + " " + pr.getPrezime() + " poslat u Fuseki!");
    }
    
    public void sacuvajOglasURDF(Oglas o) {
        StringBuilder triples = new StringBuilder();

        // 1. Definišemo Oglas i njegov naziv (imaNaziv se koristi za Oglas u RDF-u) [cite: 9, 25]
        triples.append(":").append(o.getId()).append(" rdf:type :Oglas ; ");
        triples.append(":imaNaziv \"").append(o.getNaslov()).append("\" . \n");

        // 2. Agencija objavljuje oglas [cite: 4, 28]
        triples.append(":").append(o.getAgencijaId()).append(" :objavljuje :").append(o.getId()).append(" . \n");

        // 3. Prolazimo kroz listu OglasVestina (N-ary relacija)
        if (o.getZahtevaneVestine() != null) {
            for (OglasVestina ov : o.getZahtevaneVestine()) {
                // Generišemo unikatan ID za čvor ZahtevanaVestina (npr. Zahtev_Oglas1_Java)
                String zahtevId = "Zahtev_" + o.getId() + "_" + ov.getVestina().getId();

                // Povezujemo Oglas sa Zahtevom 
                triples.append(":").append(o.getId()).append(" :imaZahtev :").append(zahtevId).append(" . \n");

                // Definišemo čvor ZahtevanaVestina [cite: 27]
                triples.append(":").append(zahtevId).append(" rdf:type :ZahtevanaVestina ; \n");

                // Povezujemo sa konkretnom veštinom 
                triples.append("  :odnosiSeNaVestinu :").append(ov.getVestina().getId()).append(" ; \n");

                // Formatiranje nivoa i prioriteta da odgovara tvom RDF-u (npr. "Napredni", "Visok") [cite: 24, 26]
                String nivoIndiv = ov.getNivo().toString().substring(0, 1).toUpperCase() 
                                 + ov.getNivo().toString().substring(1).toLowerCase();
                String prioritetIndiv = ov.getPrioritet().toString().substring(0, 1).toUpperCase() 
                                      + ov.getPrioritet().toString().substring(1).toLowerCase();

                // Dodajemo nivo i prioritet na Zahtev, ne na Oglas 
                triples.append("  :zahtevaNivo :").append(nivoIndiv).append(" ; \n");
                triples.append("  :imaPrioritet :").append(prioritetIndiv).append(" . \n");
            }
        }

        // Slanje SPARQL upita
        String query = MY_PREFIX + RDF_PREFIX + "INSERT DATA { " + triples.toString() + " }";
        izvrsiUpdate(query);

        System.out.println("Oglas '" + o.getNaslov() + "' je sinhronizovan kao N-ary struktura.");
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