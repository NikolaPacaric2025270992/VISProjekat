/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.jena;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import java.io.FileOutputStream;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

/**
 *
 * @author Nikola
 */
public class Jena {

    public static void main(String[] args) {
        String NS = "http://www.vbis.org/ontology#";
        
        ArangoDB arangoDB = new ArangoDB.Builder()
                .host("127.0.0.1", 8529)
                .user("root")
                .password("")
                .build();
        
        ArangoDatabase db = arangoDB.db("projekat");
        if (!db.exists()) {
            arangoDB.createDatabase("projekat");
            db = arangoDB.db("projekat");
        }
        if (!db.collection("users").exists()) db.createCollection("users");
        
        Model model = ModelFactory.createDefaultModel().read("src/main/resources/OWL.owl");
        
        Resource Student = model.createResource(NS + "Student");
        Resource Vestina = model.createResource(NS + "Vestina");
        Resource Predmet = model.createResource(NS + "Predmet");
        Resource Ispit = model.createResource(NS + "Ispit");
        Resource Oglas = model.createResource(NS + "Oglas");
        
        Property imaVestinu = model.createProperty(NS, "imaVestinu");
        Property zahtevaVestinu = model.createProperty(NS, "zahtevaVestinu");
        Property polozioIspit = model.createProperty(NS, "polozioIspit");
        Property ispitZaPredmet = model.createProperty(NS, "ispitZaPredmet");
        Property dajeVestinu = model.createProperty(NS, "dajeVestinu");
        Property nivo = model.createProperty(NS, "nivoVestine");
        Property prioritet = model.createProperty(NS, "prioritet");
        Property zaVestinu = model.createProperty(NS, "zaVestinu");
        
        Resource Java = model.createResource(NS+"Java").addProperty(RDF.type, Vestina);
        Resource React = model.createResource(NS+"React").addProperty(RDF.type, Vestina);
        Resource SQL = model.createResource(NS+"SQL").addProperty(RDF.type, Vestina);
        Resource Python = model.createResource(NS+"Python").addProperty(RDF.type, Vestina);
        Resource Docker = model.createResource(NS+"Docker").addProperty(RDF.type, Vestina);
        
        Resource nikola = model.createResource(NS+"Nikola").addProperty(RDF.type, Student);
        Resource ana = model.createResource(NS+"Ana").addProperty(RDF.type, Student);
        Resource marko = model.createResource(NS+"Marko").addProperty(RDF.type, Student);
        Resource jelena = model.createResource(NS+"Jelena").addProperty(RDF.type, Student);
        Resource ivan = model.createResource(NS+"Ivan").addProperty(RDF.type, Student);

        // Nikola
        Resource nikJava = model.createResource().addProperty(zaVestinu, Java).addLiteral(nivo,4);
        Resource nikReact = model.createResource().addProperty(zaVestinu, React).addLiteral(nivo,3);
        nikola.addProperty(imaVestinu, nikJava).addProperty(imaVestinu, nikReact);

        // Ana
        Resource anaJava = model.createResource().addProperty(zaVestinu, Java).addLiteral(nivo,5);
        Resource anaSQL = model.createResource().addProperty(zaVestinu, SQL).addLiteral(nivo,4);
        ana.addProperty(imaVestinu, anaJava).addProperty(imaVestinu, anaSQL);

        // Marko
        Resource markoPython = model.createResource().addProperty(zaVestinu, Python).addLiteral(nivo,5);
        Resource markoDocker = model.createResource().addProperty(zaVestinu, Docker).addLiteral(nivo,3);
        marko.addProperty(imaVestinu, markoPython).addProperty(imaVestinu, markoDocker);

        // Jelena
        Resource jelJava = model.createResource().addProperty(zaVestinu, Java).addLiteral(nivo,3);
        Resource jelReact = model.createResource().addProperty(zaVestinu, React).addLiteral(nivo,5);
        jelena.addProperty(imaVestinu, jelJava).addProperty(imaVestinu, jelReact);

        // Ivan
        Resource ivanJava = model.createResource().addProperty(zaVestinu, Java).addLiteral(nivo,2);
        Resource ivanSQL = model.createResource().addProperty(zaVestinu, SQL).addLiteral(nivo,5);
        ivan.addProperty(imaVestinu, ivanJava).addProperty(imaVestinu, ivanSQL);
        
        Resource juniorJava = model.createResource(NS+"JuniorJava").addProperty(RDF.type, Oglas);
        Resource frontend = model.createResource(NS+"FrontendDev").addProperty(RDF.type, Oglas);
        Resource backend = model.createResource(NS+"BackendDev").addProperty(RDF.type, Oglas);
        Resource pythonDev = model.createResource(NS+"PythonDev").addProperty(RDF.type, Oglas);

        // Junior Java
        juniorJava.addProperty(zahtevaVestinu,
            model.createResource().addProperty(zaVestinu, Java).addLiteral(prioritet,5));
        juniorJava.addProperty(zahtevaVestinu,
            model.createResource().addProperty(zaVestinu, SQL).addLiteral(prioritet,3));

        // Frontend
        frontend.addProperty(zahtevaVestinu,
            model.createResource().addProperty(zaVestinu, React).addLiteral(prioritet,5));
        frontend.addProperty(zahtevaVestinu,
            model.createResource().addProperty(zaVestinu, Java).addLiteral(prioritet,3));

        // Backend
        backend.addProperty(zahtevaVestinu,
            model.createResource().addProperty(zaVestinu, Java).addLiteral(prioritet,5));
        backend.addProperty(zahtevaVestinu,
            model.createResource().addProperty(zaVestinu, SQL).addLiteral(prioritet,4));
        backend.addProperty(zahtevaVestinu,
            model.createResource().addProperty(zaVestinu, Docker).addLiteral(prioritet,3));

        // Python Dev
        pythonDev.addProperty(zahtevaVestinu,
            model.createResource().addProperty(zaVestinu, Python).addLiteral(prioritet,5));
        pythonDev.addProperty(zahtevaVestinu,
            model.createResource().addProperty(zaVestinu, Docker).addLiteral(prioritet,2));
        
        
        String s = """
            PREFIX vbis: <http://www.vbis.org/ontology#>

            SELECT ?student ?job (SUM(?n * ?p) AS ?score)
            WHERE {
              ?student a vbis:Student .
              ?student vbis:imaVestinu ?sv .
              ?sv vbis:zaVestinu ?skill .
              ?sv vbis:nivoVestine ?n .

              ?job a vbis:Oglas .
              ?job vbis:zahtevaVestinu ?ov .
              ?ov vbis:zaVestinu ?skill .
              ?ov vbis:prioritet ?p .
            }
            GROUP BY ?student ?job
            ORDER BY DESC(?score)
            """;
        
        Query q = QueryFactory.create(s);
        try (QueryExecution exe = QueryExecutionFactory.create(q, model)){
            ResultSet rs = exe.execSelect();
            ResultSetFormatter.out(System.out, rs);
        }
        
        try {
            FileOutputStream rdfExport = new FileOutputStream("export.rdf");
            model.write(rdfExport, "RDF/XML");
            rdfExport.close();
            System.out.println("RDF exportovan u export.rdf");
        } catch (Exception e){
            e.printStackTrace();
        }
        
        try {
            FileOutputStream jsonExport = new FileOutputStream("export.json");
            model.write(jsonExport, "RDF/JSON");
            jsonExport.close();
            System.out.print("JSON exportovan u export.json");
        } catch (Exception e){
            e.printStackTrace();
        }
        
        System.out.println("\n--- Citanje RDF nazad ---");
        
        Model imported = ModelFactory.createDefaultModel();
        imported.read("export.rdf");
        ResIterator studenti = imported.listResourcesWithProperty(
                RDF.type,
                imported.getResource("http://www.vbis.org/ontology#Student"));
        
        while (studenti.hasNext()){
            Resource stRDF = studenti.next();
            System.out.println("Student: "+ stRDF.getLocalName());
        }
        
        System.out.println("\n--- Citanje JSON nazad ---");
        
        Model jsonModel = ModelFactory.createDefaultModel();
        jsonModel.read("file:export.json", null, "RDF/JSON");
        ResIterator studentiJSON = jsonModel.listResourcesWithProperty(
                RDF.type,
                jsonModel.getResource("http://www.vbis.org/ontology#Student"));
        
        while (studentiJSON.hasNext()){
            System.out.println("JSON student: "+ 
                    studentiJSON.next().getLocalName());
        }
        
        BaseDocument studentDoc = new BaseDocument();
        studentDoc.setKey("Nikola");
        studentDoc.addAttribute("role", "student");
        studentDoc.addAttribute("name", "Nikola Pacaric");
        studentDoc.addAttribute("skills", new String[]{"Java", "React"});
        studentDoc.addAttribute("studyProgram", "Informatika");
        studentDoc.addAttribute("lookingForJob", true);
        db.collection("users").insertDocument(studentDoc);
        
        BaseDocument agencyDoc = new BaseDocument();
        agencyDoc.setKey("Agencija1");
        agencyDoc.addAttribute("role", "agency");
        agencyDoc.addAttribute("name", "Agencija1");
        db.collection("users").insertDocument(agencyDoc);
        
        System.out.println("Studenti i agencije ubaceni u ArangoDB.");
    }
}
