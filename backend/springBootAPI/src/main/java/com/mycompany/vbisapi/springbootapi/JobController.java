package com.mycompany.vbisapi.springbootapi;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jobs")
public class JobController {

    private final Jena jena;

    public JobController() {
        // Putanja do OWL fajla
        this.jena = new Jena("src/main/resources/OWL.owl");
    }

    @GetMapping("/students")
    public List<String> getAllStudents() {
        return jena.getAllStudents();
    }

    @GetMapping("/recommendations/{student}")
    public List<String> getRecommendations(@PathVariable String student) {
        return jena.getRecommendationsForStudent(student);
    }
}
