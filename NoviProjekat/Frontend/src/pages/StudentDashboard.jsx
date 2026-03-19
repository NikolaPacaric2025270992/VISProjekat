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
    
    // NOVO: Dodato stanje za SVE oglase, pored preporučenih
    const [preporuceniOglasi, setPreporuceniOglasi] = useState([]);
    const [sviOglasi, setSviOglasi] = useState([]); 

    // Stanja za Podešavanja profila
    const [prikaziPodesavanja, setPrikaziPodesavanja] = useState(false);
    const [editIme, setEditIme] = useState('');
    const [editPrezime, setEditPrezime] = useState('');
    const [editTraziZaposlenje, setEditTraziZaposlenje] = useState(false);
    const [trenutnaLozinka, setTrenutnaLozinka] = useState('');
    const [novaLozinka, setNovaLozinka] = useState('');
    const [potvrdaLozinke, setPotvrdaLozinke] = useState('');

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

        // NOVO: Dohvatanje apsolutno svih oglasa u bazi za tržište rada
        axios.get('http://localhost:8080/api/oglasi/svi')
            .then(res => setSviOglasi(res.data))
            .catch(err => console.error("Greška pri učitavanju svih oglasa:", err));

    }, [navigate]);

    const handleOdjava = () => {
        localStorage.removeItem('user');
        localStorage.removeItem('role');
        navigate('/prijava');
    };

    const otvoriPodesavanja = () => {
        setEditIme(student.ime);
        setEditPrezime(student.prezime);
        setEditTraziZaposlenje(student.traziZaposlenje);
        setPrikaziPodesavanja(true);
    };

    const handleAzurirajProfil = async (e) => {
        e.preventDefault();
        const azuriranStudent = { ...student, ime: editIme, prezime: editPrezime, traziZaposlenje: editTraziZaposlenje };
        
        if (trenutnaLozinka || novaLozinka || potvrdaLozinke) {
            if (trenutnaLozinka !== student.lozinka) { alert("Trenutna lozinka nije tačna!"); return; }
            if (novaLozinka !== potvrdaLozinke) { alert("Nove lozinke se ne poklapaju!"); return; }
            if (novaLozinka.length < 5) { alert("Nova lozinka mora imati bar 5 karaktera."); return; }
            azuriranStudent.lozinka = novaLozinka; 
        }

        try {
            await axios.put('http://localhost:8080/api/studenti/update', azuriranStudent);
            setStudent(azuriranStudent);
            localStorage.setItem('user', JSON.stringify(azuriranStudent)); 
            alert("Profil uspešno ažuriran!");
            setTrenutnaLozinka(''); setNovaLozinka(''); setPotvrdaLozinke('');
            setPrikaziPodesavanja(false); 
        } catch (error) { alert("Greška pri ažuriranju profila."); }
    };

    const handleObrisiNalog = async () => {
        const potvrda = window.confirm("Da li ste sigurni da želite trajno da obrišete nalog? Svi vaši podaci i položeni ispiti će biti obrisani.");
        if (potvrda) {
            try {
                await axios.delete(`http://localhost:8080/api/studenti/obrisi/${student.id}`);
                alert("Nalog je uspešno obrisan.");
                handleOdjava(); 
            } catch (error) { alert("Greška pri brisanju naloga."); }
        }
    };

    const handleObrisiPolaganje = async (id) => {
        if (window.confirm("Da li ste sigurni da želite da obrišete ovaj ispit?")) {
            try {
                await axios.delete(`http://localhost:8080/api/polaganja/obrisi/${id}`);
                setPolaganja(polaganja.filter(p => p.id !== id));
                const resPreporuke = await axios.get(`http://localhost:8080/api/studenti/${student.email}/preporuke`);
                setPreporuceniOglasi(resPreporuke.data);
                alert("Ispit uspešno uklonjen.");
            } catch (error) { alert("Greška pri brisanju ispita."); }
        }
    };

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
            setSearchQuery(''); setIzabranPredmet(null); setOcena(6);
            
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
                    {!prikaziPodesavanja ? (
                        <button onClick={otvoriPodesavanja} className="btn btn-outline-secondary btn-sm me-2">⚙️ Podešavanja</button>
                    ) : (
                        <button onClick={() => setPrikaziPodesavanja(false)} className="btn btn-outline-secondary btn-sm me-2">🔙 Nazad na Dashboard</button>
                    )}
                    <button onClick={handleOdjava} className="btn btn-outline-danger btn-sm">Odjavi se</button>
                </div>
            </div>
            
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

                                    <hr className="my-4"/>
                                    <h6 className="fw-bold mb-3">Promena lozinke (Opciono)</h6>
                                    <div className="mb-2">
                                        <input type="password" placeholder="Trenutna lozinka" className="form-control" value={trenutnaLozinka} onChange={(e) => setTrenutnaLozinka(e.target.value)} />
                                    </div>
                                    <div className="mb-2">
                                        <input type="password" placeholder="Nova lozinka" className="form-control" value={novaLozinka} onChange={(e) => setNovaLozinka(e.target.value)} />
                                    </div>
                                    <div className="mb-4">
                                        <input type="password" placeholder="Potvrdi novu lozinku" className="form-control" value={potvrdaLozinke} onChange={(e) => setPotvrdaLozinke(e.target.value)} />
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
                /* PRIKAZ: STANDARDNI DASHBOARD (Ispiti i Oglasi) */
                <>
                    {/* Gornji red: Unos ispita i Tabela */}
                    <div className="row animate__animated animate__fadeIn">
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

                        <div className="col-md-7">
                            <h4 className="mb-3 text-secondary">Moji položeni ispiti</h4>
                            <div className="table-responsive shadow-sm" style={{ maxHeight: '350px', overflowY: 'auto' }}>
                                <table className="table table-hover bg-white border mb-0">
                                    <thead className="table-success text-white position-sticky top-0" style={{ zIndex: 1 }}>
                                        <tr>
                                            <th>Predmet</th>
                                            <th className="text-center">Ocena</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {polaganja.length === 0 ? (
                                            <tr><td colSpan="2" className="text-center text-muted py-4">Nema evidentiranih ispita.</td></tr>
                                        ) : (
                                            polaganja.map((p, index) => (
                                                <tr key={p.id || index}>
                                                    <td className="align-middle">{p.nazivPredmeta || p.predmetId}</td>
                                                    <td className="text-center align-middle">
                                                        <span className="badge bg-success rounded-pill me-2">{p.ocena}</span>
                                                        <button 
                                                            onClick={() => handleObrisiPolaganje(p.id)} 
                                                            className="btn btn-sm btn-danger py-0 px-2 fw-bold" 
                                                            title="Ukloni polaganje">
                                                            X
                                                        </button>
                                                    </td>
                                                </tr>
                                            ))
                                        )}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>

                    {/* Dve kolone za Oglase (NOVO: Svi Oglasi vs Preporučeni Oglasi) */}
                    <div className="row mt-5 pt-3 border-top animate__animated animate__fadeInUp">
                        
                        {/* Leva Kolona: Svi Oglasi sa tržišta */}
                        <div className="col-lg-6 mb-4">
                            <h4 className="mb-3 text-secondary d-flex justify-content-between align-items-center pb-2 border-bottom">
                                <span>Tržište rada (Svi oglasi)</span>
                                <span className="badge bg-secondary rounded-pill">{sviOglasi.length}</span>
                            </h4>
                            
                            <div className="pe-2" style={{ maxHeight: '600px', overflowY: 'auto' }}>
                                {sviOglasi.length === 0 ? (
                                    <div className="alert alert-light text-center border text-muted">Trenutno nema otvorenih pozicija.</div>
                                ) : (
                                    sviOglasi.map((oglas, idx) => (
                                        <div key={oglas.id || idx} className="card shadow-sm mb-3 border-start border-secondary border-4">
                                            <div className="card-body">
                                                <h6 className="card-title text-dark fw-bold mb-1">{oglas.naslov}</h6>
                                                <p className="text-muted small mb-3">ID Oglasa: {oglas.oglasID || oglas.id}</p>
                                                
                                                {/* Prikaz traženih veština za oglas (ukoliko postoje) */}
                                                <div className="mb-3">
                                                    {oglas.zahtevaneVestine && oglas.zahtevaneVestine.map((zv, zIdx) => (
                                                        <span key={zIdx} className="badge bg-light text-secondary border me-1 mb-1" style={{ fontSize: '0.75rem' }}>
                                                            {zv.vestina?.id || zv.vestinaId}
                                                        </span>
                                                    ))}
                                                </div>

                                                <button className="btn btn-sm btn-outline-secondary w-100">Vidi detalje</button>
                                            </div>
                                        </div>
                                    ))
                                )}
                            </div>
                        </div>

                        {/* Desna Kolona: Preporučeni poslovi */}
                        <div className="col-lg-6 mb-4">
                            <h4 className="mb-3 text-success d-flex justify-content-between align-items-center pb-2 border-bottom">
                                <span>Preporučeni za tebe 🌟</span>
                                <span className="badge bg-success rounded-pill">{preporuceniOglasi.length}</span>
                            </h4>
                            
                            <div className="pe-2" style={{ maxHeight: '600px', overflowY: 'auto' }}>
                                {preporuceniOglasi.length === 0 ? (
                                    <div className="alert alert-success bg-opacity-10 text-center border shadow-sm text-success py-4">
                                        Trenutno nemamo poslova koji se poklapaju sa tvojim veštinama. <br/>
                                        <strong>Položi još neki ispit da otključaš preporuke!</strong>
                                    </div>
                                ) : (
                                    preporuceniOglasi.map((oglas, idx) => (
                                        <div key={oglas.id || idx} className="card shadow-sm mb-3 border-start border-primary border-4 bg-light">
                                            <div className="card-body">
                                                <div className="d-flex justify-content-between align-items-start mb-2">
                                                    <h6 className="card-title text-success fw-bold mb-0">{oglas.naslov}</h6>
                                                    <span className="badge bg-warning text-dark" title="Tvoj skor za ovaj oglas">
                                                        Bodovi: {oglas.bodovi || oglas.ukupniBodovi || 'N/A'}
                                                    </span>
                                                </div>
                                                <p className="text-muted small mb-3">Savršeno se uklapa u tvoj profil!</p>
                                                <button className="btn btn-sm btn-success w-100 fw-bold">Prijavi se odmah</button>
                                            </div>
                                        </div>
                                    ))
                                )}
                            </div>
                        </div>

                    </div>
                </>
            )}
        </div>
    );
}

export default StudentDashboard;