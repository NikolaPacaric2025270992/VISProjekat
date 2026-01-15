package com.mycompany.vbisapi.springbootapi;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import java.io.FileOutputStream;
import java.io.OutputStream;
import org.apache.jena.reasoner.ValidityReport;
import java.util.Iterator;
import org.apache.jena.query.*;
import java.util.ArrayList;
import java.util.List;
import java.io.InputStream;
import org.apache.jena.reasoner.ValidityReport;

public class Jena {
    private OntModel model;
    private String sourcePath;
    private final String NS = "http://www.vbis.org/ontology#";

    public Jena(String path) {
        this.sourcePath = path;
        this.model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        // Učitavanje fajla
        if (FileManager.get().open(path) != null) {
            model.read(path);
        }
    }

    public void addStudent(String username, String imePrezime) {
        OntClass studentClass = model.getOntClass(NS + "Student");
        Individual individual = model.createIndividual(NS + username, studentClass);
        
        // Dodavanje imena i prezimena
        DatatypeProperty imeProperty = model.getDatatypeProperty(NS + "imePrezime");
        individual.addProperty(imeProperty, imePrezime);
        
        // Inicijalno postavljanje da traži posao
        DatatypeProperty traziPosao = model.getDatatypeProperty(NS + "traziPosao");
        individual.addLiteral(traziPosao, true);
        
        saveModel();
    }

    public void saveModel() {
        try (OutputStream out = new FileOutputStream(sourcePath)) {
            model.write(out, "RDF/XML-ABBREV");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Ovde ćeš kasnije dodati SPARQL upite za preporuke
    public OntModel getModel() { return model; }
    
    public void checkConsistency() {
    // Reasoner proverava pravila iz OWL fajla nad podacima
        ValidityReport report = model.validate();

        if (report.isValid()) {
            System.out.println("Sve je u redu: Ontologija je konzistentna.");
        } else {
            System.out.println("Greška: Pronađene su logičke kontradikcije!");
            // Ispisujemo tačno šta nije u redu
            for (Iterator<ValidityReport.Report> i = report.getReports(); i.hasNext(); ) {
                System.out.println(" - " + i.next().getDescription());
            }
        }
    }
    
    public List<String> getRecommendedAds(String studentUsername) {
        List<String> preporuceniOglasi = new ArrayList<>();
        String studentUri = NS + studentUsername;

        // SPARQL Upit: 
        // 1. Nađi sve ispite koje je student položio
        // 2. Nađi predmete za te ispite i veštine koje ti predmeti daju
        // 3. Nađi oglase koji zahtevaju te iste veštine
        String queryString = 
            "PREFIX : <" + NS + "> " +
            "SELECT DISTINCT ?naslovOglasa " +
            "WHERE { " +
            "  <" + studentUri + "> :polozioIspit ?ispit . " +
            "  ?ispit :ispitZaPredmet ?predmet . " +
            "  ?predmet :dajeVestinu ?vestina . " +
            "  ?oglas :zahtevaVestinu ?vestina . " +
            "  ?oglas :naslovOglasa ?naslovOglasa . " +
            "}";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                preporuceniOglasi.add(soln.getLiteral("naslovOglasa").getString());
            }
        }
        return preporuceniOglasi;
    }
    
    public boolean importData(InputStream in) {
        try {
            // Kreiramo privremeni model da proverimo nove podatke
            OntModel tempModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
            tempModel.read(in, null, "RDF/XML");

            // VALIDACIJA: Provera konzistentnosti novih podataka
            ValidityReport report = tempModel.validate();
            if (!report.isValid()) {
                System.out.println("Nevalidni podaci u fajlu!");
                return false;
            }

            // Ako je sve u redu, spajamo sa glavnim modelom
            this.model.add(tempModel);
            saveModel(); // Snimamo promene u OWL.owl fajl
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}