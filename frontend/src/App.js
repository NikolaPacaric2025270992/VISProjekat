import { useState, useEffect } from "react";

const API_BASE = "http://localhost:8080";

function App() {
  const [authUser, setAuthUser] = useState(null);
  
  // State za liste
  const [ads, setAds] = useState([]);
  const [recommendations, setRecommendations] = useState([]);
  const [searchingStudents, setSearchingStudents] = useState([]);
  const [rankedKandidati, setRankedKandidati] = useState([]);

  // Forme - Auth
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [realName, setRealName] = useState("");
  const [type, setType] = useState("student");

  // Forme - Student (Ispiti)
  const [predmetId, setPredmetId] = useState("");
  const [ocena, setOcena] = useState(10);

  // Forme - Agencija (Oglas)
  const [jobId, setJobId] = useState("");
  const [jobTitle, setJobTitle] = useState("");
  const [jobSkills, setJobSkills] = useState(""); // npr: Java, React

  // ================= AUTH FUNKCIJE =================
  const handleLogin = async () => {
    const res = await fetch(`${API_BASE}/api/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password })
    });
    if (res.ok) {
      const data = await res.json();
      setAuthUser(data);
    } else alert("Pogrešni podaci");
  };

  const handleRegister = async () => {
    const res = await fetch(`${API_BASE}/api/auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password, type, realName })
    });
    if (res.ok) alert("Registrovan! Možeš se prijaviti.");
    else alert("Greška pri registraciji");
  };

  // ================= STUDENT FUNKCIJE =================
  const updateStatus = async (status) => {
    await fetch(`${API_BASE}/api/profile/status/${authUser.username}?traziPosao=${status}`, { method: "PUT" });
    setAuthUser({ ...authUser, traziPosao: status });
  };

  const addExam = async () => {
    await fetch(`${API_BASE}/api/profile/add-exam/${authUser.username}?predmetId=${predmetId}&ocena=${ocena}`, { method: "POST" });
    alert("Ispit dodat!");
    fetchRecommendations();
  };

  const fetchRecommendations = async () => {
      try {
        const res = await fetch(`${API_BASE}/recommendations/${authUser.username}`);
        const data = await res.json();
        
        // Proveravamo da li je data zaista niz pre nego što ga sačuvamo
        if (Array.isArray(data)) {
          setRecommendations(data);
        } else {
          console.error("Server nije vratio niz:", data);
          setRecommendations([]); // Vraćamo na prazan niz da ne pukne .map()
        }
      } catch (err) {
        console.error("Greška pri dobavljanju preporuka:", err);
        setRecommendations([]);
      }
    };

  const fetchAllAds = async () => {
    try {
      const res = await fetch(`${API_BASE}/api/ads/all`);
      const data = await res.json();

      if (Array.isArray(data)) {
        setAds(data);
      } else {
        console.error("Server za oglase nije vratio niz:", data);
        setAds([]); // Sigurnosna kočnica
      }
    } catch (err) {
      console.error("Mrežna greška pri dobavljanju oglasa:", err);
      setAds([]);
    }
  };

  // ================= AGENCY FUNKCIJE =================
  const handleCreateJob = async () => {
    const vestineList = jobSkills.split(",").map(s => `http://www.vbis.org/ontology#${s.trim()}`);
    await fetch(`${API_BASE}/api/agency/add-job`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ id: jobId, naslov: jobTitle, agencijaUsername: authUser.username, vestine: vestineList })
    });
    alert("Oglas postavljen!");
  };

  const getRanked = async (id) => {
    const res = await fetch(`${API_BASE}/api/agency/rank-students/${id}`);
    const data = await res.json();
    setRankedKandidati(data);
  };

  const fetchSearchingStudents = async () => {
    const res = await fetch(`${API_BASE}/api/agency/students-searching`);
    const data = await res.json();
    setSearchingStudents(data);
  };

  // ================= EXPORT =================
  const exportData = (type) => {
    window.open(`${API_BASE}/export/${type}`);
  };

  // Učitavanje podataka na osnovu uloge
  useEffect(() => {
    if (authUser) {
      if (authUser.type === "student") {
        fetchAllAds();
        fetchRecommendations();
      } else {
        fetchSearchingStudents();
        fetchAllAds(); // Agencija takođe vidi svoje/sve oglase
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [authUser]);

  // Stilovi (zadržani tvoji)
  const box = { background: "#f7f7f7", padding: "20px", borderRadius: "8px", marginBottom: "20px" };
  const inputStyle = { padding: "8px", margin: "5px", width: "200px" };

  return (
    <div style={{ padding: "40px", maxWidth: "900px", margin: "0 auto" }}>
      <h1>VBIS – Semantički sistem za zapošljavanje</h1>

      {!authUser ? (
        <div style={box}>
          <h2>Login / Registracija</h2>
          <input style={inputStyle} placeholder="Username" onChange={e => setUsername(e.target.value)} />
          <input style={inputStyle} type="password" placeholder="Password" onChange={e => setPassword(e.target.value)} />
          <input style={inputStyle} placeholder="Ime i Prezime" onChange={e => setRealName(e.target.value)} />
          <select style={inputStyle} onChange={e => setType(e.target.value)}>
            <option value="student">Student</option>
            <option value="agency">Agencija</option>
          </select>
          <button onClick={handleLogin}>Login</button>
          <button onClick={handleRegister}>Register</button>
        </div>
      ) : (
        <div>
          <button onClick={() => setAuthUser(null)}>Logout ({authUser.username})</button>
          <hr />

          {authUser.type === "student" ? (
            <div>
              <div style={box}>
                <h3>Moj Profil (Status: {authUser.traziPosao ? "Tražim posao" : "Ne tražim posao"})</h3>
                <button onClick={() => updateStatus(!authUser.traziPosao)}>Promeni status</button>
                
                <h4>Dodaj položen ispit</h4>
                <input style={inputStyle} placeholder="ID Predmeta (npr. Java1)" onChange={e => setPredmetId(e.target.value)} />
                <input style={inputStyle} type="number" placeholder="Ocena" onChange={e => setOcena(e.target.value)} />
                <button onClick={addExam}>Sačuvaj ispit</button>
              </div>

              <div style={box}>
                <h3>Preporučeni oglasi (na osnovu tvojih ispita)</h3>
                <ul>
                  {Array.isArray(recommendations) && recommendations.length > 0 
                    ? recommendations.map((r, i) => <li key={i}><strong>{r}</strong></li>)
                    : <li>Trenutno nema preporuka.</li>
                  }
                </ul>
              </div>
            </div>
          ) : (
            <div>
              <div style={box}>
                <h3>Postavi novi oglas</h3>
                <input style={inputStyle} placeholder="ID Oglasa" onChange={e => setJobId(e.target.value)} />
                <input style={inputStyle} placeholder="Naslov oglasa" onChange={e => setJobTitle(e.target.value)} />
                <input style={inputStyle} placeholder="Veštine (npr. React, Java)" onChange={e => setJobSkills(e.target.value)} />
                <button onClick={handleCreateJob}>Objavi</button>
              </div>

              <div style={box}>
                <h3>Studenti koji traže posao</h3>
                <ul>
                  {Array.isArray(searchingStudents) && searchingStudents.length > 0 
                    ? searchingStudents.map((s, i) => <li key={i}>{s.name} ({s.username})</li>)
                    : <li>Trenutno nema studenata koji traže zaposlenje.</li>
                  }
                </ul>
              </div>
            </div>
          )}

          <div style={box}>
            <h3>Svi oglasi u sistemu</h3>
            {/* DODATO: Sigurnosna provera pre map funkcije */}
            {Array.isArray(ads) && ads.map((a, i) => (
              <div key={i} style={{ borderBottom: "1px solid #ccc", padding: "10px" }}>
                <span>{a.naslov}</span>
                {authUser.type === "agency" && (
                  <button style={{ marginLeft: "10px" }} onClick={() => getRanked(a.id)}>Prikaži rang listu kandidate</button>
                )}
              </div>
            ))}
            
            {/* DODATO: Isto uradi i za rangirane kandidate da ne pukne kasnije */}
            {Array.isArray(rankedKandidati) && rankedKandidati.length > 0 && (
              <div style={{ background: "#e8f5e9", padding: "10px", marginTop: "10px" }}>
                <h4>Idealni kandidati za oglas:</h4>
                <ol>{rankedKandidati.map((k, i) => <li key={i}>{k.name}</li>)}</ol>
              </div>
            )}
          </div>

          <div style={{ marginTop: "20px" }}>
            <button onClick={() => exportData("ontology")}>Izvezi RDF (Ontologija)</button>
            <button onClick={() => exportData("users")}>Izvezi JSON (Korisnici)</button>
          </div>
        </div>
      )}
    </div>
  );
}

export default App;