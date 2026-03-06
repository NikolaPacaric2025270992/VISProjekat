/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.vbisapi.vbis_projekat;

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
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        
        try{
            InputStream in = VBIS_Projekat.class.getClassLoader().getResourceAsStream("model.rdf");
            
            if (in == null){
                System.err.println("GRESKA: Fajl model.rdf nije pronadjen u resources folderu!");
                return;
            }
            
            model.read(in, null);
            System.err.println("--- Ontologija je uspesno ucitana ---");
            
            System.err.println("Instance u modelu: ");
            Iterator<Individual> it = model.listIndividuals();
            
            if (!it.hasNext()){
                System.err.println("nema pronadjenih individua (mozda je fajl prazan?).");
            }
            
            while (it.hasNext()){
                Individual ind = it.next();
                System.err.println("- " + ind.getLocalName() + " (Tip: " + ind.getOntClass().getLocalName() + ")");
            }
        }catch (Exception e) {
            System.out.println("Doslo je do greske: " + e.getMessage());
            e.printStackTrace();
        }
    }   
}
