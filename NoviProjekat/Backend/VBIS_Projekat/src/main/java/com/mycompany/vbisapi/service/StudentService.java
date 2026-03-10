/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.service;

import com.mycompany.vbisapi.model.Student;
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
    
    public void registrujStudenta(Student s){
        System.out.println("StudentService: Pokrecem sinhronu registraciju za " + s.getIme());
        
        arangoService.sacuvajStudenta(s);
        fusekiService.sacuvajStudentaURDF(s);
        
        System.out.println("StudentService: Registracija uspesno zavrsena u oba sistema.");
    }
}
