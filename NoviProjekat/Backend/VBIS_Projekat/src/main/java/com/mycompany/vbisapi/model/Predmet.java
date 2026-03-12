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
    private Vestina vestina;
    private NivoSpremnosti nivoKojiNudi;
    private String predavacId;
    
    public Predmet(){}

    public Predmet(String id, String nazivPredmeta, int ects, Vestina vestina, NivoSpremnosti nivoKojiNudi, String predavacId) {
        this.id = id;
        this.nazivPredmeta = nazivPredmeta;
        this.ects = ects;
        this.vestina = vestina;
        this.nivoKojiNudi = nivoKojiNudi;
        this.predavacId = predavacId;
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

    public Vestina getVestina() {
        return vestina;
    }

    public void setVestina(Vestina vestina) {
        this.vestina = vestina;
    }

    public NivoSpremnosti getNivoKojiNudi() {
        return nivoKojiNudi;
    }

    public void setNivoKojiNudi(NivoSpremnosti nivoKojiNudi) {
        this.nivoKojiNudi = nivoKojiNudi;
    }

    public String getPredavacId() {
        return predavacId;
    }

    public void setPredavacId(String predavacId) {
        this.predavacId = predavacId;
    }
    
}
