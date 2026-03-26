/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.model;

/**
 *
 * @author nikol
 */
public class PreporuceniOglas {
    private String oglasId;
    private String naslov;
    private double bodovi;
    
    public PreporuceniOglas(){}

    public PreporuceniOglas(String oglasId, String naslov, double bodovi) {
        this.oglasId = oglasId;
        this.naslov = naslov;
        this.bodovi = bodovi;
    }

    public String getOglasId() {
        return oglasId;
    }

    public void setOglasId(String oglasId) {
        this.oglasId = oglasId;
    }

    public String getNaslov() {
        return naslov;
    }

    public void setNaslov(String naslov) {
        this.naslov = naslov;
    }

    public double getBodovi() {
        return bodovi;
    }

    public void setBodovi(double bodovi) {
        this.bodovi = bodovi;
    }
    
    
}
