package com.mycompany.vbisapi.springbootapi;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;

import java.util.ArrayList;
import java.util.List;

public class Jena {

    private final Model model;
    private final String NS = "http://www.vbis.org/ontology#";

    public Jena(String owlFilePath) {
        // Učitaj OWL fajl
        this.model = ModelFactory.createDefaultModel().read(owlFilePath);
    }

    /**
     * Vrati sve studente iz RDF modela
     */
    public List<String> getAllStudents() {
        List<String> students = new ArrayList<>();
        ResIterator iter = model.listResourcesWithProperty(
                RDF.type,
                model.getResource(NS + "Student")
        );
        while (iter.hasNext()) {
            students.add(iter.next().getLocalName());
        }
        return students;
    }

    /**
     * Dobavi preporuke za studenta po SPARQL scoringu
     */
    public List<String> getRecommendationsForStudent(String studentName) {
        List<String> recommendations = new ArrayList<>();

        String sparql = """
            PREFIX vbis: <http://www.vbis.org/ontology#>
            
            SELECT ?job (SUM(?n * ?p) AS ?score)
            WHERE {
              vbis:%s a vbis:Student .
              vbis:%s vbis:imaVestinu ?sv .
              ?sv vbis:zaVestinu ?skill .
              ?sv vbis:nivoVestine ?n .

              ?job a vbis:Oglas .
              ?job vbis:zahtevaVestinu ?ov .
              ?ov vbis:zaVestinu ?skill .
              ?ov vbis:prioritet ?p .
            }
            GROUP BY ?job
            ORDER BY DESC(?score)
            """.formatted(studentName, studentName);

        Query query = QueryFactory.create(sparql);
        try (QueryExecution qe = QueryExecutionFactory.create(query, model)) {
            ResultSet rs = qe.execSelect();
            while (rs.hasNext()) {
                QuerySolution sol = rs.next();
                String job = sol.getResource("job").getLocalName();
                int score = sol.getLiteral("score").getInt();
                recommendations.add(job + " (score " + score + ")");
            }
        }

        return recommendations;
    }
}
