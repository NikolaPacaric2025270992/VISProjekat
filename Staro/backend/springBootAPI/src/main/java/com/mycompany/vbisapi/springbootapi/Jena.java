package com.mycompany.vbisapi.springbootapi;

import org.apache.jena.ontology.*;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.reasoner.ValidityReport;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;

public class Jena {
    private OntModel model;
    private Dataset dataset;
    private final String NS = "http://www.vbis.org/ontology#";
    private final String TDB_PATH = "tdb_data"; // Folder za bazu znanja

    public Jena(String path) {
        try {
            // 1. Inicijalizacija TDB baze
            this.dataset = TDBFactory.createDataset(TDB_PATH);

            // 2. Učitavanje šeme
            Model schema = ModelFactory.createDefaultModel();
            schema.read(path);

            // 3. Eksplicitno kreiranje Reasoner-a
            Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
            reasoner = reasoner.bindSchema(schema);

            // 4. Povezivanje sa TDB podacima
            InfModel infModel = ModelFactory.createInfModel(reasoner, dataset.getDefaultModel());

            // Zaključivanje (INF)
            this.model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, infModel);

            System.out.println("Jena sistem uspešno inicijalizovan sa podrškom za validaciju.");
        } catch (Exception e) {
            System.err.println("GREŠKA: Jena nije mogla da se pokrene!");
            e.printStackTrace();
        }
    }
    
    public void debugPrintAllTriples() {
        dataset.begin(ReadWrite.READ);
        try {
            System.out.println("\n--- DEBUG: SVE TRIPLETE U BAZI ---");
            StmtIterator iter = model.listStatements();
            int count = 0;
            while (iter.hasNext()) {
                Statement stmt = iter.nextStatement();
                Resource  subject   = stmt.getSubject();
                Property  predicate = stmt.getPredicate();
                RDFNode   object    = stmt.getObject();

                // Filtriram na namespace da ne bi gledao hiljade sistemskih OWL tripleta
                if (subject.toString().contains(NS) || predicate.toString().contains(NS)) {
                    System.out.println(subject.getLocalName() + " -> " + predicate.getLocalName() + " -> " + object.toString());
                    count++;
                }
            }
            System.out.println("Ukupno tvojih tripleta: " + count);
            System.out.println("----------------------------------\n");
        } finally {
            dataset.end();
        }
    }

    // Dodavanje Studenta (Upisuje se u TDB)
    public void addStudent(String username, String realName) throws Exception {
        dataset.begin(ReadWrite.WRITE);
        try {
            OntClass studentClass = model.getOntClass(NS + "Student");
            Individual individual = model.createIndividual(NS + username, studentClass);
            individual.addProperty(model.getDatatypeProperty(NS + "imePrezime"), realName);
            individual.addLiteral(model.getDatatypeProperty(NS + "traziPosao"), true);
            validateTransaction();
            dataset.commit();
        } catch (Exception e) {
            if (dataset.isInTransaction()) {
                dataset.abort(); // Poništavanje transakciju u slučaju greške
            }
            throw e; // Prosleđivanje greške dalje da bi je kontroler video
        } finally {
            dataset.end();
        }
    }
    
    public void addAgency(String username, String realName) throws Exception {
        dataset.begin(ReadWrite.WRITE);
        try {
            OntClass agencyClass = model.getOntClass(NS + "Agencija");
            Individual individual = model.createIndividual(NS + username, agencyClass);
            validateTransaction();
            dataset.commit();
        } catch (Exception e) {
            if (dataset.isInTransaction()) {
                dataset.abort(); // Poništavanje transakciju u slučaju greške
            }
            throw e; // Prosleđivanje greške dalje da bi je kontroler video
        } finally {
            dataset.end();
        }
    }

    // Metoda za Agenciju (Specifikacija zahteva)
    public void addOglas(String oglasId, String naslov, String agencijaUsername, 
                         List<Map<String, Object>> vestineSaDetaljima) throws Exception {
        dataset.begin(ReadWrite.WRITE);
        try {
            Individual oglas = model.createIndividual(NS + oglasId, model.getOntClass(NS + "Oglas"));
            oglas.addProperty(model.getDatatypeProperty(NS + "naslovOglasa"), naslov);
            oglas.addProperty(model.getObjectProperty(NS + "postavljenOd"), model.getResource(NS + agencijaUsername));

            for (Map<String, Object> detalji : vestineSaDetaljima) {
                String vUri = (String) detalji.get("uri");
                int nivo = (int) detalji.get("nivo");
                int prioritet = (int) detalji.get("prioritet");
                Resource vestina = model.getResource(vUri);

                // Veza Oglas -> Vestina
                oglas.addProperty(model.getObjectProperty(NS + "zahtevaVestinu"), vestina);

                // Kreiram 'Zahtev' (Individual tipa Vestina koji čuva nivo i prioritet)
                String zahtevId = oglasId + "_" + vestina.getLocalName() + "_req";
                Individual zahtev = model.createIndividual(NS + zahtevId, model.getOntClass(NS + "Vestina"));
                zahtev.addLiteral(model.getDatatypeProperty(NS + "nivoVestine"), nivo);
                zahtev.addLiteral(model.getDatatypeProperty(NS + "prioritet"), prioritet);

                // Povezujem ovaj zahtev sa konkretnom veštinom
                zahtev.addProperty(model.getObjectProperty(NS + "imaVestinu"), vestina);
            }
            dataset.commit();
        } catch (Exception e) {
            if (dataset.isInTransaction()) dataset.abort();
            throw e;
        } finally {
            dataset.end();
        }
    }
    
    // Ažuriranje statusa traženja posla u TDB bazi
    public void updateStudentStatus(String username, boolean traziPosao) throws Exception {
        dataset.begin(ReadWrite.WRITE);
        try {
            Individual student = model.getIndividual(NS + username);
            if (student != null) {
                DatatypeProperty traziPosaoProp = model.getDatatypeProperty(NS + "traziPosao");

                // Prvo uklanjam staru vrednost
                student.removeAll(traziPosaoProp);

                // Dodajem novu vrednost
                student.addLiteral(traziPosaoProp, traziPosao);

                // Validacija pre upisa
                validateTransaction();
                dataset.commit();
            }
        } catch (Exception e) {
            if (dataset.isInTransaction()) {
                dataset.abort();
            }
            throw e; 
        } finally {
            dataset.end();
        }
    }

    // Preporuka oglasa za studenta
    public List<String> getRecommendedAds(String studentUsername) {
        List<String> oglasi = new ArrayList<>();

        // READ za TDB
        dataset.begin(ReadWrite.READ);
        try {
            String queryString = 
                "PREFIX : <" + NS + "> " +
                "SELECT DISTINCT ?naslov " +
                "WHERE { " +
                "  <" + NS + studentUsername + "> :polozioIspit ?ispit . " +
                "  ?ispit :ispitZaPredmet ?predmet . " +
                "  ?predmet :dajeVestinu ?v . " +
                "  ?oglas :zahtevaVestinu ?v . " +
                "  ?oglas :naslovOglasa ?naslov . " +
                "}";

            // Try-with-resources da automatski zatvorim upit
            try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
                ResultSet results = qexec.execSelect();
                while (results.hasNext()) {
                    QuerySolution soln = results.nextSolution();
                    if (soln.contains("naslov")) {
                        oglasi.add(soln.getLiteral("naslov").getString());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Greška u SPARQL preporukama: " + e.getMessage());
        } finally {
            // Završavam transakciju
            dataset.end();
        }

        return oglasi;
    }
    
    // AddExam - Kreiram Ispit, Predmet i povezuje sa Veštinom
    public void addExam(String username, String predmetId, int ocena, int nivoKojiPredmetDaje) throws Exception {
        dataset.begin(ReadWrite.WRITE);
        try {
            // Kreiram Ispit
            String examId = "ispit_" + username + "_" + predmetId;
            Individual ispit = model.createIndividual(NS + examId, model.getOntClass(NS + "Ispit"));
            ispit.addLiteral(model.getDatatypeProperty(NS + "ocena"), ocena);

            // Kreiram Predmet
            Individual predmet = model.createIndividual(NS + predmetId, model.getOntClass(NS + "Predmet"));
            predmet.removeAll(model.getDatatypeProperty(NS + "nivoVestine"));
            predmet.addLiteral(model.getDatatypeProperty(NS + "nivoVestine"), nivoKojiPredmetDaje);

            // Ispit -> Predmet
            ispit.addProperty(model.getObjectProperty(NS + "ispitZaPredmet"), predmet);

            // Predmet -> Vestina
            String vestinaId = predmetId.replaceAll("\\d", ""); 
            Resource vestina = model.getResource(NS + vestinaId);
            predmet.addProperty(model.getObjectProperty(NS + "dajeVestinu"), vestina);

            // Student -> Ispit
            Resource student = model.getResource(NS + username);
            student.addProperty(model.getObjectProperty(NS + "polozioIspit"), ispit);

            validateTransaction();
            dataset.commit();
        } catch (Exception e) {
            if (dataset.isInTransaction()) dataset.abort();
            throw e;
        } finally {
            dataset.end();
        }
    }

    
    public List<Map<String, String>> getRankedStudentsForOglas(String oglasId) {
        List<Map<String, String>> kandidati = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            String queryString = 
                "PREFIX : <" + NS + "> " +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                "SELECT DISTINCT ?username ?studentName ?score " +
                "WHERE { " +
                "  <" + NS + oglasId + "> :zahtevaVestinu ?v . " +
                "  ?zahtev :imaVestinu ?v . " +
                "  FILTER(CONTAINS(STR(?zahtev), \"" + oglasId + "\")) " +
                "  ?zahtev :prioritet ?prioritet . " +
                "  ?student :polozioIspit ?ispit . " +
                "  ?student :traziPosao true . " +
                "  ?ispit :ispitZaPredmet ?predmet . " +
                "  ?predmet :dajeVestinu ?v . " +
                "  ?predmet :nivoVestine ?nivoStudenta . " +
                "  ?ispit :ocena ?ocena . " +
                "  ?student :imePrezime ?studentName . " +
                "  BIND(STRAFTER(STR(?student), '#') AS ?username) " +
                "  BIND((xsd:integer(?ocena) * xsd:integer(?nivoStudenta) * xsd:integer(?prioritet)) AS ?score) " +
                "} " +
                "ORDER BY DESC(?score)";

            try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
                ResultSet results = qexec.execSelect();
                while (results.hasNext()) {
                    QuerySolution soln = results.nextSolution();
                    Map<String, String> k = new HashMap<>();
                    k.put("username", soln.get("username").toString());
                    k.put("name", soln.getLiteral("studentName").getString());
                    k.put("score", soln.get("score").asLiteral().getString());
                    kandidati.add(k);
                }
            }
        } catch (Exception e) {
            System.err.println("Greška u rangiranju: " + e.getMessage());
        } finally {
            dataset.end();
        }
        return kandidati;
    }
    
    public String getOntologyExport() {
        dataset.begin(ReadWrite.READ);
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            model.write(baos, "RDF/XML-ABBREV");
            return baos.toString();
        } finally {
            dataset.end();
        }
    }
    
    private void validateTransaction() throws Exception {
        if (model == null) return;

        // ValidityReport iz modela
        ValidityReport report = model.validate();

        // Ako model ne podržava validaciju, preskačem
        if (report == null) {
            System.out.println("Upozorenje: Model ne podržava validaciju.");
            return;
        }

        if (!report.isValid()) {
            Iterator<ValidityReport.Report> reports = report.getReports();
            StringBuilder sb = new StringBuilder();
            sb.append("Ontološka validacija nije uspela:\n");
            while (reports.hasNext()) {
                sb.append(" - ").append(reports.next().getDescription()).append("\n");
            }
            throw new Exception(sb.toString());
        }
    }

    public boolean importData(InputStream in) {
        dataset.begin(ReadWrite.WRITE);
        try {
            model.read(in, null, "RDF/XML");
            ValidityReport report = model.validate();
            if (!report.isValid()) return false;
            dataset.commit();
            return true;
        } finally {
            dataset.end();
        }
    }
    
    // Spisak svih oglasa sa naslovima
    public List<Map<String, String>> getAllAds() {
        List<Map<String, String>> oglasi = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            String queryString = 
                "PREFIX : <" + NS + "> " +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + 
                "SELECT ?id ?naslov " +
                "WHERE { " +
                "  ?id rdf:type :Oglas . " + 
                "  ?id :naslovOglasa ?naslov . " +
                "}";

            try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
                ResultSet results = qexec.execSelect();
                while (results.hasNext()) {
                    QuerySolution soln = results.nextSolution();
                    Map<String, String> o = new HashMap<>();
                    o.put("id", soln.getResource("id").getLocalName());
                    o.put("naslov", soln.getLiteral("naslov").getString());
                    oglasi.add(o);
                }
            }
        } finally {
            dataset.end();
        }
        return oglasi;
    }

    // Spisak svih studenata koji su označili da traže posao
    public List<Map<String, String>> getStudentsLookingForWork() {
        List<Map<String, String>> studenti = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            String queryString = 
                "PREFIX : <" + NS + "> " +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "SELECT ?username ?name " +
                "WHERE { " +
                "  ?s rdf:type :Student . " +
                "  ?s :traziPosao true . " +
                "  ?s :imePrezime ?name . " +
                "  BIND(STRAFTER(STR(?s), '#') AS ?username) " +
                "}";

            try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
                ResultSet results = qexec.execSelect();
                while (results.hasNext()) {
                    QuerySolution soln = results.nextSolution();
                    Map<String, String> s = new HashMap<>();
                    s.put("username", soln.get("username").toString());
                    s.put("name", soln.getLiteral("name").getString());
                    studenti.add(s);
                }
            }
        } finally {
            dataset.end();
        }
        return studenti;
    }
    
    public void updateStudentProfile(String username, String newRealName) throws Exception {
        dataset.begin(ReadWrite.WRITE);
        try {
            Individual student = model.getIndividual(NS + username);
            if (student != null) {
                DatatypeProperty imeProp = model.getDatatypeProperty(NS + "imePrezime");
                // Uklanjamo staro ime i postavljamo novo
                student.removeAll(imeProp);
                student.addProperty(imeProp, newRealName);

                dataset.commit();
            }
        } catch (Exception e) {
            if (dataset.isInTransaction()) dataset.abort();
            throw e;
        } finally {
            dataset.end();
        }
    }

    public OntModel getModel() { return model; }
}