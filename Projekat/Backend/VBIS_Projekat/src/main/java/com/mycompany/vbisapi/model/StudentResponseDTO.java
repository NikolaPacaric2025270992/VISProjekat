package com.mycompany.vbisapi.model;

public class StudentResponseDTO {
    private String id;
    private String ime;
    private String prezime;
    private String email;
    private String nivoStudija;
    private boolean traziZaposlenje;

    public StudentResponseDTO() {
    }

    public StudentResponseDTO(Student student) {
        this.id = student.getId();
        this.ime = student.getIme();
        this.prezime = student.getPrezime();
        this.email = student.getEmail();
        this.nivoStudija = student.getNivoStudija();
        this.traziZaposlenje = student.isTraziZaposlenje();
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

    public String getNivoStudija() {
        return nivoStudija;
    }

    public void setNivoStudija(String nivoStudija) {
        this.nivoStudija = nivoStudija;
    }

    public boolean isTraziZaposlenje() {
        return traziZaposlenje;
    }

    public void setTraziZaposlenje(boolean traziZaposlenje) {
        this.traziZaposlenje = traziZaposlenje;
    }
}
