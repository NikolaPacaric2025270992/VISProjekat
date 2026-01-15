package com.mycompany.vbisapi.springbootapi;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired; // Dodaj ovaj import
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController; // Dodaj ovaj import
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.apache.jena.rdf.model.Model;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@SpringBootApplication
@RestController // OVO JE OBAVEZNO da bi Mapping radio
public class SpringBootAPI {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootAPI.class, args);
    }

    // Ubrizgavamo Jena bean koji smo definisali dole
    @Autowired
    private Jena jena;

    @Bean
    public ArangoDatabase arangoDatabase() {
        ArangoDB arangoDB = new ArangoDB.Builder()
                .host("127.0.0.1", 8529)
                .user("root")
                .password("")
                .build();
        if (!arangoDB.db("projekat").exists()) {
            arangoDB.createDatabase("projekat");
        }
        ArangoDatabase db = arangoDB.db("projekat");
        if (!db.collection("users").exists()) {
            db.createCollection("users");
        }
        return db;
    }

    @Bean
    public Jena jena() {
        return new Jena("src/main/resources/OWL.owl");
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
            }
        };
    }

    @GetMapping("/recommendations/{username}")
    public ResponseEntity<List<String>> getAds(@PathVariable String username) {
        // Sada će jena.getRecommendedAds raditi jer je @Autowired uradio svoje
        List<String> oglasi = jena.getRecommendedAds(username);
        return ResponseEntity.ok(oglasi);
    }
    
    // 1. IZVOZ ONTOLOGIJE (RDF/XML)
    @GetMapping("/export/ontology")
    public ResponseEntity<String> exportOntology() {
        Model model = jena.getModel(); // Uzimamo Jena model
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // Jena direktno piše u stream u RDF/XML formatu
        model.write(baos, "RDF/XML-ABBREV");
        
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=ontologija_export.rdf")
                .body(baos.toString());
    }

    // 2. IZVOZ KORISNIKA IZ ARANGODB (JSON)
    @GetMapping("/export/users")
    public ResponseEntity<List<Map<String, Object>>> exportUsers() {
        List<Map<String, Object>> users = new ArrayList<>();
        
        // AQL upit za izvlačenje svih korisnika
        String query = "FOR u IN users RETURN u";
        ArangoCursor<Map> cursor = arangoDatabase().query(query, Map.class);
        
        cursor.forEachRemaining(users::add);
        
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=korisnici_export.json")
                .body(users);
    }
    
    @PostMapping("/import/ontology")
    public ResponseEntity<String> importOntology(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Fajl je prazan!");
        }

        try {
            boolean success = jena.importData(file.getInputStream());
            if (success) {
                return ResponseEntity.ok("Podaci su uspešno uvezeni i validirani.");
            } else {
                return ResponseEntity.status(400).body("Uvoz nije uspeo. Podaci nisu konzistentni sa OWL šemom.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Greška pri obradi fajla.");
        }
    }
}