/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.model;

import java.util.List;

/**
 *
 * @author nikol
 */
public class Oglas {
    private String id;
    private String naslov;
    private String opis;
    private double plata;
    private String agencijaId;
    private NivoSpremnosti zahtevaniNivo;
    private Prioritet prioritet;
    private List<Vestina> zahtevaneVestine;
    
    public Oglas(){}

    public Oglas(String id, String naslov, String opis, double plata, String agencijaId, NivoSpremnosti zahtevaniNivo, Prioritet prioritet, List<Vestina> zahtevaneVestine) {
        this.id = id;
        this.naslov = naslov;
        this.opis = opis;
        this.plata = plata;
        this.agencijaId = agencijaId;
        this.zahtevaniNivo = zahtevaniNivo;
        this.prioritet = prioritet;
        this.zahtevaneVestine = zahtevaneVestine;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNaslov() {
        return naslov;
    }

    public void setNaslov(String naslov) {
        this.naslov = naslov;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public double getPlata() {
        return plata;
    }

    public void setPlata(double plata) {
        this.plata = plata;
    }

    public String getAgencijaId() {
        return agencijaId;
    }

    public void setAgencijaId(String agencijaId) {
        this.agencijaId = agencijaId;
    }

    public NivoSpremnosti getZahtevaniNivo() {
        return zahtevaniNivo;
    }

    public void setZahtevaniNivo(NivoSpremnosti zahtevaniNivo) {
        this.zahtevaniNivo = zahtevaniNivo;
    }

    public Prioritet getPrioritet() {
        return prioritet;
    }

    public void setPrioritet(Prioritet prioritet) {
        this.prioritet = prioritet;
    }

    public List<Vestina> getZahtevaneVestine() {
        return zahtevaneVestine;
    }

    public void setZahtevaneVestine(List<Vestina> zahtevaneVestine) {
        this.zahtevaneVestine = zahtevaneVestine;
    }
}
