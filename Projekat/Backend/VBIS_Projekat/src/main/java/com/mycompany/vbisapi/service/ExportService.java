/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 *
 * @author nikol
 */
@Service
public class ExportService {
    
    // Izvoz u JSON formatu
    public byte[] eksportujUJson(List<?> podaci) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(podaci);
    }

    // Izvoz u XML formatu
    public byte[] eksportujUXml(List<?> podaci) throws Exception {
        XmlMapper xmlMapper = new XmlMapper();
        return xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(podaci);
    }
}
