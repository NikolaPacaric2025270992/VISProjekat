/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.model;

/**
 *
 * @author nikol
 */
public class Polaganje {
    private String id;
    private String studentId;
    private String predmetId;
    private int ocena; 
    
    public Polaganje(){}

    public Polaganje(String id, String studentId, String predmetId, int ocena) {
        this.id = id;
        this.studentId = studentId;
        this.predmetId = predmetId;
        this.ocena = ocena;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getPredmetId() {
        return predmetId;
    }

    public void setPredmetId(String predmetId) {
        this.predmetId = predmetId;
    }

    public int getOcena() {
        return ocena;
    }

    public void setOcena(int ocena) {
        if (ocena >= 6 && ocena <= 10){
            this.ocena = ocena;
        } else {
            throw new IllegalArgumentException("ocena mora biti izmedju 6 i 10!");
        }
    }
    
    
}
