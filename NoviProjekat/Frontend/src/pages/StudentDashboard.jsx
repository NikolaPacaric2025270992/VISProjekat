import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

function StudentDashboard() {
    const navigate = useNavigate();
    const [student, setStudent] = useState(null);
    const [predmeti, setPredmeti] = useState([]);
    
    // Stanja za Search i Ispite
    const [searchQuery, setSearchQuery] = useState('');
    const [filtriraniPredmeti, setFiltriraniPredmeti] = useState([]);
    const [prikaziDropdown, setPrikaziDropdown] = useState(false);
    const [izabranPredmet, setIzabranPredmet] = useState(null); 
    const [ocena, setOcena] = useState(6);
    const [polaganja, setPolaganja] = useState([]);
    const [preporuceniOglasi, setPreporuceniOglasi] = useState([]);

    // NOVO: Stanja za Podešavanja profila
    const [prikaziPodesavanja, setPrikaziPodesavanja] = useState(false);
    const [editIme, setEditIme] = useState('');
    const [editPrezime, setEditPrezime] = useState('');
    const [editTraziZaposlenje, setEditTraziZaposlenje] = useState(false);

    useEffect(() => {
        const ulogovanKorisnik = localStorage.getItem('user');
        const rola = localStorage.getItem('role');
        if (!ulogovanKorisnik || rola !== 'student') { navigate('/prijava'); return; }

        const podaciStudenta = JSON.parse(ulogovanKorisnik);
        setStudent(podaciStudenta);

        axios.get('http://localhost:8080/api/predmeti')
            .then(res => setPredmeti(res.data))
            .catch(err => console.error("Greška:", err));

        axios.get(`http://localhost:8080/api/polaganja/student/${podaciStudenta.id}`)
            .then(res => setPolaganja(res.data))
            .catch(err => console.error("Greška pri učitavanju polaganja:", err));

        axios.get(`http://localhost:8080/api/studenti/${podaciStudenta.email}/preporuke`)
            .then(res => setPreporuceniOglasi(res.data))
            .catch(err => console.error("Greška pri učitavanju preporuka:", err));

    }, [navigate]);

    const handleOdjava = () => {
        localStorage.removeItem('user');
        localStorage.removeItem('role');
        navigate('/prijava');
    };

    // NOVO: Funkcija za otvaranje podešavanja i popunjavanje forme
    const otvoriPodesavanja = () => {
        setEditIme(student.ime);
        setEditPrezime(student.prezime);
        setEditTraziZaposlenje(student.traziZaposlenje);
        setPrikaziPodesavanja(true);
    };

    // NOVO: UPDATE Funkcija (Šalje PUT zahtev)
    const handleAzurirajProfil = async (e) => {
        e.preventDefault();
        const azuriranStudent = { ...student, ime: editIme, prezime: editPrezime, traziZaposlenje: editTraziZaposlenje };
        
        try {
            await axios.put('http://localhost:8080/api/studenti/update', azuriranStudent);
            setStudent(azuriranStudent);
            localStorage.setItem('user', JSON.stringify(azuriranStudent)); // Čuvamo promenu i u browseru
            alert("Profil uspešno ažuriran!");
            setPrikaziPodesavanja(false); // Vraćamo se na početni ekran
        } catch (error) {
            alert("Greška pri ažuriranju profila.");
        }
    };

    // NOVO: DELETE Funkcija (Šalje DELETE zahtev)
    const handleObrisiNalog = async () => {
        const potvrda = window.confirm("Da li ste sigurni da želite trajno da obrišete nalog? Svi vaši podaci i položeni ispiti će biti obrisani.");
        if (potvrda) {
            try {
                await axios.delete(`http://localhost:8080/api/studenti/obrisi/${student.id}`);
                alert("Nalog je uspešno obrisan.");
                handleOdjava(); // Odjavljujemo korisnika i prebacujemo ga na login ekran
            } catch (error) {
                alert("Greška pri brisanju naloga.");
            }
        }
    };

    // --- Funkcije za ispite ostaju iste ---
    const handleSearchChange = (e) => {
        const query = e.target.value;
        setSearchQuery(query);
        if (query.length > 1) {
            const filtrirano = predmeti.filter(p => p.nazivPredmeta.toLowerCase().includes(query.toLowerCase()));
            setFiltriraniPredmeti(filtrirano);
            setPrikaziDropdown(true);
        } else { setPrikaziDropdown(false); }
    };

    const izaberiPredmet = (predmet) => {
        setIzabranPredmet(predmet);
        setSearchQuery(predmet.nazivPredmeta);
        setPrikaziDropdown(false);
    };

    const handleDodajPolaganje = async (e) => {
        e.preventDefault();
        if(!izabranPredmet) { alert("Prvo izaberi predmet iz liste!"); return; }
        const novoPolaganje = { id: `polaganje_${Date.now()}`, studentId: student.id, predmetId: izabranPredmet.id, ocena: parseInt(ocena) };
        try {
            await axios.post('http://localhost:8080/api/polaganja/dodaj', novoPolaganje);
            alert("Ispit uspešno evidentiran!");
            setPolaganja([...polaganja, { ...novoPolaganje, nazivPredmeta: izabranPredmet.nazivPredmeta }]);
            setSearchQuery(''); setIzabranPredmet(null);
            
            // Osvežavamo preporuke nakon unosa ispita
            const resPreporuke = await axios.get(`http://localhost:8080/api/studenti/${student.email}/preporuke`);
            setPreporuceniOglasi(resPreporuke.data);
        } catch (error) { alert("Greška pri čuvanju."); }
    };

    if (!student) return <div className="text-center mt-5">Učitavanje...</div>;

    return (
        <div className="container mt-4">
            {/* ZAGLAVLJE */}
            <div className="d-flex justify-content-between align-items-center mb-4 pb-2 border-bottom">
                <h2 className="text-success">Profil: {student.ime} {student.prezime}</h2>
                <div>
                    <span className="me-3 fw-bold text-muted">Status: {student.traziZaposlenje ? 'Traži posao 🟢' : 'Nije aktivan 🔴'}</span>
                    
                    {/* NOVO: Dugmići za navigaciju u zaglavlju */}
                    {!prikaziPodesavanja ? (
                        <button onClick={otvoriPodesavanja} className="btn btn-outline-secondary btn-sm me-2">⚙️ Podešavanja</button>
                    ) : (
                        <button onClick={() => setPrikaziPodesavanja(false)} className="btn btn-outline-secondary btn-sm me-2">🔙 Nazad na Dashboard</button>
                    )}
                    
                    <button onClick={handleOdjava} className="btn btn-outline-danger btn-sm">Odjavi se</button>
                </div>
            </div>
            
            {/* USLOVNO RENDEROVANJE: Podešavanja ILI Dashboard */}
            {prikaziPodesavanja ? (
                /* PRIKAZ: PODEŠAVANJA PROFILA */
                <div className="row justify-content-center animate__animated animate__fadeIn">
                    <div className="col-md-6">
                        <div className="card shadow-sm border-0">
                            <div className="card-header bg-secondary text-white py-3">
                                <h5 className="mb-0">Podešavanja profila</h5>
                            </div>
                            <div className="card-body">
                                <form onSubmit={handleAzurirajProfil}>
                                    <div className="mb-3">
                                        <label className="form-label fw-bold">Ime:</label>
                                        <input type="text" className="form-control" value={editIme} onChange={(e) => setEditIme(e.target.value)} required />
                                    </div>
                                    <div className="mb-3">
                                        <label className="form-label fw-bold">Prezime:</label>
                                        <input type="text" className="form-control" value={editPrezime} onChange={(e) => setEditPrezime(e.target.value)} required />
                                    </div>
                                    <div className="mb-4 form-check form-switch">
                                        <input className="form-check-input" type="checkbox" id="traziPosaoSwitch" checked={editTraziZaposlenje} onChange={(e) => setEditTraziZaposlenje(e.target.checked)} />
                                        <label className="form-check-label fw-bold text-success" htmlFor="traziPosaoSwitch">
                                            Aktivno tražim posao i želim preporuke poslodavaca
                                        </label>
                                    </div>
                                    <button type="submit" className="btn btn-primary w-100 mb-3">Sačuvaj izmene</button>
                                </form>
                                <hr />
                                <div className="text-center mt-4">
                                    <p className="text-muted small">Ukoliko obrišete nalog, svi vaši podaci biće trajno uklonjeni iz sistema.</p>
                                    <button onClick={handleObrisiNalog} className="btn btn-danger btn-sm">🗑️ Obriši nalog trajno</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            ) : (
                /* PRIKAZ: STANDARDNI DASHBOARD (Ispiti i Preporuke) */
                <>
                    <div className="row animate__animated animate__fadeIn">
                        {/* Leva kolona: Forma za ispite */}
                        <div className="col-md-5">
                            <div className="card shadow-sm border-0">
                                <div className="card-header bg-success text-white py-3">
                                    <h5 className="mb-0">Evidentiraj novi ispit</h5>
                                </div>
                                <div className="card-body">
                                    <form onSubmit={handleDodajPolaganje} className="position-relative">
                                        <label className="form-label fw-bold">Pretraži predmet:</label>
                                        <input type="text" className="form-control" placeholder="Kucaj naziv predmeta..." value={searchQuery} onChange={handleSearchChange} />
                                        {prikaziDropdown && filtriraniPredmeti.length > 0 && (
                                            <ul className="list-group position-absolute w-100 shadow-lg" style={{ zIndex: 1000, maxHeight: '200px', overflowY: 'auto' }}>
                                                {filtriraniPredmeti.map(p => (
                                                    <li key={p.id} className="list-group-item list-group-item-action" onClick={() => izaberiPredmet(p)} style={{ cursor: 'pointer' }}>
                                                        {p.nazivPredmeta} <small className="text-muted">({p.ects} ESPB)</small>
                                                    </li>
                                                ))}
                                            </ul>
                                        )}
                                        <div className="mt-3 mb-3">
                                            <label className="form-label fw-bold">Ocena (6-10):</label>
                                            <input type="number" min="6" max="10" className="form-control" value={ocena} onChange={(e) => setOcena(e.target.value)} />
                                        </div>
                                        <button type="submit" className="btn btn-success w-100 py-2">Sačuvaj polaganje</button>
                                    </form>
                                </div>
                            </div>
                        </div>

                        {/* Desna kolona: Tabela ispita */}
                        <div className="col-md-7">
                            <h4 className="mb-3 text-secondary">Moji položeni ispiti</h4>
                            <div className="table-responsive shadow-sm">
                                <table className="table table-hover bg-white border">
                                    <thead className="table-success text-white">
                                        <tr>
                                            <th>Predmet</th>
                                            <th className="text-center">Ocena</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {polaganja.length === 0 ? (
                                            <tr><td colSpan="2" className="text-center text-muted">Nema evidentiranih ispita.</td></tr>
                                        ) : (
                                            polaganja.map((p, index) => (
                                                <tr key={p.id || index}>
                                                    <td>{p.nazivPredmeta || p.predmetId}</td>
                                                    <td className="text-center"><span className="badge bg-success rounded-pill">{p.ocena}</span></td>
                                                </tr>
                                            ))
                                        )}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>

                    {/* Donji red: Preporučeni poslovi */}
                    <div className="row mt-5 animate__animated animate__fadeInUp">
                        <div className="col-12">
                            <h4 className="mb-3 text-primary border-bottom pb-2">Preporučeni poslovi za tebe</h4>
                            {preporuceniOglasi.length === 0 ? (
                                <div className="alert alert-light text-center border shadow-sm text-muted py-4">
                                    Trenutno nemamo poslova koji odgovaraju tvom profilu. 
                                    <br/>Položi još neki ispit kako bi otključao nove veštine!
                                </div>
                            ) : (
                                <div className="row">
                                    {preporuceniOglasi.map((oglas, idx) => (
                                        <div key={oglas.id || idx} className="col-md-6 col-lg-4 mb-4">
                                            <div className="card h-100 shadow-sm border-start border-primary border-4">
                                                <div className="card-body">
                                                    <div className="d-flex justify-content-between align-items-start mb-2">
                                                        <h5 className="card-title text-primary mb-0">{oglas.naslov}</h5>
                                                        <span className="badge bg-warning text-dark fs-6" title="Tvoj skor za ovaj oglas">
                                                            Bodovi: {oglas.bodovi || oglas.ukupniBodovi || 'N/A'}
                                                        </span>
                                                    </div>
                                                    <p className="text-muted small mb-4">ID Oglasa: {oglas.oglasID || oglas.id}</p>
                                                    <button className="btn btn-outline-primary w-100 fw-bold mt-auto">
                                                        Prijavi se na oglas
                                                    </button>
                                                </div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>
                </>
            )}
        </div>
    );
}

export default StudentDashboard;