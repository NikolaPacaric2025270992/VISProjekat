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
import java.util.List;

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
            throw arangoGreska("Inicijalizacija ArangoDB sistema", e);
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
        
            String generisanId = idIzEmaila(s.getEmail());
            s.setId(generisanId);
            
            doc.setKey(generisanId);
            doc.addAttribute("id", generisanId);
            doc.addAttribute("ime", s.getIme());
            doc.addAttribute("prezime", s.getPrezime());
            doc.addAttribute("email", s.getEmail());
            doc.addAttribute("lozinka", s.getLozinka());
            doc.addAttribute("nivoStudija", s.getNivoStudija());
            doc.addAttribute("traziZaposlenje", s.isTraziZaposlenje());

            arangoDB.db(dbName).collection("studenti").insertDocument(doc);
            System.out.println("Student " + s.getIme() + " uspesno sacuvan u ArangoDB!");
        }catch(Exception e) {
            throw arangoGreska("Čuvanje studenta " + s.getEmail(), e);
        }  
    }
    
    public void sacuvajAgenciju(Agencija a){
        try{
            BaseDocument doc = new BaseDocument();
        
            String generisanId = idIzEmaila(a.getEmail());
            a.setId(generisanId);
            
            doc.setKey(generisanId);
            doc.addAttribute("id", generisanId);
            doc.addAttribute("nazivAgencije", a.getNazivAgencije());
            doc.addAttribute("email", a.getEmail());
            doc.addAttribute("lozinka", a.getLozinka()); 
            doc.addAttribute("pib", a.getPib());
            doc.addAttribute("lokacija", a.getLokacija());

            arangoDB.db(dbName).collection("agencije").insertDocument(doc);
            System.out.println("Agencija " + a.getNazivAgencije() + " uspesno sacuvana u ArangoDB!");
        } catch (Exception e){
            throw arangoGreska("Čuvanje agencije " + a.getEmail(), e);
        }
    }
    
    public void sacuvajPredmet(Predmet p) {
        try {
            BaseDocument doc = new BaseDocument();
            doc.setKey(p.getId());
            doc.addAttribute("id", p.getId());
            doc.addAttribute("nazivPredmeta", p.getNazivPredmeta());
            doc.addAttribute("ects", p.getEcts());
            doc.addAttribute("nivoKojiNudi", p.getNivoKojiNudi().toString());
            doc.addAttribute("vestinaId", p.getVestina().getId());
            doc.addAttribute("predavacId", p.getPredavacId());

            arangoDB.db(dbName).collection("predmeti").insertDocument(doc);
            System.out.println("Predmet " + p.getNazivPredmeta() + " sačuvan u ArangoDB!");
        } catch (Exception e) {
            throw arangoGreska("Čuvanje predmeta " + p.getId(), e);
        }
    }
    
    public void sacuvajPredavaca(Predavac pr) {
        try {
            BaseDocument doc = new BaseDocument();
            doc.setKey(pr.getId());
            doc.addAttribute("id", pr.getId());
            doc.addAttribute("ime", pr.getIme());
            doc.addAttribute("prezime", pr.getPrezime());
            doc.addAttribute("titula", pr.getTitula());

            arangoDB.db(dbName).collection("predavaci").insertDocument(doc);
            System.out.println("Predavač sačuvan u ArangoDB!");
        } catch (Exception e) {
            throw arangoGreska("Čuvanje predavača " + pr.getId(), e);
        }
    }
    
    public void sacuvajPolaganje(Polaganje pol){
        try {
            BaseDocument doc = new BaseDocument();
        
            doc.setKey(pol.getId());
            doc.addAttribute("id", pol.getId());
            doc.addAttribute("studentId", pol.getStudentId());
            doc.addAttribute("predmetId", pol.getPredmetId());
            doc.addAttribute("ocena", pol.getOcena());

            arangoDB.db(dbName).collection("polaganja").insertDocument(doc);
            System.out.println("Polaganje " + pol.getId() + " uspesno sacuvano u ArtangoDB!");
        } catch (Exception e){
            throw arangoGreska("Čuvanje polaganja " + pol.getId(), e);
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

            if (o.getZahtevaneVestine() != null) {
                doc.addAttribute("zahtevaneVestine", o.getZahtevaneVestine());
            }
            arangoDB.db(dbName).collection("oglasi").insertDocument(doc);
            System.out.println("Oglas '" + o.getNaslov() + "' uspješno sačuvan u ArangoDB sa kompleksnim zahtjevima!");
        } catch (Exception e){
            throw arangoGreska("Čuvanje oglasa " + o.getId(), e);
        }
    }
    
    public void sacuvajVestinu(Vestina v) {
        try {
            BaseDocument doc = new BaseDocument();

            doc.setKey(v.getId()); 
            doc.addAttribute("id", v.getId());
            doc.addAttribute("naziv", v.getNaziv());

            arangoDB.db(dbName).collection("vestine").insertDocument(doc);
            System.out.println("Veština " + v.getNaziv() + " uspešno sačuvana u ArangoDB!");
        } catch (Exception e) {
            throw arangoGreska("Čuvanje veštine " + v.getId(), e);
        }
    }

    // --- LOGIN METODE ---
    public Student loginStudent(String email, String lozinka) {
        String query = "FOR s IN studenti FILTER s.email == @email AND s.lozinka == @lozinka RETURN s";
        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("email", email);
        bindVars.put("lozinka", lozinka);

        ArangoCursor<Student> cursor = arangoDB.db(dbName).query(query, Student.class, bindVars, null);
        Student ulogovan = cursor.hasNext() ? cursor.next() : null; 
        
        if (ulogovan != null) {
            ulogovan.setId(idIzEmaila(email));
        }
        
        return ulogovan;
    }

    public Agencija loginAgencija(String email, String lozinka) {
        String query = "FOR a IN agencije FILTER a.email == @email AND a.lozinka == @lozinka RETURN a";
        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("email", email);
        bindVars.put("lozinka", lozinka);

        ArangoCursor<Agencija> cursor = arangoDB.db(dbName).query(query, Agencija.class, bindVars, null);
        Agencija ulogovana = cursor.hasNext() ? cursor.next() : null;
        
        if (ulogovana != null) {
            ulogovana.setId(idIzEmaila(email));
        }
        
        return ulogovana;
    }

    // --- UPDATE METODE ---
    public void azurirajStudenta(Student s) {
        try {
            BaseDocument doc = new BaseDocument();
            doc.addAttribute("ime", s.getIme());
            doc.addAttribute("prezime", s.getPrezime());
            doc.addAttribute("email", s.getEmail());
            doc.addAttribute("nivoStudija", s.getNivoStudija());
            doc.addAttribute("traziZaposlenje", s.isTraziZaposlenje());

            String key = idIzEmaila(s.getEmail());
            arangoDB.db(dbName).collection("studenti").updateDocument(key, doc);
            System.out.println("ArangoDB: Student " + s.getIme() + " ažuriran.");
        } catch (Exception e) {
            throw arangoGreska("Ažuriranje studenta " + s.getEmail(), e);
        }
    }

    public void azurirajAgenciju(Agencija a) {
        try {
            BaseDocument doc = new BaseDocument();
            doc.addAttribute("nazivAgencije", a.getNazivAgencije());
            doc.addAttribute("email", a.getEmail());
            doc.addAttribute("lokacija", a.getLokacija());
            doc.addAttribute("pib", a.getPib());

            arangoDB.db(dbName).collection("agencije").updateDocument(a.getId(), doc);
            System.out.println("ArangoDB: Agencija " + a.getNazivAgencije() + " ažurirana.");
        } catch (Exception e) {
            throw arangoGreska("Ažuriranje agencije " + a.getId(), e);
        }
    }

    public void promeniLozinkuStudenta(String email, String novaLozinka) {
        try {
            BaseDocument doc = new BaseDocument();
            doc.addAttribute("lozinka", novaLozinka);
            arangoDB.db(dbName).collection("studenti").updateDocument(idIzEmaila(email), doc);
            System.out.println("ArangoDB: Lozinka studenta " + email + " je ažurirana.");
        } catch (Exception e) {
            throw arangoGreska("Promena lozinke studenta " + email, e);
        }
    }

    public void promeniLozinkuAgencije(String email, String novaLozinka) {
        try {
            BaseDocument doc = new BaseDocument();
            doc.addAttribute("lozinka", novaLozinka);
            arangoDB.db(dbName).collection("agencije").updateDocument(idIzEmaila(email), doc);
            System.out.println("ArangoDB: Lozinka agencije " + email + " je ažurirana.");
        } catch (Exception e) {
            throw arangoGreska("Promena lozinke agencije " + email, e);
        }
    }
    
    // --- DELETE METODE (ArangoDB) ---
    public void obrisiStudenta(String id) {
        try {
            arangoDB.db(dbName).collection("studenti").deleteDocument(id);
            System.out.println("ArangoDB: Student " + id + " je obrisan.");
        } catch (Exception e) {
            throw arangoGreska("Brisanje studenta " + id, e);
        }
    }

    public void obrisiAgenciju(String id) {
        try {
            arangoDB.db(dbName).collection("agencije").deleteDocument(id);
            System.out.println("ArangoDB: Agencija " + id + " je obrisana.");
        } catch (Exception e) {
            throw arangoGreska("Brisanje agencije " + id, e);
        }
    }
    
    public void obrisiPolaganje(String id) {
        try {
            arangoDB.db(dbName).collection("polaganja").deleteDocument(id);
        } catch (Exception e) { throw arangoGreska("Brisanje polaganja " + id, e); }
    }

    public void obrisiOglas(String id) {
        try {
            arangoDB.db(dbName).collection("oglasi").deleteDocument(id);
        } catch (Exception e) { throw arangoGreska("Brisanje oglasa " + id, e); }
    }

    public void obrisiPredmet(String id) {
        try {
            arangoDB.db(dbName).collection("predmeti").deleteDocument(id);
        } catch (Exception e) { throw arangoGreska("Brisanje predmeta " + id, e); }
    }

    public void obrisiPredavaca(String id) {
        try {
            arangoDB.db(dbName).collection("predavaci").deleteDocument(id);
        } catch (Exception e) { throw arangoGreska("Brisanje predavaca " + id, e); }
    }

    public void obrisiVestinu(String id) {
        try {
            arangoDB.db(dbName).collection("vestine").deleteDocument(id);
        } catch (Exception e) { throw arangoGreska("Brisanje vestine " + id, e); }
    }
    
    public java.util.List<Oglas> nadjiOglasePoAgenciji(String agencijaId) {
        String query = "FOR o IN oglasi FILTER o.agencijaId == @agencijaId RETURN MERGE(o, { id: HAS(o, 'id') ? o.id : o._key })";
        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("agencijaId", agencijaId);

        try {
            com.arangodb.ArangoCursor<Oglas> cursor = arangoDB.db(dbName).query(query, Oglas.class, bindVars, null);
            return cursor.asListRemaining();
        } catch (Exception e) {
            throw arangoGreska("Preuzimanje oglasa agencije " + agencijaId, e);
        }
    }
    
    public List<Vestina> sveVestine() {
        String query = "FOR v IN vestine RETURN v";
        try {
            ArangoCursor<Vestina> cursor = arangoDB.db(dbName).query(query, Vestina.class, null, null);
            return cursor.asListRemaining();
        } catch (Exception e) {
            throw arangoGreska("Dohvatanje veština", e);
        }
    }
    
    public List<Predmet> sviPredmeti() {
        String query = "FOR p IN predmeti " +
                       "  LET vId = HAS(p, 'vestinaId') ? p.vestinaId : p.vestina.id " +
                       "  LET v = FIRST(FOR vest IN vestine FILTER vest.id == vId OR vest._key == vId RETURN vest) " +
                       "  RETURN MERGE(p, { " +
                       "    id: HAS(p, 'id') ? p.id : p._key, " +
                       "    nazivPredmeta: HAS(p, 'nazivPredmeta') ? p.nazivPredmeta : p.naziv, " +
                       "    nivoKojiNudi: HAS(p, 'nivoKojiNudi') ? p.nivoKojiNudi : p.nivo, " +
                       "    vestina: v != null ? v : p.vestina " +
                       "  })";
        try {
            com.arangodb.ArangoCursor<Predmet> cursor = arangoDB.db(dbName).query(query, Predmet.class, null, null);
            List<Predmet> rezultati = cursor.asListRemaining();
            System.out.println("Arango: Uspesno povuceno " + rezultati.size() + " predmeta sa mapiranim nivoima.");
            return rezultati;
        } catch (Exception e) {
            throw arangoGreska("Dohvatanje i mapiranje predmeta", e);
        }
    }

    public List<Predavac> sviPredavaci() {
        String query = "FOR pr IN predavaci RETURN pr";
        try {
            ArangoCursor<Predavac> cursor = arangoDB.db(dbName).query(query, Predavac.class, null, null);
            return cursor.asListRemaining();
        } catch (Exception e) {
            throw arangoGreska("Dohvatanje predavača", e);
        }
    }
    
    public List<Student> sviStudenti() {
        String query = "FOR s IN studenti RETURN s";
        try {
            ArangoCursor<Student> cursor = arangoDB.db(dbName).query(query, Student.class, null, null);
            return cursor.asListRemaining();
        } catch (Exception e) {
            throw arangoGreska("Dohvatanje studenata", e);
        }
    }
    
    public List<Student> nadjiAktivneStudente() {
        String query = "FOR s IN studenti FILTER s.traziZaposlenje == true RETURN MERGE(s, { id: HAS(s, 'id') ? s.id : s._key })";
        try {
            com.arangodb.ArangoCursor<Student> cursor = arangoDB.db(dbName).query(query, Student.class, null, null);
            return cursor.asListRemaining();
        } catch (Exception e) {
            throw arangoGreska("Dohvatanje aktivnih studenata", e);
        }
    }

    public List<Agencija> sveAgencije() {
        String query = "FOR a IN agencije RETURN a";
        try {
            ArangoCursor<Agencija> cursor = arangoDB.db(dbName).query(query, Agencija.class, null, null);
            return cursor.asListRemaining();
        } catch (Exception e) {
            throw arangoGreska("Dohvatanje agencija", e);
        }
    }

    public List<Polaganje> svaPolaganja() {
        String query = "FOR p IN polaganja RETURN p";
        try {
            ArangoCursor<Polaganje> cursor = arangoDB.db(dbName).query(query, Polaganje.class, null, null);
            return cursor.asListRemaining();
        } catch (Exception e) {
            throw arangoGreska("Dohvatanje polaganja", e);
        }
    }

    public List<Oglas> sviOglasi() {
        String query = "FOR o IN oglasi RETURN MERGE(o, { id: HAS(o, 'id') ? o.id : o._key })";
        try {
            com.arangodb.ArangoCursor<Oglas> cursor = arangoDB.db(dbName).query(query, Oglas.class, null, null);
            return cursor.asListRemaining();
        } catch (Exception e) {
            throw arangoGreska("Dohvatanje oglasa", e);
        }
    }   
    
    public List<Polaganje> nadjiPolaganjaStudenta(String studentId) {
        String query = "FOR p IN polaganja FILTER p.studentId == @studentId RETURN p";
        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("studentId", studentId);

        try {
            com.arangodb.ArangoCursor<Polaganje> cursor = arangoDB.db(dbName).query(query, Polaganje.class, bindVars, null);
            return cursor.asListRemaining();
        } catch (Exception e) {
            throw arangoGreska("Preuzimanje polaganja studenta " + studentId, e);
        }
    }

    private RuntimeException arangoGreska(String operacija, Exception e) {
        return new IllegalStateException(operacija + " nije uspela u ArangoDB.", e);
    }

    private String idIzEmaila(String email) {
        return email.replace("@", "_").replace(".", "_");
    }
}
