/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.service;

import com.mycompany.vbisapi.model.Oglas;
import com.mycompany.vbisapi.model.OglasVestina;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author nikol
 */
@Service
public class OglasService {
    
    @Autowired
    private ArangoService arango;
    
    @Autowired
    private FusekiService fuseki;
    
    public void postaviOglas(Oglas o){
        validirajZahtevaneVestine(o);

        arango.sacuvajOglas(o);
        try {
            fuseki.sacuvajOglasURDF(o);
        } catch (RuntimeException e) {
            SinhronizacijaHelper.rollbackArangoUpis(
                    "oglasa",
                    o.getId(),
                    () -> arango.obrisiOglas(o.getId()),
                    e);
            throw e;
        }
        
        System.out.println("OglasService: Oglas '" + 
                            o.getNaslov() + "' je potpuno sinhronizovan.");
    }
    
    public void obrisiOglas(String id) {
        fuseki.obrisiOglasIzRDF(id);
        arango.obrisiOglas(id);
        
        System.out.println("OglasService: Oglas " + id + " uspesno uklonjen iz obe baze.");
    }
    
    public List<Oglas> nadjiOglaseAgencije(String agencijaId) {
        return arango.nadjiOglasePoAgenciji(agencijaId);
    }
    
    public List<Oglas> dobijSveOglase() {
        return arango.sviOglasi(); 
    }

    private void validirajZahtevaneVestine(Oglas o) {
        if (o.getZahtevaneVestine() == null || o.getZahtevaneVestine().isEmpty()) {
            return;
        }

        Set<String> vidjeneVestine = new HashSet<>();
        for (OglasVestina zahtev : o.getZahtevaneVestine()) {
            String vestinaId = zahtev.getVestina() != null ? zahtev.getVestina().getId() : null;
            if (vestinaId == null || vestinaId.isBlank()) {
                throw new IllegalArgumentException("Svaka zahtevana veština mora imati ID.");
            }

            if (!vidjeneVestine.add(vestinaId)) {
                throw new IllegalArgumentException("Veština '" + vestinaId + "' je duplirana u istom oglasu.");
            }
        }
    }
}
