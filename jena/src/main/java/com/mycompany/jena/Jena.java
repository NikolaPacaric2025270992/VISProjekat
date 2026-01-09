/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.jena;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

/**
 *
 * @author Nikola
 */
public class Jena {

    public static void main(String[] args) {
        Model model = ModelFactory.createDefaultModel().read("src/main/resources/OWL.owl");
        
        ResIterator classes = model.listResourcesWithProperty(RDF.type, OWL.Class);
        while (classes.hasNext()){
            Resource cls = classes.nextResource();
            System.out.println(cls.getLocalName());
        }
    }
}
