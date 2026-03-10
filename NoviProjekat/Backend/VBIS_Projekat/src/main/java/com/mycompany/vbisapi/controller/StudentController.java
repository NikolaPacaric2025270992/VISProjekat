/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.controller;

import com.mycompany.vbisapi.model.Student;
import com.mycompany.vbisapi.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author nikol
 */
@RestController
@RequestMapping("/api/studenti")
public class StudentController {
    
    @Autowired
    private StudentService studentService;
    
    @PostMapping("/registracija")
    public String registracijaStudenta(@RequestBody Student s){
        try {
            studentService.registrujStudenta(s);
            return "Uspeh: Student " + s.getIme() + " " + s.getPrezime() + " je registrovan u oba sistema!";
        } catch (Exception e){
            return "Greska pri registraciji: " + e.getMessage();
        }
    }
    
    @GetMapping("/provera")
    public String provera(){
        return "StudentController je aktivan i spreman!";
    }
}
