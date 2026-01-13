package com.mycompany.vbisapi.springbootapi;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@SpringBootApplication
public class SpringBootAPI {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootAPI.class, args);
    }

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
}
