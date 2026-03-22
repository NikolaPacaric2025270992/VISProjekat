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

    // Stanja za IMPORT fajlova
    const [importFajl, setImportFajl] = useState(null);
    const [importLoading, setImportLoading] = useState(false);

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

    const handleImportPolaganja = async (e) => {
        e.preventDefault();
        if (!importFajl) {
            alert("Molimo vas da prvo izaberete JSON ili XML fajl.");
            return;
        }

        const formData = new FormData();
        formData.append("fajl", importFajl); 
        formData.append("studentId", student.id); 

        setImportLoading(true);
        try {
            await axios.post('http://localhost:8080/api/studenti/import-polaganja', formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });
            alert("Položeni ispiti su uspešno importovani!");
            
            // Osvežavamo tabelu polaganja
            const resPolaganja = await axios.get(`http://localhost:8080/api/polaganja/student/${student.id}`);
            setPolaganja(resPolaganja.data);

            // Osvežavamo tabelu preporuka pošto su došli novi ispiti
            const resPreporuke = await axios.get(`http://localhost:8080/api/studenti/${student.email}/preporuke`);
            setPreporuceniOglasi(resPreporuke.data);

            setImportFajl(null);
            document.getElementById('importFileInput').value = ''; 
        } catch (error) {
            console.error("Greška pri importu polaganja:", error);
            alert("Došlo je do greške prilikom importa. Provjerite da li je format fajla ispravan (JSON/XML).");
        } finally {
            setImportLoading(false);
        }
    };

    const preuzmiFajl = async (url, format, filename) => {
        try {
            const res = await axios.get(`${url}?format=${format}`, { responseType: 'blob' });
            const blob = new Blob([res.data]);
            const link = document.createElement('a');
            link.href = window.URL.createObjectURL(blob);
            link.download = `${filename}.${format}`;
            link.click();
        } catch (error) {
            alert("Greška pri preuzimanju fajla.");
        }
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
                            <div className="card shadow-sm border-0 mb-4">
                                <div className="card-header bg-success text-white py-3">
                                    <h5 className="mb-0">Evidentiraj novi ispit</h5>
                                </div>
                                <div className="card-body">
                                    
                                    {/* DEO 1: IMPORT (Kompaktno na vrhu) */}
                                    <div className="mb-4 bg-light p-3 rounded border border-success border-opacity-50">
                                        <h6 className="fw-bold text-success mb-2 small">Masovni unos (JSON/XML)</h6>
                                        <form onSubmit={handleImportPolaganja} className="d-flex gap-2">
                                            <input 
                                                id="importFileInput"
                                                type="file" 
                                                className="form-control form-control-sm" 
                                                accept=".json,.xml" 
                                                onChange={(e) => setImportFajl(e.target.files[0])} 
                                            />
                                            <button 
                                                type="submit" 
                                                className="btn btn-sm btn-success text-white fw-bold px-3 text-nowrap" 
                                                disabled={importLoading || !importFajl}
                                            >
                                                {importLoading ? '⏳...' : '📥 Importuj'}
                                            </button>
                                        </form>
                                    </div>

                                    {/* VIZUELNI PREKID */}
                                    <div className="text-center text-muted mb-4 position-relative">
                                        <hr className="position-absolute w-100" style={{ top: '50%', transform: 'translateY(-50%)', zIndex: 0 }} />
                                        <span className="bg-white px-2 position-relative fw-bold" style={{ fontSize: '0.8rem', zIndex: 1 }}>ILI UNESITE MANUELNO</span>
                                    </div>

                                    {/* DEO 2: MANUELNI UNOS */}
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
                            <div className="table-responsive shadow-sm" style={{ maxHeight: '450px', overflowY: 'auto' }}>
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

                    {/* Dve kolone za Oglase (NOVO: Kompaktni List View sa Export dugmićima) */}
                    <div className="row mt-5 pt-3 border-top animate__animated animate__fadeInUp">
                        
                        {/* Leva Kolona: Svi Oglasi sa tržišta */}
                        <div className="col-lg-6 mb-4">
                            <h4 className="mb-3 text-secondary d-flex justify-content-between align-items-center pb-2 border-bottom">
                                <div>
                                    <span>Tržište rada (Svi oglasi)</span>
                                    <span className="badge bg-secondary rounded-pill ms-2">{sviOglasi.length}</span>
                                </div>
                                <div>
                                    {/* Export dugmići sa disabled logikom */}
                                    <button 
                                        onClick={() => preuzmiFajl('http://localhost:8080/api/oglasi/svi/export', 'json', 'svi_oglasi')}
                                        className="btn btn-sm btn-outline-secondary me-1"
                                        disabled={sviOglasi.length === 0} title="Preuzmi kao JSON"
                                    >📥 JSON</button>
                                    <button 
                                        onClick={() => preuzmiFajl('http://localhost:8080/api/oglasi/svi/export', 'xml', 'svi_oglasi')}
                                        className="btn btn-sm btn-outline-secondary"
                                        disabled={sviOglasi.length === 0} title="Preuzmi kao XML"
                                    >📥 XML</button>
                                </div>
                            </h4>
                            
                            <div className="pe-2" style={{ maxHeight: '600px', overflowY: 'auto' }}>
                                {sviOglasi.length === 0 ? (
                                    <div className="alert alert-light border text-center text-muted py-3">Trenutno nema otvorenih pozicija na tržištu.</div>
                                ) : (
                                    <div className="d-flex flex-column">
                                        {sviOglasi.map((oglas, idx) => (
                                            <div key={oglas.id || idx} className="d-flex justify-content-between align-items-center p-3 mb-2 bg-white border rounded shadow-sm">
                                                
                                                {/* Levo: Naslov, Veštine i ID */}
                                                <div className="d-flex flex-column" style={{ flex: '1' }}>
                                                    <h6 className="fw-bold text-dark mb-1">{oglas.naslov}</h6>
                                                    <div className="mb-1">
                                                        {oglas.zahtevaneVestine && oglas.zahtevaneVestine.map((zv, zIdx) => (
                                                            <span key={zIdx} className="badge bg-light text-secondary border me-1" style={{ fontSize: '0.7rem', fontWeight: 'normal' }}>
                                                                {zv.vestina?.id || zv.vestinaId}
                                                            </span>
                                                        ))}
                                                    </div>
                                                    <small className="text-muted" style={{ fontSize: '0.7rem' }}>ID: {oglas.oglasID || oglas.id}</small>
                                                </div>

                                                {/* Desno: Dugme */}
                                                <div className="text-end ms-2">
                                                    <button className="btn btn-sm btn-outline-secondary fw-bold px-3">Detalji</button>
                                                </div>

                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                        </div>

                        {/* Desna Kolona: Preporučeni poslovi */}
                        <div className="col-lg-6 mb-4">
                            <h4 className="mb-3 text-success d-flex justify-content-between align-items-center pb-2 border-bottom">
                                <div>
                                    <span>Preporučeni za tebe 🌟</span>
                                    <span className="badge bg-success rounded-pill ms-2">{preporuceniOglasi.length}</span>
                                </div>
                                <div>
                                    {/* Export dugmići sa disabled logikom */}
                                    <button 
                                        onClick={() => preuzmiFajl(`http://localhost:8080/api/studenti/${student.email}/preporuke/export`, 'json', 'preporuke')}
                                        className="btn btn-sm btn-outline-success me-1"
                                        disabled={preporuceniOglasi.length === 0} title="Preuzmi kao JSON"
                                    >📥 JSON</button>
                                    <button 
                                        onClick={() => preuzmiFajl(`http://localhost:8080/api/studenti/${student.email}/preporuke/export`, 'xml', 'preporuke')}
                                        className="btn btn-sm btn-outline-success"
                                        disabled={preporuceniOglasi.length === 0} title="Preuzmi kao XML"
                                    >📥 XML</button>
                                </div>
                            </h4>
                            
                            <div className="pe-2" style={{ maxHeight: '600px', overflowY: 'auto' }}>
                                {preporuceniOglasi.length === 0 ? (
                                    <div className="alert alert-success bg-opacity-10 text-center border border-success border-opacity-25 shadow-sm text-success py-4">
                                        Trenutno nemamo poslova koji se poklapaju sa tvojim veštinama. <br/>
                                        <strong>Položi još neki ispit da otključaš preporuke!</strong>
                                    </div>
                                ) : (
                                    <div className="d-flex flex-column">
                                        {preporuceniOglasi.map((oglas, idx) => (
                                            <div key={oglas.id || idx} className="d-flex justify-content-between align-items-center p-3 mb-2 bg-success bg-opacity-10 border border-success border-opacity-25 rounded shadow-sm">
                                                
                                                {/* Levo: Naslov, Bodovi i Poruka */}
                                                <div className="d-flex flex-column" style={{ flex: '1' }}>
                                                    <div className="d-flex align-items-center mb-1">
                                                        <h6 className="fw-bold text-success mb-0 me-2">{oglas.naslov}</h6>
                                                        <span className="badge bg-warning text-dark py-1" style={{ fontSize: '0.7rem' }} title="Tvoj skor za ovaj oglas">
                                                            Bodovi: {oglas.bodovi || oglas.ukupniBodovi || 'N/A'}
                                                        </span>
                                                    </div>
                                                    <small className="text-success text-opacity-75" style={{ fontSize: '0.75rem' }}>Savršeno se uklapa u tvoj profil!</small>
                                                </div>

                                                {/* Desno: Dugme */}
                                                <div className="text-end ms-2">
                                                    <button className="btn btn-sm btn-success fw-bold px-3">Prijavi se</button>
                                                </div>

                                            </div>
                                        ))}
                                    </div>
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