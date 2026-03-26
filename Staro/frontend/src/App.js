import { useState, useEffect } from "react";

const API_BASE = "http://localhost:8080";

function App() {
  // --- AUTH STATE ---
  const [authUser, setAuthUser] = useState(null);
  const [isRegisterMode, setIsRegisterMode] = useState(false);
  const [authForm, setAuthForm] = useState({ username: "", password: "", realName: "", type: "student" });

  // --- DATA LISTS ---
  const [ads, setAds] = useState([]);
  const [recommendations, setRecommendations] = useState([]);
  const [searchingStudents, setSearchingStudents] = useState([]);
  const [rankedKandidati, setRankedKandidati] = useState([]);

  // --- SPECIFIC FORMS STATE ---
  const [examForm, setExamForm] = useState({ predmetId: "", ocena: 10, nivo: 1 });
  const [jobForm, setJobForm] = useState({ id: "", title: "", skills: "", level: 1, priority: 1 });

  // --- USER UPDATE --- 
  const [isEditMode, setIsEditMode] = useState(false);
  const [newName, setNewName] = useState("");
  const [newPassword, setNewPassword] = useState("");

  const [editNameMode, setEditNameMode] = useState(false);
  const [editPassMode, setEditPassMode] = useState(false);
  const [tempName, setTempName] = useState("");
  const [tempPass, setTempPass] = useState("");

  // ================= POMOĆNE FUNKCIJE =================

  const resetAuthForm = () => setAuthForm({ username: "", password: "", realName: "", type: "student" });

  // ================= AUTH FUNKCIJE =================

  const handleLogin = async () => {
    try {
      const res = await fetch(`${API_BASE}/api/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username: authForm.username, password: authForm.password })
      });
      if (res.ok) {
        const data = await res.json();
        setAuthUser(data);
        resetAuthForm();
      } else alert("Pogrešni podaci");
    } catch (err) { alert("Server nije dostupan"); }
  };

  const handleRegister = async () => {
    const res = await fetch(`${API_BASE}/api/auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(authForm)
    });

    if (res.ok) {
      alert("Registrovan! Možeš se prijaviti.");
      setIsRegisterMode(false);
      resetAuthForm();
    } else alert("Greška pri registraciji");
  };

  // ================= STUDENT FUNKCIJE =================

  const updateStatus = async (status) => {
    await fetch(`${API_BASE}/api/profile/status/${authUser.username}?traziPosao=${status}`, { method: "PUT" });
    setAuthUser({ ...authUser, traziPosao: status });
  };

  const addExam = async () => {
    const { predmetId, ocena, nivo } = examForm;
    if (!predmetId) return alert("Unesite ID predmeta");

    await fetch(`${API_BASE}/api/profile/add-exam/${authUser.username}?predmetId=${predmetId}&ocena=${ocena}&nivo=${nivo}`, { 
      method: "POST" 
    });
    alert("Ispit dodat!");
    setExamForm({ predmetId: "", ocena: 10, nivo: 1 }); // Reset forme
    fetchRecommendations();
  };

  const fetchRecommendations = async () => {
    try {
      const res = await fetch(`${API_BASE}/recommendations/${authUser.username}`);
      const data = await res.json();
      setRecommendations(Array.isArray(data) ? data : []);
    } catch (err) { setRecommendations([]); }
  };

  // ================= AGENCY FUNKCIJE =================

  const handleCreateJob = async () => {
    const vestineObjekti = jobForm.skills.split(",").map(s => ({
      uri: `http://www.vbis.org/ontology#${s.trim()}`,
      nivo: parseInt(jobForm.level),
      prioritet: parseInt(jobForm.priority)
    }));
    
    const res = await fetch(`${API_BASE}/api/agency/add-job`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ 
        id: jobForm.id, 
        naslov: jobForm.title, 
        agencijaUsername: authUser.username, 
        vestine: vestineObjekti 
      })
    });

    if (res.ok) {
      alert("Oglas uspešno postavljen!");
      fetchAllAds(); 
      setJobForm({ id: "", title: "", skills: "", level: 1, priority: 1 }); // Reset forme
    } else alert("Greška pri postavljanju oglasa.");
  };

  const getRanked = async (id) => {
    const res = await fetch(`${API_BASE}/api/agency/rank-students/${id}`);
    const data = await res.json();
    setRankedKandidati(data);
  };

  // ================= ZAJEDNIČKE FUNKCIJE =================

  const fetchAllAds = async () => {
    try {
      const res = await fetch(`${API_BASE}/api/ads/all`);
      const data = await res.json();
      setAds(Array.isArray(data) ? data : []);
    } catch (err) { setAds([]); }
  };

  const fetchSearchingStudents = async () => {
    try {
      const res = await fetch(`${API_BASE}/api/agency/students-searching`);
      const data = await res.json();
      setSearchingStudents(Array.isArray(data) ? data : []);
    } catch(e) { setSearchingStudents([]); }
  };

  const exportData = (type) => window.open(`${API_BASE}/export/${type}`);

  const handleUpdateName = async () => {
    const res = await fetch(`${API_BASE}/api/profile/update-name/${authUser.username}?newName=${encodeURIComponent(tempName)}`, {
      method: "PUT"
    });
    if (res.ok) {
      alert("Ime izmenjeno!");
      setAuthUser({ ...authUser, realName: tempName });
      setEditNameMode(false);
    }
  };

  const handleUpdatePassword = async () => {
    const res = await fetch(`${API_BASE}/api/profile/update-password/${authUser.username}?newPassword=${encodeURIComponent(tempPass)}`, {
      method: "PUT"
    });
    if (res.ok) {
      alert("Lozinka izmenjena!");
      setEditPassMode(false);
      setTempPass("");
    }
  };

  const handleLogout = () => {
    setAuthUser(null);
    setRankedKandidati([]);      // Brišemo rang listu
    setRecommendations([]);      // Brišemo preporuke za studenta
    setSearchingStudents([]);   // Brišemo listu studenata za agenciju
    setIsRegisterMode(false);    // Vraćamo na login ekran
  };

  // Osvežavanje podataka pri loginu
  useEffect(() => {
    if (authUser) {
      fetchAllAds();
      if (authUser.type === "student") fetchRecommendations();
      else fetchSearchingStudents();
    }
  }, [authUser]);

  // --- STYLES ---
  const box = { background: "#f7f7f7", padding: "20px", borderRadius: "8px", marginBottom: "20px", border: "1px solid #ddd" };
  const inputStyle = { padding: "8px", margin: "5px", width: "220px", borderRadius: "4px", border: "1px solid #ccc" };

  return (
    <div style={{ padding: "40px", maxWidth: "900px", margin: "0 auto", fontFamily: "sans-serif" }}>
      <h1 style={{ textAlign: "center", color: "#2c3e50" }}>VBIS Semantičko zapošljavanje</h1>

      {!authUser ? (
        <div style={box}>
          <h2>{isRegisterMode ? "Registracija" : "Prijava"}</h2>
          <input 
            style={inputStyle} 
            placeholder="Korisničko ime" 
            value={authForm.username}
            onChange={e => setAuthForm({...authForm, username: e.target.value})} 
          />
          <input 
            style={inputStyle} 
            type="password" 
            placeholder="Lozinka" 
            value={authForm.password}
            onChange={e => setAuthForm({...authForm, password: e.target.value})} 
          />

          {isRegisterMode && (
            <>
              <input 
                style={inputStyle} 
                placeholder="Ime i Prezime" 
                value={authForm.realName}
                onChange={e => setAuthForm({...authForm, realName: e.target.value})} 
              />
              <select 
                style={inputStyle} 
                value={authForm.type}
                onChange={e => setAuthForm({...authForm, type: e.target.value})}
              >
                <option value="student">Student</option>
                <option value="agency">Agencija</option>
              </select>
            </>
          )}

          <div style={{ marginTop: "15px" }}>
            <button 
              onClick={isRegisterMode ? handleRegister : handleLogin} 
              style={{ padding: "10px 20px", cursor: "pointer", background: "#2ecc71", color: "white", border: "none", borderRadius: "4px" }}
            >
              {isRegisterMode ? "Završi registraciju" : "Uloguj se"}
            </button>
            <p onClick={() => setIsRegisterMode(!isRegisterMode)} style={{ color: "#3498db", cursor: "pointer", marginTop: "10px", fontSize: "14px" }}>
              {isRegisterMode ? "Već imate nalog? Prijavite se" : "Nemate nalog? Registrujte se"}
            </p>
          </div>
        </div>
      ) : (
        <div>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
            <span>Ulogovani ste kao: <b>{authUser.username}</b> ({authUser.type})</span>
            <button 
              onClick={handleLogout} 
              style={{ background: "#e74c3c", color: "white", border: "none", padding: "5px 10px", borderRadius: "4px", cursor: "pointer" }}
            >
              Logout ({authUser.username})
            </button>
          </div>
          <hr />

          {/* --- STUDENT PANEL --- */}
          {authUser.type === "student" && (
            <div>
              <div style={box}>
                <h3>Moj Profil</h3>
                
                {/* SEKCIJA ZA IME */}
                <div style={{ marginBottom: "15px", borderBottom: "1px solid #ddd", paddingBottom: "10px" }}>
                  <label>Ime i prezime:</label> <br />
                  {!editNameMode ? (
                    <>
                      <b>{authUser.realName}</b> 
                      <button onClick={() => { setEditNameMode(true); setTempName(authUser.realName); }} style={smallBtn}>Izmeni</button>
                    </>
                  ) : (
                    <>
                      <input style={inputStyle} value={tempName} onChange={e => setTempName(e.target.value)} />
                      <button onClick={handleUpdateName} style={saveBtn}>Sačuvaj</button>
                      <button onClick={() => setEditNameMode(false)} style={cancelBtn}>Otkaži</button>
                    </>
                  )}
                </div>

                {/* SEKCIJA ZA LOZINKU */}
                <div style={{ marginBottom: "15px" }}>
                  <label>Lozinka:</label> <br />
                  {!editPassMode ? (
                    <>
                      <b>********</b> 
                      <button onClick={() => setEditPassMode(true)} style={smallBtn}>Promeni lozinku</button>
                    </>
                  ) : (
                    <>
                      <input type="password" style={inputStyle} placeholder="Nova lozinka" value={tempPass} onChange={e => setTempPass(e.target.value)} />
                      <button onClick={handleUpdatePassword} style={saveBtn}>Sačuvaj</button>
                      <button onClick={() => setEditPassMode(false)} style={cancelBtn}>Otkaži</button>
                    </>
                  )}
                </div>

                <hr />
                <p>Status: <b>{authUser.traziPosao ? "Tražim posao" : "Ne tražim posao"}</b></p>
                <button onClick={() => updateStatus(!authUser.traziPosao)}>Promeni status</button>
              </div>
              
              <div style={box}>
                <h3>✨ Preporučeni oglasi za tebe</h3>
                <ul>
                  {recommendations.length > 0 ? recommendations.map((r, i) => <li key={i}><b>{r}</b></li>) : <li>Nema preporuka.</li>}
                </ul>
              </div>
            </div>
          )}

          {/* --- AGENCY PANEL --- */}
          {authUser.type === "agency" && (
            <div>
              <div style={box}>
                <h3>Postavi novi oglas</h3>
                <input style={inputStyle} placeholder="ID Oglasa" value={jobForm.id} onChange={e => setJobForm({...jobForm, id: e.target.value})} />
                <input style={inputStyle} placeholder="Naslov oglasa" value={jobForm.title} onChange={e => setJobForm({...jobForm, title: e.target.value})} />
                <input style={inputStyle} placeholder="Veštine (npr. Java, SQL)" value={jobForm.skills} onChange={e => setJobForm({...jobForm, skills: e.target.value})} />
                <div style={{ margin: "10px 5px" }}>
                  <label>Traženi nivo: </label>
                  <input type="number" style={{ width: "50px" }} value={jobForm.level} onChange={e => setJobForm({...jobForm, level: e.target.value})} />
                  <label> Prioritet: </label>
                  <input type="number" style={{ width: "50px" }} value={jobForm.priority} onChange={e => setJobForm({...jobForm, priority: e.target.value})} />
                </div>
                <button onClick={handleCreateJob} style={{ background: "#27ae60", color: "white", padding: "10px", border: "none", borderRadius: "4px", width: "100%" }}>Objavi oglas</button>
              </div>

              <div style={box}>
                <h3>Korisnici koji traže posao</h3>
                <ul>
                  {searchingStudents.map((s, i) => <li key={i}>{s.name} (<b>{s.username}</b>)</li>)}
                </ul>
              </div>
            </div>
          )}

          {/* --- ZAJEDNIČKI PRIKAZ OGLASA --- */}
          <div style={box}>
            <h3>Svi oglasi u sistemu</h3>
            {ads.map((a, i) => (
              <div key={i} style={{ borderBottom: "1px solid #ccc", padding: "10px", display: "flex", justifyContent: "space-between" }}>
                <span>{a.naslov}</span>
                {authUser.type === "agency" && (
                  <button onClick={() => getRanked(a.id)}>Rangiraj kandidate</button>
                )}
              </div>
            ))}
            
            {/* Dodajemo proveru: samo ako je agencija I ako ima rezultata */}
            {authUser?.type === "agency" && rankedKandidati.length > 0 && (
              <div style={{ background: "#e8f5e9", padding: "15px", marginTop: "15px", borderRadius: "5px", border: "1px solid #c8e6c9" }}>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                  <h4 style={{ margin: 0 }}>Top kandidati za oglas:</h4>
                  <button 
                    onClick={() => setRankedKandidati([])} 
                    style={{ cursor: "pointer", background: "none", border: "none", color: "#666" }}
                  >
                    zatvori ✖
                  </button>
                </div>
                <ol>
                  {rankedKandidati.map((k, i) => (
                    <li key={i}>
                      <b>{k.name}</b> - Skor: <span style={{ color: "green", fontWeight: "bold" }}>{k.score}</span>
                    </li>
                  ))}
                </ol>
              </div>
            )}
          </div>

          <div style={{ textAlign: "center" }}>
            <button onClick={() => exportData("ontology")} style={exportBtn}>Export RDF</button>
            <button onClick={() => exportData("users")} style={exportBtn}>Export JSON</button>
          </div>
        </div>
      )}
    </div>
  );
}
const smallBtn = { marginLeft: "10px", padding: "2px 8px", fontSize: "12px", cursor: "pointer" };
const saveBtn = { background: "#27ae60", color: "white", border: "none", padding: "5px 10px", borderRadius: "4px", cursor: "pointer", marginRight: "5px" };
const cancelBtn = { background: "#95a5a6", color: "white", border: "none", padding: "5px 10px", borderRadius: "4px", cursor: "pointer" };
const exportBtn = { margin: "5px", padding: "10px", cursor: "pointer", background: "#95a5a6", color: "white", border: "none", borderRadius: "4px" };

export default App;