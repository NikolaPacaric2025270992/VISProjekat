/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.model;

/**
 *
 * @author nikol
 */
public class Predmet {
    private String id;
    private String nazivPredmeta;
    private int ects;
    private Vestina vestinaId;
    
    public Predmet(){}

    public Predmet(String id, String nazivPredmeta, int ects, Vestina vestinaId) {
        this.id = id;
        this.nazivPredmeta = nazivPredmeta;
        this.ects = ects;
        this.vestinaId = vestinaId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNazivPredmeta() {
        return nazivPredmeta;
    }

    public void setNazivPredmeta(String nazivPredmeta) {
        this.nazivPredmeta = nazivPredmeta;
    }

    public int getEcts() {
        return ects;
    }

    public void setEcts(int ects) {
        this.ects = ects;
    }

    public Vestina getVestinaId() {
        return vestinaId;
    }

    public void setVestinaId(Vestina vestinaId) {
        this.vestinaId = vestinaId;
    }
}
