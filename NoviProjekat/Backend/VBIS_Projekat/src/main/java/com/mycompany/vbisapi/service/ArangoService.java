/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.service;

import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import com.mycompany.vbisapi.model.Agencija;
import com.mycompany.vbisapi.model.Oglas;
import com.mycompany.vbisapi.model.Polaganje;
import com.mycompany.vbisapi.model.Predmet;
import com.mycompany.vbisapi.model.Student;
import com.mycompany.vbisapi.model.Vestina;

/**
 *
 * @author nikol
 */
public class ArangoService {
    private final ArangoDB arangoDB = new ArangoDB.Builder()
            .host("127.0.0.1", 8529)
            .user("root")
            .password("")
            .build();
    
    private final String dbName = "projekat";
    
    public void inicijalizujSistem(){
        if (!arangoDB.getDatabases().contains(dbName)){
            arangoDB.createDatabase(dbName);
            System.out.println("Baza " + dbName + " je kreirana.");
        }
        
        String[] kolekcije = {"studenti", "agencije", "oglasi", "predmeti", "predavaci", "polaganja", "vestine"};
        
        java.util.Collection<String> postojeceKol = arangoDB.db(dbName).getCollections()
                .stream()
                .map(c -> c.getName())
                .collect(java.util.stream.Collectors.toList());
        
        for (String kol : kolekcije){
            if (!postojeceKol.contains(kol)){
                arangoDB.db(dbName).createCollection(kol);
                System.out.println("Kolekcija " + kol + " je kreirana.");
            }
        }
    }
    
    public void sacuvajStudenta(Student s){
        BaseDocument doc = new BaseDocument();
        
        doc.setKey(s.getEmail().replace("@", "_").replace(".", "_"));
        doc.addAttribute("ime", s.getIme());
        doc.addAttribute("prezime", s.getPrezime());
        doc.addAttribute("email", s.getEmail());
        doc.addAttribute("lozinka", s.getLozinka());
        doc.addAttribute("nivoStudija", s.getNivoStudija());
        
        arangoDB.db(dbName).collection("studenti").insertDocument(doc);
        System.out.println("Student " + s.getIme() + " uspesno sacuvan u ArangoDB!");
    }
    
    public void sacuvajAgenciju(Agencija a){
        BaseDocument doc = new BaseDocument();
        
        doc.setKey(a.getId());
        doc.addAttribute("naziv", a.getNazivAgencije());
        doc.addAttribute("email", a.getEmail());
        doc.addAttribute("lozinka", a.getLozinka()); // DODATO
        doc.addAttribute("pib", a.getPib());
        
        arangoDB.db(dbName).collection("agencije").insertDocument(doc);
        System.out.println("Agencija " + a.getNazivAgencije() + " uspesno sacuvana u ArangoDB!");
    }
    
    public void sacuvajPredmet(Predmet p){
        BaseDocument doc = new BaseDocument();
        
        doc.setKey(p.getId());
        doc.addAttribute("naziv", p.getNazivPredmeta());
        doc.addAttribute("ects", p.getEcts());
        
        arangoDB.db(dbName).collection("predmeti").insertDocument(doc);
        System.out.println("Predmet " + p.getNazivPredmeta() + " uspesno sacuvan u ArangoDB!");
    }
    
    public void sacuvajPolaganje(Polaganje pol){
        BaseDocument doc = new BaseDocument();
        
        doc.setKey(pol.getId());
        doc.addAttribute("studentId", pol.getStudentId());
        doc.addAttribute("predmetId", pol.getPredmetId());
        doc.addAttribute("ocena", pol.getOcena());
        
        arangoDB.db(dbName).collection("polaganja").insertDocument(doc);
        System.out.println("Polaganje " + pol.getId() + " uspesno sacuvano u ArtangoDB!");
    }
    
    public void sacuvajOglas(Oglas o){
        BaseDocument doc = new BaseDocument();
        
        doc.setKey(o.getId());
        doc.addAttribute("naslov", o.getNaslov());
        doc.addAttribute("plata", o.getPlata());
        doc.addAttribute("nivo", o.getZahtevaniNivo().toString());
        doc.addAttribute("prioritet", o.getPrioritet().toString());
        
        arangoDB.db(dbName).collection("oglasi").insertDocument(doc);
        System.out.println("Oglas '" + o.getNaslov() + "' sacuvan u ArangoDB!");
    }
    
    public void sacuvajVestinu(Vestina v) {
        try {
            BaseDocument doc = new BaseDocument();

            doc.setKey(v.getId()); 
            doc.addAttribute("naziv", v.getNaziv());

            arangoDB.db(dbName).collection("vestine").insertDocument(doc);
            System.out.println("Veština " + v.getNaziv() + " uspešno sačuvana u ArangoDB!");
        } catch (Exception e) {
            System.err.println("Greška pri čuvanju veštine u Arango: " + e.getMessage());
        }
    }
}
