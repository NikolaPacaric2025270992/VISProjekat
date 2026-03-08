/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.service;

import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import com.mycompany.vbisapi.model.Oglas;
import com.mycompany.vbisapi.model.Student;

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
        
        String[] kolekcije = {"studenti", "agencije", "oglasi", "predmeti", "predavaci"};
        
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
        doc.addAttribute("nivoStudija", s.getNivoStudija());
        
        arangoDB.db(dbName).collection("studenti").insertDocument(doc);
        System.out.println("Student " + s.getIme() + " uspesno sacuvan u ArangoDB!");
    }
    
    public void sacuvajOglas(Oglas o){
        BaseDocument doc = new BaseDocument();
        doc.setKey(o.getId());
        doc.addAttribute("naslov", o.getNaslov());
        doc.addAttribute("plata", o.getPlata());
        doc.addAttribute("nivo", o.getZahtevaniNivo().toString());
        doc.addAttribute("prioritet", o.getPrioritet().toString());
        
        arangoDB.db(dbName).collection("oglas").insertDocument(doc);
        System.out.println("Oglas '" + o.getNaslov() + "' sacuvan u ArangoDB!");
    }
}
