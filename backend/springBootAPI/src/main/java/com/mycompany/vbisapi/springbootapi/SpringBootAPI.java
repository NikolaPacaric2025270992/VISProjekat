package com.mycompany.vbisapi.mavenproject1;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class SpringBootAPI {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootAPI.class, args);
    }
    
    public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // omogućava CORS za sve putanje
                        .allowedOrigins("http://localhost:3000") // samo frontend
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
            }
        };
    }
}

    @RestController
    class ApiController {

        // Povezivanje na ArangoDB
        private final ArangoDB arangoDB = new ArangoDB.Builder()
                .host("127.0.0.1", 8529)
                .user("root")
                .password("")
                .build();

        private final ArangoDatabase db = arangoDB.db("projekat");

        public ApiController() {
            // Kreiranje baze i kolekcije ako ne postoje
            if (!db.exists()) {
                arangoDB.createDatabase("projekat");
            }
            if (!db.collection("users").exists()) {
                db.createCollection("users");
            }
        }

        // Endpoint za sve studente
        @GetMapping("/students")
        public List<String> getStudents() {
            List<String> students = new ArrayList<>();
            String aql = "FOR u IN users FILTER u.role == @role RETURN u.name";
            db.query(aql,
                    Map.of("role", "student"),
                    null,
                    String.class)
              .forEachRemaining(students::add);
            return students;
        }

        // Endpoint za preporuke (SPARQL simulacija)
        @GetMapping("/recommendations")
        public List<String> getRecommendations() {
            // Ovde samo simuliramo SPARQL upit
            // Kasnije možeš povezati sa tvojim Jena.java logikom
            List<String> recommendations = new ArrayList<>();
            recommendations.add("Ana -> BackendDev (score 41)");
            recommendations.add("Ana -> JuniorJava (score 37)");
            recommendations.add("Jelena -> FrontendDev (score 34)");
            recommendations.add("Marko -> PythonDev (score 31)");
            return recommendations;
        }
    }
}
