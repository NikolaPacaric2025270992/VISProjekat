import { useState, useEffect } from "react";

function App() {
  const [students, setStudents] = useState([]);
  const [user, setUser] = useState("");
  const [recommendations, setRecommendations] = useState([]);

  // Funkcija za učitavanje svih studenata sa backend-a
  const fetchStudents = async () => {
    try {
      const response = await fetch("http://localhost:8080/students");
      const data = await response.json();
      setStudents(data);
    } catch (error) {
      console.error("Error fetching students:", error);
    }
  };

  // Funkcija za preporuke po korisniku
  const fetchRecommendations = async () => {
    try {
      const response = await fetch(
        `http://localhost:8080/recommendations?user=${encodeURIComponent(user)}`
      );
      const data = await response.json();
      setRecommendations(data);
    } catch (error) {
      console.error("Error fetching recommendations:", error);
    }
  };

  // Učitaj studente pri prvom renderu
  useEffect(() => {
    fetchStudents();
  }, []);

  return (
    <div style={{ padding: "20px", fontFamily: "Arial" }}>
      <h1>VBIS API Frontend</h1>

      <section style={{ marginBottom: "20px" }}>
        <h2>Students</h2>
        {students.length === 0 ? (
          <p>No students found.</p>
        ) : (
          <ul>
            {students.map((s, idx) => (
              <li key={idx}>{s}</li>
            ))}
          </ul>
        )}
      </section>

      <section>
        <h2>Recommendations</h2>
        <input
          type="text"
          value={user}
          placeholder="Enter username"
          onChange={(e) => setUser(e.target.value)}
        />
        <button onClick={fetchRecommendations} style={{ marginLeft: "10px" }}>
          Get Recommendations
        </button>

        {recommendations.length > 0 && (
          <ul style={{ marginTop: "10px" }}>
            {recommendations.map((r, idx) => (
              <li key={idx}>{r}</li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}

export default App;
