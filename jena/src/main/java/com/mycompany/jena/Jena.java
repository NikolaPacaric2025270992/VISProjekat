/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.jena;

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
        
        Resource java = model.createResource(NS + "Java").addProperty(RDF.type, Vestina);
        Resource react = model.createResource(NS + "React").addProperty(RDF.type, Vestina);
        Resource web = model.createResource(NS + "WebProgramiranje")
                .addProperty(RDF.type, Predmet)
                .addProperty(dajeVestinu, java)
                .addProperty(dajeVestinu, react);
        Resource ispit1 = model.createResource(NS + "IspitWeb")
                .addProperty(RDF.type, Ispit)
                .addProperty(ispitZaPredmet, web);
        Resource nikola = model.createResource(NS + "Nikola")
                .addProperty(RDF.type, Student)
                .addProperty(polozioIspit, ispit1);
        Resource oglas = model.createResource(NS + "JuniorDeveloper")
                .addProperty(RDF.type, Oglas)
                .addProperty(zahtevaVestinu, java)
                .addProperty(zahtevaVestinu, react);
        
        nikola.addProperty(imaVestinu, java);
        nikola.addProperty(imaVestinu, react);
        
        String s = """
                   PREFIX vbis: <http://www.vbis.org/ontology#>
                   SELECT ?student
                   WHERE {
                        ?student a vbis:Student .
                        ?student vbis:imaVestinu ?v .
                        vbis:JuniorDeveloper vbis:zahtevaVestinu ?v .
                   }
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
        
    }
}
