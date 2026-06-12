/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.service;

import com.mycompany.vbisapi.model.Polaganje;
import com.mycompany.vbisapi.model.Student;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author nikol
 */
@Service
public class StudentService {
    @Autowired
    private ArangoService arangoService;
    
    @Autowired
    private FusekiService fusekiService;

    @Autowired
    private PolaganjeService polaganjeService;
    
    public Student login(String email, String lozinka){
        return arangoService.loginStudent(email, lozinka);
    }
    
    public void registrujStudenta(Student s){
        System.out.println("StudentService: Pokrecem sinhronu registraciju za " + s.getIme());
        
        arangoService.sacuvajStudenta(s);
        try {
            fusekiService.sacuvajStudentaURDF(s);
        } catch (RuntimeException e) {
            SinhronizacijaHelper.rollbackArangoUpis(
                    "studenta",
                    s.getId(),
                    () -> arangoService.obrisiStudenta(s.getId()),
                    e);
            throw e;
        }
        
        System.out.println("StudentService: Registracija uspesno zavrsena u oba sistema.");
    }
    
    public void azurirajStudenta(Student s){
        arangoService.azurirajStudenta(s);
        fusekiService.azurirajStudentaURDF(s);
    }

    public void promeniLozinku(String email, String staraLozinka, String novaLozinka) {
        if (novaLozinka == null || novaLozinka.length() < 5) {
            throw new IllegalArgumentException("Nova lozinka mora imati bar 5 karaktera.");
        }

        Student student = arangoService.loginStudent(email, staraLozinka);
        if (student == null) {
            throw new IllegalArgumentException("Trenutna lozinka nije tačna.");
        }

        arangoService.promeniLozinkuStudenta(email, novaLozinka);
    }

    public List<Student> nadjiAktivneStudente() {
        return arangoService.nadjiAktivneStudente();
    }
    
    public void obrisiStudenta(String id) {
        List<Polaganje> polaganja = arangoService.nadjiPolaganjaStudenta(id);
        for (Polaganje polaganje : polaganja) {
            polaganjeService.obrisiPolaganje(polaganje.getId());
        }

        fusekiService.obrisiKorisnikaIzRDF(id);
        arangoService.obrisiStudenta(id);
        
        System.out.println("StudentService: Student " + id + " je potpuno uklonjen iz sistema.");
    }
}
