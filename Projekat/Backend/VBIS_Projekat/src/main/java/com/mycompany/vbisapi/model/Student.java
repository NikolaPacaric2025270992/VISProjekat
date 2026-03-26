/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.model;

/**
 *
 * @author nikol
 */
public class Student {
    private String id;
    private String ime;
    private String prezime;
    private String email;
    private String lozinka;
    private String nivoStudija;
    private boolean traziZaposlenje;
    
    public Student() {}

    public Student(String id, String ime, String prezime, String email, String lozinka, String nivoStudija, boolean traziZaposlenje) {
        this.id = id;
        this.ime = ime;
        this.prezime = prezime;
        this.email = email;
        this.lozinka = lozinka;
        this.nivoStudija = nivoStudija;
        this.traziZaposlenje = traziZaposlenje;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getPrezime() {
        return prezime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLozinka() {
        return lozinka;
    }

    public void setLozinka(String lozinka) {
        this.lozinka = lozinka;
    }

    public String getNivoStudija() {
        return nivoStudija;
    }

    public void setNivoStudija(String nivoStudija) {
        this.nivoStudija = nivoStudija;
    }
    
    public boolean isTraziZaposlenje(){
        return traziZaposlenje;
    }
    
    public void setTraziZaposlenje(boolean traziZaposlenje){
        this.traziZaposlenje = traziZaposlenje;
    }
}
