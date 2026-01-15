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

public class Jena {
    private OntModel model;
    private Dataset dataset;
    private final String NS = "http://www.vbis.org/ontology#";
    private final String TDB_PATH = "tdb_data"; // Folder za bazu znanja

    public Jena(String path) {
        // 1. Inicijalizacija TDB baze
        this.dataset = TDBFactory.createDataset(TDB_PATH);

        // 2. Učitavanje šeme (OWL.owl) kao običan model
        Model schema = ModelFactory.createDefaultModel();
        schema.read(path);

        // 3. Kreiranje OWL Reasoner-a i povezivanje sa šemom
        // Ovo osigurava da Reasoner zna tvoja pravila (npr. šta je Student)
        org.apache.jena.reasoner.Reasoner reasoner = org.apache.jena.reasoner.ReasonerRegistry.getOWLReasoner();
        reasoner = reasoner.bindSchema(schema);

        // 4. Kreiranje Inference modela koji spaja Reasoner i TDB podatke
        InfModel infModel = ModelFactory.createInfModel(reasoner, dataset.getDefaultModel());

        // 5. Na kraju sve to stavljamo u OntModel radi lakšeg rada sa Individualima
        this.model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, infModel);
    }

    // Dodavanje Studenta (Upisuje se u TDB, ne u fajl)
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
                dataset.abort(); // OBAVEZNO poništavamo transakciju u slučaju greške
            }
            throw e; // Prosleđujemo grešku dalje da bi je kontroler video
        } finally {
            dataset.end();
        }
    }
    
    public void addAgency(String username, String realName) throws Exception {
        dataset.begin(ReadWrite.WRITE);
        try {
            OntClass agencyClass = model.getOntClass(NS + "Agencija");
            Individual individual = model.createIndividual(NS + username, agencyClass);
            // Možeš dodati i naziv agencije kao property ako želiš
            validateTransaction();
            dataset.commit();
        } catch (Exception e) {
            if (dataset.isInTransaction()) {
                dataset.abort(); // OBAVEZNO poništavamo transakciju u slučaju greške
            }
            throw e; // Prosleđujemo grešku dalje da bi je kontroler video
        } finally {
            dataset.end();
        }
    }

    // DODAJEMO: Metoda za Agenciju (Specifikacija zahteva)
    public void addOglas(String oglasId, String naslov, String agencijaUsername, List<String> vestineURIs) throws Exception {
        dataset.begin(ReadWrite.WRITE);
        try {
            OntClass oglasClass = model.getOntClass(NS + "Oglas");
            Individual oglas = model.createIndividual(NS + oglasId, oglasClass);
            oglas.addProperty(model.getDatatypeProperty(NS + "naslovOglasa"), naslov);
            
            // Povezivanje sa agencijom
            Resource agencija = model.getResource(NS + agencijaUsername);
            oglas.addProperty(model.getObjectProperty(NS + "postavljenOd"), agencija);

            // Dodavanje potrebnih veština
            for (String vUri : vestineURIs) {
                Resource vestina = model.getResource(vUri);
                oglas.addProperty(model.getObjectProperty(NS + "zahtevaVestinu"), vestina);
            }
            validateTransaction();
            dataset.commit();
        } catch (Exception e) {
            if (dataset.isInTransaction()) {
                dataset.abort(); // OBAVEZNO poništavamo transakciju u slučaju greške
            }
            throw e; // Prosleđujemo grešku dalje da bi je kontroler video
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

                // Prvo uklanjamo staru vrednost da ne bismo imali duple triplete
                student.removeAll(traziPosaoProp);

                // Dodajemo novu vrednost
                student.addLiteral(traziPosaoProp, traziPosao);

                // Validacija pre upisa
                validateTransaction();
                dataset.commit();
            }
        } catch (Exception e) {
            if (dataset.isInTransaction()) {
                dataset.abort(); // OBAVEZNO poništavamo transakciju u slučaju greške
            }
            throw e; // Prosleđujemo grešku dalje da bi je kontroler video
        } finally {
            dataset.end();
        }
    }

    // Preporuka oglasa za studenta
    public List<String> getRecommendedAds(String studentUsername) {
        List<String> oglasi = new ArrayList<>();

        // 1. Započinjemo READ transakciju (neophodno za TDB!)
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

            // 2. Koristimo try-with-resources da automatski zatvorimo upit
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
            // 3. OBAVEZNO završavamo transakciju
            dataset.end();
        }

        return oglasi;
    }
    
    // 1. Metoda za dodavanje položenog ispita (TDB)
    public void addExam(String username, String predmetId, int ocena) throws Exception {
        dataset.begin(ReadWrite.WRITE);
        try {
            String examId = "ispit_" + username + "_" + predmetId;
            OntClass ispitClass = model.getOntClass(NS + "Ispit");
            Individual ispit = model.createIndividual(NS + examId, ispitClass);

            // Postavljanje ocene
            ispit.addLiteral(model.getDatatypeProperty(NS + "ocena"), ocena);

            // Povezivanje sa predmetom
            Resource predmet = model.getResource(NS + predmetId);
            ispit.addProperty(model.getObjectProperty(NS + "ispitZaPredmet"), predmet);

            // Povezivanje studenta sa ispitom
            Resource student = model.getResource(NS + username);
            student.addProperty(model.getObjectProperty(NS + "polozioIspit"), ispit);
            validateTransaction();

            dataset.commit();
        } catch (Exception e) {
            if (dataset.isInTransaction()) {
                dataset.abort(); // OBAVEZNO poništavamo transakciju u slučaju greške
            }
            throw e; // Prosleđujemo grešku dalje da bi je kontroler video
        } finally {
            dataset.end();
        }
    }

    // 2. RANG LISTA: Pronalaženje idealnih kandidata za oglas (SPARQL)
    public List<Map<String, String>> getRankedStudentsForOglas(String oglasId) {
        List<Map<String, String>> kandidati = new ArrayList<>();

        // OBAVEZNO dodajemo transakciju i ovde
        dataset.begin(ReadWrite.READ);
        try {
            String queryString = 
                "PREFIX : <" + NS + "> " +
                "SELECT DISTINCT ?studentName ?username " +
                "WHERE { " +
                "  <" + NS + oglasId + "> :zahtevaVestinu ?v . " +
                "  ?student :imaVestinu ?v . " + 
                "  ?student :imePrezime ?studentName . " +
                "  ?student :traziPosao true . " + 
                "  BIND(STRAFTER(STR(?student), '#') AS ?username) " +
                "}";

            // Koristimo try-with-resources da zatvorimo QueryExecution
            try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
                ResultSet results = qexec.execSelect();
                while (results.hasNext()) {
                    QuerySolution soln = results.nextSolution();
                    Map<String, String> k = new HashMap<>();
                    k.put("name", soln.getLiteral("studentName").getString());
                    k.put("username", soln.get("username").toString());
                    kandidati.add(k);
                }
            }
        } finally {
            dataset.end();
        }
        return kandidati;
    }
    
    private void validateTransaction() throws Exception {
        if (model == null) return;

        // Uzimamo ValidityReport iz modela
        ValidityReport report = model.validate();

        // Ako model ne podržava validaciju (što se sad ne bi smelo desiti), preskačemo
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
    
    // Za studente: Spisak svih oglasa sa naslovima
    public List<Map<String, String>> getAllAds() {
        List<Map<String, String>> oglasi = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            String queryString = "PREFIX : <" + NS + "> SELECT ?id ?naslov WHERE { ?id rdf:type :Oglas . ?id :naslovOglasa ?naslov . }";
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

    // Za agencije: Spisak svih studenata koji su označili da traže posao
    public List<Map<String, String>> getStudentsLookingForWork() {
        List<Map<String, String>> studenti = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            String queryString = 
                "PREFIX : <" + NS + "> " +
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

    public OntModel getModel() { return model; }
}