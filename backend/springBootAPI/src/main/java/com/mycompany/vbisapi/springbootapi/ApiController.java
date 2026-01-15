package com.mycompany.vbisapi.springbootapi;

import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final ArangoDatabase db;

    public ApiController(ArangoDatabase db) {
        this.db = db;
    }

    @GetMapping("/students")
    public List<Map<String,Object>> getStudentsLookingForJob() {
        List<Map<String,Object>> students = new ArrayList<>();
        String aql = "FOR u IN users FILTER u.role=='student' AND u.lookingForJob==true RETURN u";
        db.query(aql, null, null, BaseDocument.class)
                .forEachRemaining(doc -> {
                    Map<String,Object> s = new HashMap<>();
                    s.put("username", doc.getKey());
                    s.put("name", doc.getAttribute("name"));
                    s.put("skills", doc.getAttribute("skills"));
                    s.put("studyProgram", doc.getAttribute("studyProgram"));
                    s.put("lookingForJob", doc.getAttribute("lookingForJob"));
                    students.add(s);
                });
        return students;
    }

    @GetMapping("/students/{username}")
    public Map<String,Object> getStudentProfile(@PathVariable String username) {
        BaseDocument doc = db.collection("users").getDocument(username, BaseDocument.class);
        if(doc==null) return Map.of("error","Student not found");
        return Map.of(
                "username", doc.getKey(),
                "name", doc.getAttribute("name"),
                "skills", doc.getAttribute("skills"),
                "studyProgram", doc.getAttribute("studyProgram"),
                "lookingForJob", doc.getAttribute("lookingForJob")
        );
    }
}
