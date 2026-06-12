package com.mycompany.vbisapi.model;

public class PromenaLozinkeDTO {
    private String email;
    private String staraLozinka;
    private String novaLozinka;

    public PromenaLozinkeDTO() {
    }

    public PromenaLozinkeDTO(String email, String staraLozinka, String novaLozinka) {
        this.email = email;
        this.staraLozinka = staraLozinka;
        this.novaLozinka = novaLozinka;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStaraLozinka() {
        return staraLozinka;
    }

    public void setStaraLozinka(String staraLozinka) {
        this.staraLozinka = staraLozinka;
    }

    public String getNovaLozinka() {
        return novaLozinka;
    }

    public void setNovaLozinka(String novaLozinka) {
        this.novaLozinka = novaLozinka;
    }
}
