/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.vbisapi.vbis_projekat;

import com.mycompany.vbisapi.model.Agencija;
import com.mycompany.vbisapi.model.NivoSpremnosti;
import com.mycompany.vbisapi.model.Oglas;
import com.mycompany.vbisapi.model.Polaganje;
import com.mycompany.vbisapi.model.Predmet;
import com.mycompany.vbisapi.model.Prioritet;
import com.mycompany.vbisapi.model.Student;
import com.mycompany.vbisapi.model.Vestina;
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
        ArangoService arango = new ArangoService();
        FusekiService fuseki = new FusekiService();
        
        System.out.println("Inicijalizujem ArangoDB bazu i kolekcije...");
        arango.inicijalizujSistem();

        System.out.println("Započinjem sinhronizaciju...");

        // 1. ZAJEDNIČKI OBJEKAT VEŠTINE
        Vestina javaVestina = new Vestina("Vestina_Java", "Java Programiranje");
        fuseki.sacuvajVestinuURDF(javaVestina); // <-- OVO JE KLJUČ ZA POPUNJENO POLJE
        

        // 2. STUDENT (Dodaj sva polja koja tvoj konstruktor traži: npr. email, lozinka, ime, prezime, indeks, telefon...)
        Student s = new Student(
            "2021230000",                     // id (Indeks)
            "Nikola",                         // ime
            "Pacaric",                        // prezime
            "nikola.final@vbis-projekat.rs",  // email
            "lozinka123",                     // lozinka
            "Osnovne studije"                 // nivoStudija (umesto broja telefona)
        );
        arango.sacuvajStudenta(s);     // Arango čuva SVE (ceo profil)
        fuseki.sacuvajStudentaURDF(s); // Fuseki uzima samo ime, prezime i email za graf

        // 3. AGENCIJA (PIB, adresa, naziv, email...)
        Agencija agencija = new Agencija(
            "agencija_it_solutions",                // id
            "IT Solutions d.o.o.",                  // nazivAgencije
            "123456789",                            // pib (ovde ide PIB!)
            "Bulevar Mihajla Pupina 10, Novi Sad",  // lokacija
            "it@solutions.rs",                      // email (ovde ide EMAIL!)
            "lozinka123"                            // lozinka
        );
        arango.sacuvajAgenciju(agencija);
        fuseki.sacuvajAgencijuURDF(agencija);

        // 4. PREDMET (ID, naziv, ECTS, veština)
        Predmet vis = new Predmet("predmet_vis", "Veb Informacioni Sistemi", 6, javaVestina);
        arango.sacuvajPredmet(vis);
        fuseki.sacuvajPredmetURDF(vis);

        // 5. OGLAS (ID, naslov, opis, plata, agencijaId, nivo, prioritet, veština)
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

        // 6. POLAGANJE (ID, studentId, predmetId, ocena)
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
    }
}
