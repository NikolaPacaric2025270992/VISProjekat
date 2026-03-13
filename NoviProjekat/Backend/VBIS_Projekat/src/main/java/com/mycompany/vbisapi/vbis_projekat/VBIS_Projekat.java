/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.vbisapi.vbis_projekat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 *
 * @author nikol
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.mycompany.vbisapi")
public class VBIS_Projekat {

    public static void main(String[] args) {
        SpringApplication.run(VBIS_Projekat.class, args);
        System.out.println("SERVER JE UPALJEN I ČEKA NA http://localhost:8080");
        /*
        ArangoService arango = new ArangoService();
        FusekiService fuseki = new FusekiService();
        
        System.out.println("Inicijalizujem ArangoDB bazu i kolekcije...");
        arango.inicijalizujSistem();

        System.out.println("Započinjem sinhronizaciju...");


        Vestina javaVestina = new Vestina("Vestina_Java", "Java Programiranje");
        fuseki.sacuvajVestinuURDF(javaVestina); 
        

        Student s = new Student(
            "2021230000",                  
            "Nikola",                     
            "Pacaric",                      
            "nikola.final@vbis-projekat.rs", 
            "lozinka123",                  
            "Osnovne studije"              
        );
        arango.sacuvajStudenta(s);   
        fuseki.sacuvajStudentaURDF(s); 

        Agencija agencija = new Agencija(
            "agencija_it_solutions",               
            "IT Solutions d.o.o.",                 
            "123456789",                         
            "Bulevar Mihajla Pupina 10, Novi Sad", 
            "it@solutions.rs",                      
            "lozinka123"                            
        );
        arango.sacuvajAgenciju(agencija);
        fuseki.sacuvajAgencijuURDF(agencija);

        Predmet vis = new Predmet("predmet_vis", "Veb Informacioni Sistemi", 6, javaVestina);
        arango.sacuvajPredmet(vis);
        fuseki.sacuvajPredmetURDF(vis);

        Oglas oglas = new Oglas(
            "oglas_java_senior", 
            "Senior Java Developer", 
            "Rad na razvoju kompleksnih mikroservisa u Spring Boot-u.", 
            3500.0, 
            agencija.getId(), 
            NivoSpremnosti.NAPREDNI, 
            Prioritet.VISOK, 
            javaVestina
        );
        arango.sacuvajOglas(oglas);
        fuseki.sacuvajOglasURDF(oglas);

        String studentID = s.getEmail().replace("@", "_").replace(".", "_");
        Polaganje pol = new Polaganje(
            "polaganje_nikola_vis", 
            studentID, 
            vis.getId(), 
            10
        );
        arango.sacuvajPolaganje(pol);
        fuseki.sacuvajPolaganjeURDF(pol);

        System.out.println("\n[STATUS] Sinhronizacija uspešna za oba sistema!");
        */
    }
}
