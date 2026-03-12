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
import com.mycompany.vbisapi.model.Predavac;
import com.mycompany.vbisapi.model.Predmet;
import com.mycompany.vbisapi.model.Student;
import com.mycompany.vbisapi.model.Vestina;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import com.arangodb.ArangoCursor;
import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author nikol
 */
@Service 
public class ArangoService {
    private final ArangoDB arangoDB = new ArangoDB.Builder()
            .host("127.0.0.1", 8529)
            .user("root")
            .password("")
            .build();
    
    private final String dbName = "projekat";
    
    @PostConstruct
    public void autoInit(){
        System.out.println("Spring Boot pokrece auto-inicijalizaciju ArangoDB...");
        try{
            inicijalizujSistem();
        } catch (Exception e){
            System.err.println("Greška pri inicijalizaciji Arango servera: " + e.getMessage());
        }
        
    }
    
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
        try{
            BaseDocument doc = new BaseDocument();
        
            doc.setKey(s.getEmail().replace("@", "_").replace(".", "_"));
            doc.addAttribute("ime", s.getIme());
            doc.addAttribute("prezime", s.getPrezime());
            doc.addAttribute("email", s.getEmail());
            doc.addAttribute("lozinka", s.getLozinka());
            doc.addAttribute("nivoStudija", s.getNivoStudija());
            doc.addAttribute("traziZaposlenje", s.isTraziZaposlenje());

            arangoDB.db(dbName).collection("studenti").insertDocument(doc);
            System.out.println("Student " + s.getIme() + " uspesno sacuvan u ArangoDB!");
        }catch(Exception e) {
            System.err.println("Greška pri čuvanju studenta u Arango: " + e.getMessage());
        }  
    }
    
    public void sacuvajAgenciju(Agencija a){
        try{
            BaseDocument doc = new BaseDocument();
        
            doc.setKey(a.getId());
            doc.addAttribute("naziv", a.getNazivAgencije());
            doc.addAttribute("email", a.getEmail());
            doc.addAttribute("lozinka", a.getLozinka()); // DODATO
            doc.addAttribute("pib", a.getPib());

            arangoDB.db(dbName).collection("agencije").insertDocument(doc);
            System.out.println("Agencija " + a.getNazivAgencije() + " uspesno sacuvana u ArangoDB!");
        } catch (Exception e){
            System.err.println("Greška pri čuvanju agencije u Arango: " + e.getMessage());
        }
    }
    
    public void sacuvajPredmet(Predmet p) {
        try {
            BaseDocument doc = new BaseDocument();
            doc.setKey(p.getId());
            doc.addAttribute("naziv", p.getNazivPredmeta());
            doc.addAttribute("ects", p.getEcts());
            doc.addAttribute("nivo", p.getNivoKojiNudi().toString());
            doc.addAttribute("vestinaId", p.getVestina().getId());
            doc.addAttribute("predavacId", p.getPredavacId());

            arangoDB.db(dbName).collection("predmeti").insertDocument(doc);
            System.out.println("Predmet " + p.getNazivPredmeta() + " sačuvan u ArangoDB!");
        } catch (Exception e) {
            System.err.println("Greška Arango Predmet: " + e.getMessage());
        }
    }
    
    public void sacuvajPredavaca(Predavac pr) {
        try {
            BaseDocument doc = new BaseDocument();
            doc.setKey(pr.getId());
            doc.addAttribute("ime", pr.getIme());
            doc.addAttribute("prezime", pr.getPrezime());
            doc.addAttribute("titula", pr.getTitula());

            arangoDB.db(dbName).collection("predavaci").insertDocument(doc);
            System.out.println("Predavač sačuvan u ArangoDB!");
        } catch (Exception e) {
            System.err.println("Greška Arango Predavač: " + e.getMessage());
        }
    }
    
    public void sacuvajPolaganje(Polaganje pol){
        try {
            BaseDocument doc = new BaseDocument();
        
            doc.setKey(pol.getId());
            doc.addAttribute("studentId", pol.getStudentId());
            doc.addAttribute("predmetId", pol.getPredmetId());
            doc.addAttribute("ocena", pol.getOcena());

            arangoDB.db(dbName).collection("polaganja").insertDocument(doc);
            System.out.println("Polaganje " + pol.getId() + " uspesno sacuvano u ArtangoDB!");
        } catch (Exception e){
            System.err.println("Greška pri čuvanju polaganja u Arango: " + e.getMessage());
        }  
    }
    
    public void sacuvajOglas(Oglas o){
        try {
            BaseDocument doc = new BaseDocument();

            doc.setKey(o.getId());
            doc.addAttribute("naslov", o.getNaslov());
            doc.addAttribute("opis", o.getOpis());
            doc.addAttribute("plata", o.getPlata());
            doc.addAttribute("agencijaId", o.getAgencijaId());

            // VIŠE NE STAVLJAMO nivo i prioritet na vrh dokumenta jer su oni 
            // sada specifični za svaku veštinu unutar liste zahteva.

            if (o.getZahtevaneVestine() != null) {
                // ArangoDB drajver će automatski List<OglasVestina> pretvoriti u 
                // JSON niz objekata, gde svaki sadrži veštinu, nivo i prioritet.
                doc.addAttribute("zahtevi", o.getZahtevaneVestine());
            }

            arangoDB.db(dbName).collection("oglasi").insertDocument(doc);
            System.out.println("Oglas '" + o.getNaslov() + "' uspješno sačuvan u ArangoDB sa kompleksnim zahtjevima!");
        } catch (Exception e){
            System.err.println("Greška pri čuvanju oglasa u Arango: " + e.getMessage());
        }
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

    // --- LOGIN METODE ---
    public Student loginStudent(String email, String lozinka) {
        String query = "FOR s IN studenti FILTER s.email == @email AND s.lozinka == @lozinka RETURN s";
        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("email", email);
        bindVars.put("lozinka", lozinka);

        ArangoCursor<Student> cursor = arangoDB.db(dbName).query(query, Student.class, bindVars, null);
        return cursor.hasNext() ? cursor.next() : null; // Vraća studenta ako postoji, inače null
    }

    public Agencija loginAgencija(String email, String lozinka) {
        String query = "FOR a IN agencije FILTER a.email == @email AND a.lozinka == @lozinka RETURN a";
        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("email", email);
        bindVars.put("lozinka", lozinka);

        ArangoCursor<Agencija> cursor = arangoDB.db(dbName).query(query, Agencija.class, bindVars, null);
        return cursor.hasNext() ? cursor.next() : null;
    }

    // --- UPDATE METODE ---
    public void azurirajStudenta(Student s) {
        try {
            BaseDocument doc = new BaseDocument();
            doc.addAttribute("ime", s.getIme());
            doc.addAttribute("prezime", s.getPrezime());
            doc.addAttribute("email", s.getEmail());
            doc.addAttribute("lozinka", s.getLozinka());
            doc.addAttribute("nivoStudija", s.getNivoStudija());
            doc.addAttribute("traziZaposlenje", s.isTraziZaposlenje());

            String key = s.getEmail().replace("@", "_").replace(".", "_");
            arangoDB.db(dbName).collection("studenti").updateDocument(key, doc);
            System.out.println("ArangoDB: Student " + s.getIme() + " ažuriran.");
        } catch (Exception e) {
            System.err.println("Greška pri ažuriranju studenta: " + e.getMessage());
        }
    }

    public void azurirajAgenciju(Agencija a) {
        try {
            BaseDocument doc = new BaseDocument();
            doc.addAttribute("naziv", a.getNazivAgencije());
            doc.addAttribute("email", a.getEmail());
            doc.addAttribute("lozinka", a.getLozinka());
            doc.addAttribute("lokacija", a.getLokacija());
            doc.addAttribute("pib", a.getPib());

            arangoDB.db(dbName).collection("agencije").updateDocument(a.getId(), doc);
            System.out.println("ArangoDB: Agencija " + a.getNazivAgencije() + " ažurirana.");
        } catch (Exception e) {
            System.err.println("Greška pri ažuriranju agencije: " + e.getMessage());
        }
    }
}
