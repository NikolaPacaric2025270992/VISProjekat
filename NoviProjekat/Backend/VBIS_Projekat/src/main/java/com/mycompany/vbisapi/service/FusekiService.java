package com.mycompany.vbisapi.service;

import com.mycompany.vbisapi.model.*;
import org.apache.jena.query.*;
import org.apache.jena.update.*;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class FusekiService {
    
    // Razdvajamo URL za upis i URL za čitanje
    private static final String FUSEKI_UPDATE_URL = "http://localhost:3030/vbis_dataset/update";
    private static final String FUSEKI_QUERY_URL = "http://localhost:3030/vbis_dataset/query";
    
    private static final String NS = "http://www.vbis-projekat.rs/model#";
    private static final String RDF_PREFIX = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ";
    private static final String MY_PREFIX = "PREFIX : <" + NS + "> ";

    // 1. AŽURIRANO: Sada uključuje traziZaposlenje flag
    public void sacuvajStudentaURDF(Student s){
        String studentID = s.getEmail().replace("@", "_").replace(".", "_");
        String query = MY_PREFIX + RDF_PREFIX + 
                       "INSERT DATA { :" + studentID + " rdf:type :Student ; " +
                       ":imaIme \"" + s.getIme() + "\" ; " +
                       ":imaPrezime \"" + s.getPrezime() + "\" ; " +
                       ":traziZaposlenje " + s.isTraziZaposlenje() + " . }"; // Dodato
        izvrsiUpdate(query);
        System.out.println("Student " + s.getIme() + " (Traži posao: " + s.isTraziZaposlenje() + ") poslat u Fuseki!");
    }

    public List<RangiraniStudent> getRangListaStudenata(String oglasId, int stranica, int poStranici) {
        List<RangiraniStudent> lista = new ArrayList<>();
        int offset = (stranica - 1) * poStranici;

        String sparqlQuery = MY_PREFIX + RDF_PREFIX +
            "SELECT ?studentID ?ime ?prezime (SUM(?score) AS ?ukupniBodovi) WHERE { " +
            "  :" + oglasId + " :imaZahtev ?zahtev . " +
            "  ?zahtev :odnosiSeNaVestinu ?vestina ; :zahtevaNivo ?nivoOglasa ; :imaPrioritet ?prioritet . " +
            "  ?polaganje :imaStudenta ?studentID ; :imaPredmet ?predmet ; :imaOcenu ?ocena . " +
            "  ?predmet :prenosiVestinu ?vestina ; :nudiNivo ?nivoStudenta . " +
            "  ?studentID :imaIme ?ime ; :imaPrezime ?prezime ; :traziZaposlenje true . " +
            
            // 1. Pretvaramo Nivo studenta u bodove (40, 60, 80)
            "  BIND(IF(?nivoStudenta = :Napredni, 80, IF(?nivoStudenta = :Srednji, 60, 40)) AS ?nivoBodovi) " +
            
            // 2. Bodovi su ISKLJUČIVO (Ocena * 2) + Nivo studenta
            "  BIND((?ocena * 2) + ?nivoBodovi AS ?score) " +
            
            "} GROUP BY ?studentID ?ime ?prezime ORDER BY DESC(?ukupniBodovi) " +
            "LIMIT " + poStranici + " OFFSET " + offset;

        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(FUSEKI_QUERY_URL, sparqlQuery)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                lista.add(new RangiraniStudent(
                    soln.get("studentID").toString().replace(NS, ""),
                    soln.get("ime").toString(),
                    soln.get("prezime").toString(),
                    soln.get("ukupniBodovi").asLiteral().getDouble()
                ));
            }
        }
        return lista;
    }

    public List<PreporuceniOglas> getPreporukeZaStudenta(String studentEmail, int stranica, int poStranici) {
        String studentID = studentEmail.replace("@", "_").replace(".", "_");
        List<PreporuceniOglas> preporuke = new ArrayList<>();

        int offset = (stranica - 1) * poStranici;

        String sparqlQuery = MY_PREFIX + RDF_PREFIX +
            "SELECT ?oglasID ?naslov (SUM(?score) AS ?ukupniBodovi) (MAX(?priNum) AS ?maxPrioritet) WHERE { " +
            "  ?oglasID rdf:type :Oglas ; :imaNaziv ?naslov ; :imaZahtev ?zahtev . " +
            "  ?zahtev :odnosiSeNaVestinu ?vestina ; :zahtevaNivo ?nivoOglasa ; :imaPrioritet ?prioritet . " +
            "  ?polaganje :imaStudenta :" + studentID + " ; :imaPredmet ?predmet ; :imaOcenu ?ocena . " +
            "  ?predmet :prenosiVestinu ?vestina ; :nudiNivo ?nivoStudenta . " +
            
            // 1. Pretvaramo Nivo studenta u bodove (40, 60, 80)
            "  BIND(IF(?nivoStudenta = :Napredni, 80, IF(?nivoStudenta = :Srednji, 60, 40)) AS ?nivoBodovi) " +
            
            // 2. Bodovi su ISKLJUČIVO (Ocena * 2) + Nivo studenta
            "  BIND((?ocena * 2) + ?nivoBodovi AS ?score) " +
            
            // 3. Pretvaramo Prioritet oglasa u broj isključivo radi sortiranja (Visok=2, Srednji=1, Nizak=0)
            "  BIND(IF(?prioritet = :Visok, 2, IF(?prioritet = :Srednji, 1, 0)) AS ?priNum) " +
            
            // Grupišemo i sortiramo: PRVO po prioritetu, ONDA po bodovima
            "} GROUP BY ?oglasID ?naslov ORDER BY DESC(?maxPrioritet) DESC(?ukupniBodovi) " +
            "LIMIT " + poStranici + " OFFSET " + offset;

        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(FUSEKI_QUERY_URL, sparqlQuery)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                preporuke.add(new PreporuceniOglas(
                    soln.get("oglasID").toString().replace(NS, ""),
                    soln.get("naslov").toString(),
                    soln.get("ukupniBodovi").asLiteral().getDouble()
                ));
            }
        } catch (Exception e) {
            System.err.println("Greska pri preporuci: " + e.getMessage());
        }
        return preporuke;
    }

    // --- OSTALE METODE OSTAJU ISTE (Agencija, Predmet, Polaganje, Oglas) ---

    public void sacuvajAgencijuURDF(Agencija a) {
        String query = MY_PREFIX + RDF_PREFIX + 
                       "INSERT DATA { :" + a.getId() + " rdf:type :AgencijaZaZaposljavanje ; :imaNaziv \"" + a.getNazivAgencije() + "\" . }";
        izvrsiUpdate(query);
    }

    public void sacuvajPredmetURDF(Predmet p) {
        String nivoIndiv = p.getNivoKojiNudi().toString().substring(0, 1).toUpperCase() 
                         + p.getNivoKojiNudi().toString().substring(1).toLowerCase();

        String query = MY_PREFIX + RDF_PREFIX + 
                       "INSERT DATA { :" + p.getId() + " rdf:type :Predmet ; " +
                       ":imaNaziv \"" + p.getNazivPredmeta() + "\" ; " +
                       ":prenosiVestinu :" + p.getVestina().getId() + " ; " +
                       ":nudiNivo :" + nivoIndiv + " ; " +
                       ":predajeZa :" + p.getPredavacId() + " . }"; // Ispravljena veza
        izvrsiUpdate(query);
    }

    public void sacuvajPolaganjeURDF(Polaganje pol) {
        String query = MY_PREFIX + RDF_PREFIX + 
                       "INSERT DATA { :" + pol.getId() + " rdf:type :Polaganje ; " +
                       ":imaStudenta :" + pol.getStudentId() + " ; " +
                       ":imaPredmet :" + pol.getPredmetId() + " ; " +
                       ":imaOcenu " + pol.getOcena() + " . }";
        izvrsiUpdate(query);
    }

    public void sacuvajPredavacaURDF(Predavac pr) {
        String query = MY_PREFIX + RDF_PREFIX + 
                       "INSERT DATA { :" + pr.getId() + " rdf:type :Predavac ; " +
                       ":imaIme \"" + pr.getIme() + "\" ; " +
                       ":imaPrezime \"" + pr.getPrezime() + "\" ; " +
                       ":imaTitulu \"" + pr.getTitula() + "\" . }";
        izvrsiUpdate(query);
    }

    public void sacuvajOglasURDF(Oglas o) {
        StringBuilder triples = new StringBuilder();
        triples.append(":").append(o.getId()).append(" rdf:type :Oglas ; :imaNaziv \"").append(o.getNaslov()).append("\" . \n");
        triples.append(":").append(o.getAgencijaId()).append(" :objavljuje :").append(o.getId()).append(" . \n");

        if (o.getZahtevaneVestine() != null) {
            for (OglasVestina ov : o.getZahtevaneVestine()) {
                String zahtevId = "Zahtev_" + o.getId() + "_" + ov.getVestina().getId();
                triples.append(":").append(o.getId()).append(" :imaZahtev :").append(zahtevId).append(" . \n");
                triples.append(":").append(zahtevId).append(" rdf:type :ZahtevanaVestina ; \n");
                triples.append("  :odnosiSeNaVestinu :").append(ov.getVestina().getId()).append(" ; \n");
                
                String nivoIndiv = ov.getNivo().toString().substring(0, 1).toUpperCase() + ov.getNivo().toString().substring(1).toLowerCase();
                String prioritetIndiv = ov.getPrioritet().toString().substring(0, 1).toUpperCase() + ov.getPrioritet().toString().substring(1).toLowerCase();
                
                triples.append("  :zahtevaNivo :").append(nivoIndiv).append(" ; \n");
                triples.append("  :imaPrioritet :").append(prioritetIndiv).append(" . \n");
            }
        }
        String query = MY_PREFIX + RDF_PREFIX + "INSERT DATA { " + triples.toString() + " }";
        izvrsiUpdate(query);
    }

    public void sacuvajVestinuURDF(Vestina v) {
        String query = MY_PREFIX + RDF_PREFIX + 
                       "INSERT DATA { :" + v.getId() + " rdf:type :Vestina ; :imaNaziv \"" + v.getNaziv() + "\" . }";
        izvrsiUpdate(query);
    }
    
    //UPDATE//
    public void azurirajStudentaURDF(Student s) {
        String studentID = s.getEmail().replace("@", "_").replace(".", "_");

        // Brišemo staro ime, prezime i status, a zatim ubacujemo nove vrednosti
        String query = MY_PREFIX + RDF_PREFIX + 
            "DELETE WHERE { :" + studentID + " :imaIme ?i ; :imaPrezime ?p ; :traziZaposlenje ?t } ; " +
            "INSERT DATA { :" + studentID + " :imaIme \"" + s.getIme() + "\" ; " +
            ":imaPrezime \"" + s.getPrezime() + "\" ; " +
            ":traziZaposlenje " + s.isTraziZaposlenje() + " . }";

        izvrsiUpdate(query);
        System.out.println("Fuseki: Student " + s.getIme() + " ažuriran.");
    }

    public void azurirajAgencijuURDF(Agencija a) {
        String query = MY_PREFIX + RDF_PREFIX + 
            "DELETE WHERE { :" + a.getId() + " :imaNaziv ?n } ; " +
            "INSERT DATA { :" + a.getId() + " :imaNaziv \"" + a.getNazivAgencije() + "\" . }";

        izvrsiUpdate(query);
        System.out.println("Fuseki: Agencija " + a.getNazivAgencije() + " ažurirana.");
    }

    private void izvrsiUpdate(String sparql) {
        try {
            UpdateRequest request = UpdateFactory.create(sparql);
            UpdateExecutionFactory.createRemote(request, FUSEKI_UPDATE_URL).execute();
        } catch (Exception e) {
            System.err.println("Greska u Fuseki komunikaciji: " + e.getMessage());
        }
    }
    
    // --- DELETE METODE (Fuseki RDF) ---
    public void obrisiKorisnikaIzRDF(String id) {
        // Ovaj SPARQL upit briše sve triplete gde je dati ID subjekat
        String deleteQuery = MY_PREFIX +
                "DELETE { :" + id + " ?p ?o } " +
                "WHERE { :" + id + " ?p ?o }";
                
        izvrsiUpdate(deleteQuery);
        System.out.println("Fuseki: Svi podaci za entitet " + id + " su uklonjeni iz grafa.");
    }
    
    // --- BRISANJE SPECIFIČNIH ENTITETA ---
    
    public void obrisiPolaganjeIzRDF(String id) {
        // Briše polaganje i sve eventualne veze koje pokazuju na njega
        String query = MY_PREFIX +
            "DELETE { :" + id + " ?p ?o . ?s ?p2 :" + id + " } " +
            "WHERE { " +
            "  { :" + id + " ?p ?o } UNION { ?s ?p2 :" + id + " } " +
            "}";
        izvrsiUpdate(query);
        System.out.println("Fuseki: Obrisano polaganje " + id);
    }

    public void obrisiOglasIzRDF(String id) {
        // KASKADNO BRISANJE: Briše Oglas, briše vezu sa Agencijom i briše sve njegove Zahteve!
        String query = MY_PREFIX +
            "DELETE { " +
            "  :" + id + " ?p ?o . " +
            "  ?s ?p2 :" + id + " . " +
            "  ?zahtev ?zp ?zo . " +
            "} WHERE { " +
            "  { :" + id + " ?p ?o } " +
            "  UNION { ?s ?p2 :" + id + " } " +
            "  UNION { :" + id + " :imaZahtev ?zahtev . ?zahtev ?zp ?zo } " +
            "}";
        izvrsiUpdate(query);
        System.out.println("Fuseki: Obrisan oglas " + id + " i svi njegovi zahtevi.");
    }
    
    public void ocistiSve(){
        try {
            String obrisiUpit = "CLEAR ALL";
            org.apache.jena.update.UpdateRequest request = org.apache.jena.update.UpdateFactory.create(obrisiUpit);
            org.apache.jena.update.UpdateProcessor processor = org.apache.jena.update.UpdateExecutionFactory.createRemote(request, FUSEKI_UPDATE_URL);
            processor.execute();
        } catch (Exception e){
            System.out.println("Greska pri ciscenju Fuseki baze: " + e.getMessage());
        }
    }
}