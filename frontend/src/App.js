import { useState, useEffect } from "react";

function App() {
  const [students, setStudents] = useState([]);
  const [jobs, setJobs] = useState([]);
  const [recommendations, setRecommendations] = useState([]);
  const [authUser, setAuthUser] = useState(null);

  const [username, setUsername] = useState("");
  const [name, setName] = useState("");
  const [role, setRole] = useState("student");
  const [skills, setSkills] = useState("");
  const [studyProgram, setStudyProgram] = useState("");

  const [jobName, setJobName] = useState("");
  const [jobSkills, setJobSkills] = useState("");

  const inputStyle = {
    padding: "8px",
    margin: "5px",
    width: "250px"
  };

  const box = {
    background: "#f7f7f7",
    padding: "20px",
    borderRadius: "8px",
    marginBottom: "20px",
    maxWidth: "600px"
  };

  const btn = {
    padding: "8px 15px",
    margin: "5px",
    cursor: "pointer"
  };

  // ================= LOGIN =================
  const handleLogin = async () => {
    const res = await fetch("http://localhost:8080/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username })
    });
    const data = await res.json();
    if (!data.error) setAuthUser(data);
    else alert(data.error);
  };

  // ================= REGISTER =================
  const handleRegister = async () => {
    const res = await fetch("http://localhost:8080/auth/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        username,
        name,
        role,
        skills: skills.split(",").map(s => s.trim()).filter(s => s),
        studyProgram
      })
    });
    const data = await res.json();
    if (!data.error) setAuthUser(data);
    else alert(data.error);
  };

  // ================= CREATE JOB =================
  const handleCreateJob = async () => {
    const skillsArray = jobSkills.split(",").map(s => {
      const [skill, priority] = s.split(":");
      return { skill: skill.trim(), priority: parseInt(priority) || 1 };
    }).filter(s => s.skill);

    const res = await fetch("http://localhost:8080/jobs", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        jobName,
        agencyName: authUser.name,
        skills: skillsArray
      })
    });
    const data = await res.json();
    if (!data.error) {
      setJobName("");
      setJobSkills("");
      fetchJobs();
    } else {
      alert(data.error);
    }
  };

  // ================= FETCH JOBS =================
  const fetchJobs = async () => {
    const res = await fetch("http://localhost:8080/jobs/list");
    const data = await res.json();
    setJobs(Array.isArray(data) ? data : []);
  };

  // ================= FETCH RECOMMENDATIONS =================
  const fetchRecommendations = async () => {
    if (!authUser) return;
    const res = await fetch(`http://localhost:8080/jobs/recommendations/${authUser.username}`);
    const data = await res.json();
    setRecommendations(Array.isArray(data) ? data : []);
  };

  useEffect(() => {
    if (!authUser) return;
    if (authUser.role === "agency") fetchJobs();
    if (authUser.role === "student") fetchRecommendations();
  }, [authUser]);

  return (
    <div style={{ padding: "30px", fontFamily: "Arial" }}>
      <h1>VBIS – Job Recommendation System</h1>

      {!authUser && (
        <div style={box}>
          <h2>Login / Register</h2>
          <input
            style={inputStyle}
            placeholder="Username"
            value={username}
            onChange={e => setUsername(e.target.value)}
          />
          <input
            style={inputStyle}
            placeholder="Full name"
            value={name}
            onChange={e => setName(e.target.value)}
          />
          <br />
          <select style={inputStyle} value={role} onChange={e => setRole(e.target.value)}>
            <option value="student">Student</option>
            <option value="agency">Agency</option>
          </select>
          <br />
          {role === "student" && (
            <>
              <input
                style={inputStyle}
                placeholder="Skills (Java,React)"
                value={skills}
                onChange={e => setSkills(e.target.value)}
              />
              <input
                style={inputStyle}
                placeholder="Study program"
                value={studyProgram}
                onChange={e => setStudyProgram(e.target.value)}
              />
            </>
          )}
          <br />
          <button style={btn} onClick={handleLogin}>Login</button>
          <button style={btn} onClick={handleRegister}>Register</button>
        </div>
      )}

      {authUser?.role === "agency" && (
        <div style={box}>
          <h2>Create job</h2>
          <input
            style={inputStyle}
            placeholder="Job name"
            value={jobName}
            onChange={e => setJobName(e.target.value)}
          />
          <input
            style={inputStyle}
            placeholder="Java:5,React:3"
            value={jobSkills}
            onChange={e => setJobSkills(e.target.value)}
          />
          <br />
          <button style={btn} onClick={handleCreateJob}>Add Job</button>

          <h3>All jobs</h3>
          <ul>
            {jobs.map((j, i) => (
              <li key={i}>
                {j.jobName} (Agency: {j.agencyName})<br />
                Skills: {j.skills.map(s => `${s.skill}:${s.priority}`).join(", ")}
              </li>
            ))}
          </ul>
        </div>
      )}

      {authUser?.role === "student" && (
        <div style={box}>
          <h2>Recommended jobs</h2>
          <ul>
            {recommendations.map((r, i) => <li key={i}>{r}</li>)}
          </ul>
        </div>
      )}
    </div>
  );
}

export default App;
