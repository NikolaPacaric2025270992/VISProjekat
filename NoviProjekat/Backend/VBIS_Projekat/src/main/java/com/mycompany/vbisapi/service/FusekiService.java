/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.service;

import com.mycompany.vbisapi.model.Oglas;
import com.mycompany.vbisapi.model.Student;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

/**
 *
 * @author nikol
 */
public class FusekiService {
    
    private static final String FUSEKI_URL = "http://localhost:3030/vbis_dataset/update";
    
    private static final String NS = "http://www.vbis-projekat.rs/model#";
    
    public void sacuvajStudentaURDF(Student s){
        String studentID = s.getEmail().replace("@", "_").replace(".", "_");
        
        String sparqlInsert = "PREFIX : <" + NS + "> " +
                              "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                              "INSERT DATA { " +
                              "  :" + studentID + " rdf:type :Student ; " +
                              "                   :imaIme \"" + s.getIme() + "\" ; " +
                              "                   :imaPrezime \"" + s.getPrezime() + "\" . " +
                              "}";
        
        try {
            UpdateRequest request = UpdateFactory.create(sparqlInsert);
            UpdateExecutionFactory.createRemote(request, FUSEKI_URL).execute();
            System.out.println("Student " + s.getIme() + "uspesno poslat u Fuseki!");
        } catch(Exception e){
            System.err.println("Greska pri slanju u Fuseki: " + e.getMessage());
        }
    }
    
    public void sacuvajOglasURDF(Oglas o) {
        // Pretvaramo ENUM (npr. VISOK) u CamelCase (Visok) da se poklopi sa ontologijom
        String prioritetID = o.getPrioritet().toString().substring(0, 1).toUpperCase() 
                           + o.getPrioritet().toString().substring(1).toLowerCase();
        
        String nivoID = o.getZahtevaniNivo().toString().substring(0, 1).toUpperCase() 
                      + o.getZahtevaniNivo().toString().substring(1).toLowerCase();

        String sparqlInsert = "PREFIX : <" + NS + "> " +
                              "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                              "INSERT DATA { " +
                              "  :" + o.getId() + " rdf:type :Oglas ; " +
                              "                   :imaNaslov \"" + o.getNaslov() + "\" ; " +
                              "                   :imaPrioritet :" + prioritetID + " ; " +
                              "                   :zahtevaNivo :" + nivoID + " . " +
                              "}";

        try {
            UpdateRequest request = UpdateFactory.create(sparqlInsert);
            UpdateExecutionFactory.createRemote(request, FUSEKI_URL).execute();
            System.out.println("Oglas '" + o.getNaslov() + "' uspešno sinhronizovan!");
        } catch (Exception e) {
            System.err.println("Greška: " + e.getMessage());
        }
    }
}
