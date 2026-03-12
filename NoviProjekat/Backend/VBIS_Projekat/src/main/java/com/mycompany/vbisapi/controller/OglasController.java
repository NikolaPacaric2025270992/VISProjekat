/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.controller;

import com.mycompany.vbisapi.model.Oglas;
import com.mycompany.vbisapi.model.RangiraniStudent;
import com.mycompany.vbisapi.service.FusekiService;
import com.mycompany.vbisapi.service.OglasService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author nikol
 */
@RestController
@RequestMapping("/api/oglasi")
public class OglasController {
    
    @Autowired
    private FusekiService fusekiService;
    
    @Autowired
    private OglasService oglasService;
    
    @PostMapping("/postavi")
    public String postavioglas(@RequestBody Oglas o){
        try{
            oglasService.postaviOglas(o);
            return "Uspeh: Oglas '" + o.getNaslov() + "' je uspesno objavljen u oba sistema!";
        } catch (Exception e){
            e.printStackTrace();
            return "Greska pri postavljanju oglasa: " + e.getMessage();
        }
    }
    
    @GetMapping("/{id}/rang-lista")
    public List<RangiraniStudent> getRangLista(@PathVariable String id) {
        return fusekiService.getRangListaStudenata(id);
    }
    
    @GetMapping("/provera")
    public String provera(){
        return "OglasController je aktivan!";
    }
}
