/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.mycompany.vbisapi.model.Oglas;
import com.mycompany.vbisapi.model.Polaganje;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author nikol
 */
@Service
public class ImportService {
    
    public List<Oglas> obradiFajlSaOglasima(MultipartFile fajl) throws Exception {
        String fileName = fajl.getOriginalFilename();
        
        if (fileName != null && fileName.endsWith(".json")) {
            return obradiJson(fajl);
        } else if (fileName != null && fileName.endsWith(".xml")) {
            return obradiXml(fajl);
        } else {
            throw new IllegalArgumentException(
                    "Nepodzan format fajla! Dozvoljeni su samo .json i .xml");
        }
    }
    
    private List<Oglas> obradiJson(MultipartFile fajl) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(fajl.getInputStream());

        // 1. Učitavanje JSON šeme
        InputStream schemaStream = new ClassPathResource("schemas/oglas-schema.json").getInputStream();
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        com.networknt.schema.JsonSchema schema = factory.getSchema(schemaStream);

        // 2. Validacija
        Set<ValidationMessage> errors = schema.validate(jsonNode);
        if (!errors.isEmpty()) {
            throw new Exception("JSON Validacija nije uspela: " + errors.toString());
        }

        // 3. Parsiranje u List<Oglas>
        return mapper.readValue(fajl.getInputStream(), new TypeReference<List<Oglas>>(){});
    }
    
    private List<Oglas> obradiXml(MultipartFile fajl) throws Exception {
        // 1. Validacija XML-a spram XSD šeme
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new ClassPathResource("schemas/oglas-schema.xsd").getFile());
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(fajl.getInputStream()));

        // 2. Parsiranje
        XmlMapper xmlMapper = new XmlMapper();
        return xmlMapper.readValue(fajl.getInputStream(), new TypeReference<List<Oglas>>(){});
    }
    
    public List<Polaganje> obradiFajlSaPolaganjima(MultipartFile fajl) throws Exception {
        String fileName = fajl.getOriginalFilename();
        
        if (fileName != null && fileName.endsWith(".json")) {
            return obradiPolaganjaJson(fajl);
        } else if (fileName != null && fileName.endsWith(".xml")) {
            return obradiPolaganjaXml(fajl);
        } else {
            throw new IllegalArgumentException("Nepodržan format fajla! Dozvoljeni su samo .json i .xml");
        }
    }
    
    private List<Polaganje> obradiPolaganjaJson(MultipartFile fajl) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(fajl.getInputStream());

        // 1. Učitavanje JSON šeme za polaganja
        InputStream schemaStream = new ClassPathResource("schemas/polaganje-schema.json").getInputStream();
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        com.networknt.schema.JsonSchema schema = factory.getSchema(schemaStream);

        // 2. Validacija
        Set<ValidationMessage> errors = schema.validate(jsonNode);
        if (!errors.isEmpty()) {
            throw new Exception("JSON Validacija polaganja nije uspela: " + errors.toString());
        }

        // 3. Parsiranje
        return mapper.readValue(fajl.getInputStream(), new TypeReference<List<Polaganje>>(){});
    }
    
    private List<Polaganje> obradiPolaganjaXml(MultipartFile fajl) throws Exception {
        // 1. Validacija XML-a spram XSD šeme
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new ClassPathResource("schemas/polaganje-schema.xsd").getFile());
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(fajl.getInputStream()));

        // 2. Parsiranje
        XmlMapper xmlMapper = new XmlMapper();
        return xmlMapper.readValue(fajl.getInputStream(), new TypeReference<List<Polaganje>>(){});
    }
}
