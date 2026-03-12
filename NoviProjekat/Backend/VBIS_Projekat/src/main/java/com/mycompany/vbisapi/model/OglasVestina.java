/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.model;

/**
 *
 * @author nikol
 */
public class OglasVestina {
    private Vestina vestina;
    private NivoSpremnosti nivo;
    private Prioritet prioritet;
    
    public OglasVestina(){}

    public OglasVestina(Vestina vestina, NivoSpremnosti nivo, Prioritet prioritet) {
        this.vestina = vestina;
        this.nivo = nivo;
        this.prioritet = prioritet;
    }

    public Vestina getVestina() {
        return vestina;
    }

    public void setVestina(Vestina vestina) {
        this.vestina = vestina;
    }

    public NivoSpremnosti getNivo() {
        return nivo;
    }

    public void setNivo(NivoSpremnosti nivo) {
        this.nivo = nivo;
    }

    public Prioritet getPrioritet() {
        return prioritet;
    }

    public void setPrioritet(Prioritet prioritet) {
        this.prioritet = prioritet;
    }
    
    
}
