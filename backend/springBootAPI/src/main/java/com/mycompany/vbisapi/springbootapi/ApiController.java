/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vbisapi.springbootapi;


import com.arangodb.ArangoDatabase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author nikol
 */
@RestController
public class ApiController {

    private final ArangoDatabase db;

    public ApiController(ArangoDatabase db) {
        this.db = db;
    }

    @GetMapping("/students")
    public List<String> getStudents() {
        List<String> students = new ArrayList<>();
        String aql = "FOR u IN users FILTER u.role == @role RETURN u.name";
        db.query(aql, Map.of("role", "student"), null, String.class)
          .forEachRemaining(students::add);
        return students;
    }

    @GetMapping("/recommendations")
    public List<String> getRecommendations() {
        List<String> recommendations = new ArrayList<>();
        recommendations.add("Ana -> BackendDev (score 41)");
        recommendations.add("Ana -> JuniorJava (score 37)");
        recommendations.add("Jelena -> FrontendDev (score 34)");
        recommendations.add("Marko -> PythonDev (score 31)");
        return recommendations;
    }
}

