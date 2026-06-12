package com.mycompany.vbisapi.service;

import com.mycompany.vbisapi.model.*;
import org.apache.jena.query.*;
import org.apache.jena.update.*;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class FusekiService {

    private static final String FUSEKI_UPDATE_URL = "http://localhost:3030/vbis_dataset/update";
    private static final String FUSEKI_QUERY_URL = "http://localhost:3030/vbis_dataset/query";

    private static final String NS = "http://www.vbis-projekat.rs/model#";
    private static final String RDF_PREFIX = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ";
    private static final String MY_PREFIX = "PREFIX : <" + NS + "> ";
    private static final Pattern RDF_ID_PATTERN = Pattern.compile("[A-Za-z0-9_][A-Za-z0-9_-]*");

    public void sacuvajStudentaURDF(Student s) {
        String studentID = s.getEmail().replace("@", "_").replace(".", "_");
        String studentRef = rdfRef(studentID);
        String query = MY_PREFIX + RDF_PREFIX +
                "INSERT DATA { " + studentRef + " rdf:type :Student ; " +
                ":imaIme " + rdfLiteral(s.getIme()) + " ; " +
                ":imaPrezime " + rdfLiteral(s.getPrezime()) + " ; " +
                ":traziZaposlenje " + s.isTraziZaposlenje() + " . }";
        izvrsiUpdate(query);
        System.out.println("Student " + s.getIme() + " (Traži posao: " + s.isTraziZaposlenje() + ") poslat u Fuseki!");
    }

    public List<RangiraniStudent> getRangListaStudenata(String oglasId, int stranica, int poStranici) {
        List<RangiraniStudent> lista = new ArrayList<>();
        int offset = (stranica - 1) * poStranici;
        String oglasRef = rdfRef(oglasId);

        String sparqlQuery = MY_PREFIX + RDF_PREFIX +
                "SELECT ?studentID ?ime ?prezime (SUM(?score) AS ?ukupniBodovi) WHERE { " +
                "  " + oglasRef + " :imaZahtev ?zahtev . " +
                "  ?zahtev :odnosiSeNaVestinu ?vestina ; :zahtevaNivo ?nivoOglasa ; :imaPrioritet ?prioritet . " +
                "  ?studentID rdf:type :Student ; :imaIme ?ime ; :imaPrezime ?prezime ; :traziZaposlenje true . " +
                "  OPTIONAL { " +
                "    ?polaganje :imaStudenta ?studentID ; :imaPredmet ?predmet ; :imaOcenu ?ocena . " +
                "    ?predmet :prenosiVestinu ?vestina ; :nudiNivo ?nivoStudenta . " +
                "  } " +
                "  BIND(IF(?nivoOglasa = :Pocetni, 1, IF(?nivoOglasa = :Srednji, 2, 3)) AS ?nivoOglasaNum) " +
                "  BIND(IF(BOUND(?nivoStudenta), IF(?nivoStudenta = :Pocetni, 1, IF(?nivoStudenta = :Srednji, 2, 3)), 0) AS ?nivoStudentaNum) "
                +
                "  BIND(?nivoStudentaNum - ?nivoOglasaNum AS ?razlika) " +
                "  BIND(IF(!BOUND(?ocena), 0.0, IF(?razlika >= 1, 1.2, IF(?razlika = 0, 1.0, IF(?razlika = -1, 0.5, 0.2)))) AS ?faktor) "
                +
                "  BIND(IF(?prioritet = :Visok, 3, 1) AS ?prioritetNum) " +
                "  BIND(IF(BOUND(?ocena), (?ocena * 10) * ?faktor * ?prioritetNum, 0) AS ?score) "
                + "} GROUP BY ?studentID ?ime ?prezime " +
                "HAVING (SUM(?score) > 0) " +
                "ORDER BY DESC(?ukupniBodovi) " +
                "LIMIT " + poStranici + " OFFSET " + offset;

        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(FUSEKI_QUERY_URL, sparqlQuery)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                lista.add(new RangiraniStudent(
                        soln.get("studentID").toString().replace(NS, ""),
                        soln.get("ime").toString(),
                        soln.get("prezime").toString(),
                        soln.get("ukupniBodovi").asLiteral().getDouble()));
            }
        } catch (Exception e) {
            throw fusekiGreska("Rangiranje studenata za oglas " + oglasId, e);
        }
        return lista;
    }

    public List<PreporuceniOglas> getPreporukeZaStudenta(String studentEmail, int stranica, int poStranici) {
        String studentID = studentEmail.replace("@", "_").replace(".", "_");
        String studentRef = rdfRef(studentID);
        List<PreporuceniOglas> preporuke = new ArrayList<>();
        int offset = (stranica - 1) * poStranici;

        String sparqlQuery = MY_PREFIX + RDF_PREFIX +
                "SELECT ?oglasID ?naslov (SUM(?score) AS ?ukupniBodovi) WHERE { " +
                "  ?oglasID rdf:type :Oglas ; :imaNaziv ?naslov ; :imaZahtev ?zahtev . " +
                "  ?zahtev :odnosiSeNaVestinu ?vestina ; :zahtevaNivo ?nivoOglasa ; :imaPrioritet ?prioritet . " +
                "  OPTIONAL { " +
                "    ?polaganje :imaStudenta " + studentRef + " ; :imaPredmet ?predmet ; :imaOcenu ?ocena . " +
                "    ?predmet :prenosiVestinu ?vestina ; :nudiNivo ?nivoStudenta . } " +
                "  BIND(IF(?nivoOglasa = :Pocetni, 1, IF(?nivoOglasa = :Srednji, 2, 3)) AS ?nivoOglasaNum) " +
                "  BIND(IF(BOUND(?nivoStudenta), IF(?nivoStudenta = :Pocetni, 1, IF(?nivoStudenta = :Srednji, 2, 3)), 0) AS ?nivoStudentaNum) "
                +
                "  BIND(?nivoStudentaNum - ?nivoOglasaNum AS ?razlika) " +
                "  BIND(IF(!BOUND(?ocena), 0.0, IF(?razlika >= 1, 1.2, IF(?razlika = 0, 1.0, IF(?razlika = -1, 0.5, 0.2)))) AS ?faktor) "
                +
                "  BIND(IF(?prioritet = :Visok, 3, 1) AS ?prioritetNum) " +
                "  BIND(IF(BOUND(?ocena), (?ocena * 10) * ?faktor * ?prioritetNum, 0) AS ?score) " +
                "} GROUP BY ?oglasID ?naslov " +
                "HAVING (SUM(?score) > 0) " +
                "ORDER BY DESC(?ukupniBodovi) " +
                "LIMIT " + poStranici + " OFFSET " + offset;

        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(FUSEKI_QUERY_URL, sparqlQuery)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                preporuke.add(new PreporuceniOglas(
                        soln.get("oglasID").toString().replace(NS, ""),
                        soln.get("naslov").toString(),
                        soln.get("ukupniBodovi").asLiteral().getDouble()));
            }
        } catch (Exception e) {
            throw fusekiGreska("Preporuka oglasa za studenta " + studentEmail, e);
        }
        return preporuke;
    }

    public void sacuvajAgencijuURDF(Agencija a) {
        String query = MY_PREFIX + RDF_PREFIX +
                "INSERT DATA { " + rdfRef(a.getId()) + " rdf:type :AgencijaZaZaposljavanje ; :imaNaziv "
                + rdfLiteral(a.getNazivAgencije()) + " . }";
        izvrsiUpdate(query);
    }

    public void sacuvajPredmetURDF(Predmet p) {
        String nivoIndiv = p.getNivoKojiNudi().toString().substring(0, 1).toUpperCase()
                + p.getNivoKojiNudi().toString().substring(1).toLowerCase();

        String query = MY_PREFIX + RDF_PREFIX +
                "INSERT DATA { " + rdfRef(p.getId()) + " rdf:type :Predmet ; " +
                ":imaNaziv " + rdfLiteral(p.getNazivPredmeta()) + " ; " +
                ":prenosiVestinu " + rdfRef(p.getVestina().getId()) + " ; " +
                ":nudiNivo " + rdfRef(nivoIndiv) + " ; " +
                ":predajeZa " + rdfRef(p.getPredavacId()) + " . }";
        izvrsiUpdate(query);
    }

    public void sacuvajPolaganjeURDF(Polaganje pol) {
        String query = MY_PREFIX + RDF_PREFIX +
                "INSERT DATA { " + rdfRef(pol.getId()) + " rdf:type :Polaganje ; " +
                ":imaStudenta " + rdfRef(pol.getStudentId()) + " ; " +
                ":imaPredmet " + rdfRef(pol.getPredmetId()) + " ; " +
                ":imaOcenu " + pol.getOcena() + " . }";
        izvrsiUpdate(query);
    }

    public void sacuvajPredavacaURDF(Predavac pr) {
        String query = MY_PREFIX + RDF_PREFIX +
                "INSERT DATA { " + rdfRef(pr.getId()) + " rdf:type :Predavac ; " +
                ":imaIme " + rdfLiteral(pr.getIme()) + " ; " +
                ":imaPrezime " + rdfLiteral(pr.getPrezime()) + " ; " +
                ":imaTitulu " + rdfLiteral(pr.getTitula()) + " . }";
        izvrsiUpdate(query);
    }

    public void sacuvajOglasURDF(Oglas o) {
        StringBuilder triples = new StringBuilder();
        String oglasRef = rdfRef(o.getId());
        triples.append(oglasRef).append(" rdf:type :Oglas ; :imaNaziv ").append(rdfLiteral(o.getNaslov()))
                .append(" . \n");
        triples.append(rdfRef(o.getAgencijaId())).append(" :objavljuje ").append(oglasRef).append(" . \n");

        if (o.getZahtevaneVestine() != null) {
            for (OglasVestina ov : o.getZahtevaneVestine()) {
                String zahtevId = "Zahtev_" + o.getId() + "_" + ov.getVestina().getId();
                String zahtevRef = rdfRef(zahtevId);
                triples.append(oglasRef).append(" :imaZahtev ").append(zahtevRef).append(" . \n");
                triples.append(zahtevRef).append(" rdf:type :ZahtevanaVestina ; \n");
                triples.append("  :odnosiSeNaVestinu ").append(rdfRef(ov.getVestina().getId())).append(" ; \n");

                String nivoIndiv = ov.getNivo().toString().substring(0, 1).toUpperCase()
                        + ov.getNivo().toString().substring(1).toLowerCase();
                String prioritetIndiv = ov.getPrioritet().toString().substring(0, 1).toUpperCase()
                        + ov.getPrioritet().toString().substring(1).toLowerCase();

                triples.append("  :zahtevaNivo ").append(rdfRef(nivoIndiv)).append(" ; \n");
                triples.append("  :imaPrioritet ").append(rdfRef(prioritetIndiv)).append(" . \n");
            }
        }
        String query = MY_PREFIX + RDF_PREFIX + "INSERT DATA { " + triples.toString() + " }";
        izvrsiUpdate(query);
    }

    public void sacuvajVestinuURDF(Vestina v) {
        String query = MY_PREFIX + RDF_PREFIX +
                "INSERT DATA { " + rdfRef(v.getId()) + " rdf:type :Vestina ; :imaNaziv "
                + rdfLiteral(v.getNaziv()) + " . }";
        izvrsiUpdate(query);
    }

    // UPDATE//
    public void azurirajStudentaURDF(Student s) {
        String studentID = s.getEmail().replace("@", "_").replace(".", "_");
        String studentRef = rdfRef(studentID);

        String query = MY_PREFIX + RDF_PREFIX +
                "DELETE WHERE { " + studentRef + " :imaIme ?i ; :imaPrezime ?p ; :traziZaposlenje ?t } ; " +
                "INSERT DATA { " + studentRef + " :imaIme " + rdfLiteral(s.getIme()) + " ; " +
                ":imaPrezime " + rdfLiteral(s.getPrezime()) + " ; " +
                ":traziZaposlenje " + s.isTraziZaposlenje() + " . }";

        izvrsiUpdate(query);
        System.out.println("Fuseki: Student " + s.getIme() + " ažuriran.");
    }

    public void azurirajAgencijuURDF(Agencija a) {
        String agencijaRef = rdfRef(a.getId());
        String query = MY_PREFIX + RDF_PREFIX +
                "DELETE WHERE { " + agencijaRef + " :imaNaziv ?n } ; " +
                "INSERT DATA { " + agencijaRef + " :imaNaziv " + rdfLiteral(a.getNazivAgencije()) + " . }";

        izvrsiUpdate(query);
        System.out.println("Fuseki: Agencija " + a.getNazivAgencije() + " ažurirana.");
    }

    private void izvrsiUpdate(String sparql) {
        try {
            UpdateRequest request = UpdateFactory.create(sparql);
            UpdateExecutionFactory.createRemote(request, FUSEKI_UPDATE_URL).execute();
        } catch (Exception e) {
            throw fusekiGreska("SPARQL update", e);
        }
    }

    // --- DELETE METODE (Fuseki RDF) ---
    public void obrisiKorisnikaIzRDF(String id) {
        String ref = rdfRef(id);
        String deleteQuery = MY_PREFIX +
                "DELETE { " + ref + " ?p ?o . ?s ?p2 " + ref + " } " +
                "WHERE { { " + ref + " ?p ?o } UNION { ?s ?p2 " + ref + " } }";

        izvrsiUpdate(deleteQuery);
        System.out.println("Fuseki: Svi podaci za entitet " + id + " su uklonjeni iz grafa.");
    }

    // --- BRISANJE SPECIFIČNIH ENTITETA ---
    public void obrisiPolaganjeIzRDF(String id) {
        String ref = rdfRef(id);
        String query = MY_PREFIX +
                "DELETE { " + ref + " ?p ?o . ?s ?p2 " + ref + " } " +
                "WHERE { " +
                "  { " + ref + " ?p ?o } UNION { ?s ?p2 " + ref + " } " +
                "}";
        izvrsiUpdate(query);
        System.out.println("Fuseki: Obrisano polaganje " + id);
    }

    public void obrisiOglasIzRDF(String id) {
        String ref = rdfRef(id);
        String query = MY_PREFIX +
                "DELETE { " +
                "  " + ref + " ?p ?o . " +
                "  ?s ?p2 " + ref + " . " +
                "  ?zahtev ?zp ?zo . " +
                "} WHERE { " +
                "  { " + ref + " ?p ?o } " +
                "  UNION { ?s ?p2 " + ref + " } " +
                "  UNION { " + ref + " :imaZahtev ?zahtev . ?zahtev ?zp ?zo } " +
                "}";
        izvrsiUpdate(query);
        System.out.println("Fuseki: Obrisan oglas " + id + " i svi njegovi zahtevi.");
    }

    public void ocistiSve() {
        try {
            String obrisiUpit = "CLEAR ALL";
            org.apache.jena.update.UpdateRequest request = org.apache.jena.update.UpdateFactory.create(obrisiUpit);
            org.apache.jena.update.UpdateProcessor processor = org.apache.jena.update.UpdateExecutionFactory
                    .createRemote(request, FUSEKI_UPDATE_URL);
            processor.execute();
        } catch (Exception e) {
            throw fusekiGreska("Čišćenje Fuseki baze", e);
        }
    }

    private RuntimeException fusekiGreska(String operacija, Exception e) {
        return new IllegalStateException(operacija + " nije uspela u Fuseki servisu.", e);
    }

    private String rdfRef(String id) {
        validirajRdfId(id);
        return ":" + id;
    }

    private void validirajRdfId(String id) {
        if (id == null || !RDF_ID_PATTERN.matcher(id).matches()) {
            throw new IllegalArgumentException("Neispravan RDF ID: " + id);
        }
    }

    private String rdfLiteral(String vrednost) {
        if (vrednost == null) {
            return "\"\"";
        }

        String escaped = vrednost
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        return "\"" + escaped + "\"";
    }
}
