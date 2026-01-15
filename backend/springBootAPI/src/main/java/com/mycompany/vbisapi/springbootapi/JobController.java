package com.mycompany.vbisapi.springbootapi;

import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.jena.rdf.model.*;

@RestController
@RequestMapping("/jobs")
public class JobController {

    private final Jena jena;

    public JobController(Jena jena) {
        this.jena = jena;
    }

    @GetMapping("/list")
    public List<Map<String,Object>> getAllJobs() {
        List<Map<String,Object>> jobs = new ArrayList<>();
        Model model = jena.getModel();
        String NS = jena.getNS();

        model.listResourcesWithProperty(model.getProperty(NS+"postavljenOd"))
                .forEachRemaining(res -> {
                    Map<String,Object> job = new HashMap<>();
                    job.put("jobName", res.getLocalName());

                    Statement agencyStmt = res.getProperty(model.getProperty(NS+"postavljenOd"));
                    job.put("agencyName", agencyStmt != null ? agencyStmt.getObject().asResource().getLocalName() : "Unknown");

                    List<Map<String,Object>> skills = new ArrayList<>();
                    res.listProperties(model.getProperty(NS+"zahtevaVestinu"))
                            .forEachRemaining(p -> {
                                Resource reqRes = p.getObject().asResource();
                                Resource skillRes = reqRes.getProperty(model.getProperty(NS+"zaVestinu")).getObject().asResource();
                                int priority = reqRes.getProperty(model.getProperty(NS+"prioritet")).getInt();
                                skills.add(Map.of("skill", skillRes.getLocalName(), "priority", priority));
                            });
                    job.put("skills", skills);
                    jobs.add(job);
                });

        return jobs;
    }

    @PostMapping
    public Map<String,Object> createJob(@RequestBody Map<String,Object> payload) {
        String jobName = (String) payload.get("jobName");
        String agencyName = (String) payload.get("agencyName");
        List<Map<String,Object>> skills = (List<Map<String,Object>>) payload.get("skills");

        Map<String,Integer> skillMap = skills.stream()
                .collect(Collectors.toMap(
                        s -> (String)s.get("skill"),
                        s -> ((Number)s.get("priority")).intValue()
                ));

        jena.addJob(jobName, skillMap, agencyName);
        return Map.of("jobName", jobName, "agencyName", agencyName, "skills", skillMap);
    }

    @GetMapping("/recommendations/{username}")
    public List<String> getRecommendations(@PathVariable String username) {
        return jena.getRecommendationsForStudent(username);
    }
}
