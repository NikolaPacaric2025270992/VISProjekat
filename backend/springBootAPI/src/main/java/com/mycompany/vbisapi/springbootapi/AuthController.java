package com.mycompany.vbisapi.springbootapi;

import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class AuthController {

    private final ArangoDatabase db;

    public AuthController(ArangoDatabase db) {
        this.db = db;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String role = body.get("role"); // student ili agency

        // Proveri da li dokument postoji
        if (!db.collection("users").documentExists(username)) {
            throw new RuntimeException("User not found");
        }

        BaseDocument user = db.collection("users").getDocument(username, BaseDocument.class);

        // Proveri da li role odgovara korisniku
        String actualRole = (String) user.getAttribute("role");
        if (!role.equals(actualRole)) {
            throw new RuntimeException("Incorrect role selected for this user");
        }

        // Vrati JSON prema ulozi
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("username", user.getKey());
        result.put("name", user.getAttribute("name"));
        result.put("role", actualRole);

        if ("student".equals(actualRole)) {
            result.put("skills", user.getAttribute("skills"));
            result.put("studyProgram", user.getAttribute("studyProgram"));
            result.put("lookingForJob", user.getAttribute("lookingForJob"));
        }

        return result;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        String name = (String) body.get("name");
        String role = (String) body.get("role");

        if (db.collection("users").documentExists(username)) {
            throw new RuntimeException("Username already exists");
        }

        BaseDocument user = new BaseDocument();
        user.setKey(username);
        user.addAttribute("name", name);
        user.addAttribute("role", role);

        if ("student".equals(role)) {
            user.addAttribute("skills", body.getOrDefault("skills", new String[]{}));
            user.addAttribute("studyProgram", body.getOrDefault("studyProgram", ""));
            user.addAttribute("lookingForJob", body.getOrDefault("lookingForJob", true));
        }

        db.collection("users").insertDocument(user);

        // Vrati isti JSON kao i login
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("username", user.getKey());
        result.put("name", user.getAttribute("name"));
        result.put("role", role);

        if ("student".equals(role)) {
            result.put("skills", user.getAttribute("skills"));
            result.put("studyProgram", user.getAttribute("studyProgram"));
            result.put("lookingForJob", user.getAttribute("lookingForJob"));
        }

        return result;
    }
}

