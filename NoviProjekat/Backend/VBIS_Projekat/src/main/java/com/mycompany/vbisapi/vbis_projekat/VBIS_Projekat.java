/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.vbisapi.vbis_projekat;

import com.mycompany.vbisapi.model.Student;
import com.mycompany.vbisapi.service.ArangoService;
import com.mycompany.vbisapi.service.FusekiService;
import java.io.InputStream;
import java.util.Iterator;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;

/**
 *
 * @author nikol
 */
public class VBIS_Projekat {

    public static void main(String[] args) {
        
        ArangoService arangoService = new ArangoService();
        FusekiService fusekiService = new FusekiService();
        
        try{
            System.out.println("Zapocinjem povezivanje sa ArangoDB...");
            
            arangoService.inicijalizujSistem();
            
            Student noviStudent = new Student();
            noviStudent.setIme("Nikola");
            noviStudent.setPrezime("Peric");
            noviStudent.setEmail("nikola.peric@example.com");
            noviStudent.setNivoStudija("Master");
            
            arangoService.sacuvajStudenta(noviStudent);
            fusekiService.sacuvajStudentaURDF(noviStudent);
            
            System.out.println("\n--- TEST PROSAO ---");
            System.out.println("Idi na 127.0.0.1:8529 uloguj se u bazu 'projekat'");
            System.out.println("i proveri kolekciju 'studenti'!");
        }catch (Exception e) {
            System.err.println("Doslo je do greske pri radu sa ArangoDB:");
            e.printStackTrace();
        }
    }   
}
