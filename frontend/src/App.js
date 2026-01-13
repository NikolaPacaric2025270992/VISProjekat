import { useState, useEffect } from "react";

function App() {
  const [students, setStudents] = useState([]);
  const [recommendations, setRecommendations] = useState([]);
  const [authUser, setAuthUser] = useState(null); // trenutno ulogovani korisnik
  const [username, setUsername] = useState("");
  const [name, setName] = useState("");
  const [role, setRole] = useState("student"); // default student
  const [skills, setSkills] = useState("");
  const [studyProgram, setStudyProgram] = useState("");

  // ================= LOGIN =================
  const handleLogin = async () => {
    try {
      const response = await fetch("http://localhost:8080/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, role }),
      });

      if (!response.ok) throw new Error("User not found");

      const data = await response.json();
      setAuthUser(data);
      alert(`Logged in as ${data.name}`);
    } catch (error) {
      console.error("Login error:", error);
      alert(error.message);
    }
  };

  // ================= REGISTRACIJA =================
  const handleRegister = async () => {
    try {
      const body = {
        username,
        name,
        role,
        skills: skills.split(",").map((s) => s.trim()),
        studyProgram,
      };

      const response = await fetch("http://localhost:8080/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });

      if (!response.ok) throw new Error("Registration failed");

      const data = await response.json();
      setAuthUser(data);
      alert(`Registered and logged in as ${data.name}`);
    } catch (error) {
      console.error("Registration error:", error);
      alert(error.message);
    }
  };

  // ================= FETCH STUDENTS (za agenciju) =================
  const fetchStudents = async () => {
    if (!authUser || authUser.role !== "agency") return;

    try {
      const response = await fetch("http://localhost:8080/students");
      const data = await response.json();
      setStudents(data);
    } catch (error) {
      console.error("Error fetching students:", error);
    }
  };

  // ================= FETCH RECOMMENDATIONS (za studenta) =================
  const fetchRecommendations = async () => {
    if (!authUser || authUser.role !== "student") return;

    try {
      const response = await fetch(
        `http://localhost:8080/recommendations?user=${encodeURIComponent(authUser.username)}`
      );
      const data = await response.json();
      setRecommendations(data);
    } catch (error) {
      console.error("Error fetching recommendations:", error);
    }
  };

  // Auto-fetch po ulogovanju
  useEffect(() => {
    if (authUser?.role === "agency") fetchStudents();
    if (authUser?.role === "student") fetchRecommendations();
  }, [authUser]);

  // ================= RENDER =================
  return (
    <div style={{ padding: "20px", fontFamily: "Arial" }}>
      <h1>VBIS API Frontend</h1>

      {/* LOGIN / REGISTER */}
      {!authUser && (
        <section style={{ marginBottom: "20px" }}>
          <h2>Login / Register</h2>

          <input
            type="text"
            value={username}
            placeholder="Username"
            onChange={(e) => setUsername(e.target.value)}
          />
          <input
            type="text"
            value={name}
            placeholder="Full Name"
            onChange={(e) => setName(e.target.value)}
            style={{ marginLeft: "10px" }}
          />
          <select
            value={role}
            onChange={(e) => setRole(e.target.value)}
            style={{ marginLeft: "10px" }}
          >
            <option value="student">Student</option>
            <option value="agency">Agency</option>
          </select>

          {role === "student" && (
            <>
              <input
                type="text"
                value={skills}
                placeholder="Skills (comma separated)"
                onChange={(e) => setSkills(e.target.value)}
                style={{ marginLeft: "10px" }}
              />
              <input
                type="text"
                value={studyProgram}
                placeholder="Study Program"
                onChange={(e) => setStudyProgram(e.target.value)}
                style={{ marginLeft: "10px" }}
              />
            </>
          )}

          <div style={{ marginTop: "10px" }}>
            <button onClick={handleLogin}>Login</button>
            <button onClick={handleRegister} style={{ marginLeft: "10px" }}>
              Register
            </button>
          </div>
        </section>
      )}

      {/* LOGOVANI KORISNIK */}
      {authUser && (
        <div style={{ marginBottom: "20px" }}>
          <p>
            Logged in as: {authUser.name} ({authUser.role})
          </p>
          {authUser.role === "student" && (
            <div style={{ marginLeft: "20px" }}>
              <p>Skills: {(authUser.skills || []).join(", ")}</p>
              <p>Study Program: {authUser.studyProgram || "-"}</p>
              <p>Looking for job: {authUser.lookingForJob ? "Yes" : "No"}</p>
            </div>
          )}
        </div>
      )}

      {/* AGENCIJA: LISTA STUDENATA */}
      {authUser?.role === "agency" && (
        <section style={{ marginBottom: "20px" }}>
          <h2>Students looking for job</h2>
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
      )}

      {/* STUDENT: PREPORUKE OGLASA */}
      {authUser?.role === "student" && (
        <section>
          <h2>Recommended Jobs for You</h2>
          {recommendations.length === 0 ? (
            <p>No recommendations found.</p>
          ) : (
            <ul>
              {recommendations.map((r, idx) => (
                <li key={idx}>{r}</li>
              ))}
            </ul>
          )}
        </section>
      )}
    </div>
  );
}

export default App;
