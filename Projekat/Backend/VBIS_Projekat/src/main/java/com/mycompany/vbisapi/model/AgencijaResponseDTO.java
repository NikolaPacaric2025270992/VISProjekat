package com.mycompany.vbisapi.model;

public class AgencijaResponseDTO {
    private String id;
    private String nazivAgencije;
    private String pib;
    private String lokacija;
    private String email;

    public AgencijaResponseDTO() {
    }

    public AgencijaResponseDTO(Agencija agencija) {
        this.id = agencija.getId();
        this.nazivAgencije = agencija.getNazivAgencije();
        this.pib = agencija.getPib();
        this.lokacija = agencija.getLokacija();
        this.email = agencija.getEmail();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNazivAgencije() {
        return nazivAgencije;
    }

    public void setNazivAgencije(String nazivAgencije) {
        this.nazivAgencije = nazivAgencije;
    }

    public String getPib() {
        return pib;
    }

    public void setPib(String pib) {
        this.pib = pib;
    }

    public String getLokacija() {
        return lokacija;
    }

    public void setLokacija(String lokacija) {
        this.lokacija = lokacija;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
