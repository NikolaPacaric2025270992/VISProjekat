import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

function AgencijaDashboard() {
    const navigate = useNavigate();

    const [agencija, setAgencija] = useState(null);
    const [vestine, setVestine] = useState([]);
    const [oglasi, setOglasi] = useState([]); 
    
    const [prikazanePreporuke, setPrikazanePreporuke] = useState(null); 
    const [aktivniStudenti, setAktivniStudenti] = useState([]);
    const [detaljiStudenta, setDetaljiStudenta] = useState(null);

    const [naslovOglasa, setNaslovOglasa] = useState('');
    const [zahtevi, setZahtevi] = useState([
        { vestinaId: '', nivo: 'POCETNI', prioritet: 'NIZAK' }
    ]);

    const [prikaziPodesavanja, setPrikaziPodesavanja] = useState(false);
    const [editNazivAgencije, setEditNazivAgencije] = useState('');
    const [editLokacija, setEditLokacija] = useState('');
    const [editPib, setEditPib] = useState('');
    const [trenutnaLozinka, setTrenutnaLozinka] = useState('');
    const [novaLozinka, setNovaLozinka] = useState('');
    const [potvrdaLozinke, setPotvrdaLozinke] = useState('');

    // Stanja za IMPORT fajlova
    const [importFajl, setImportFajl] = useState(null);
    const [importLoading, setImportLoading] = useState(false);

    useEffect(() => {
        const ulogovanKorisnik = localStorage.getItem('user');
        const rola = localStorage.getItem('role');

        if (!ulogovanKorisnik || rola !== 'agencija') {
            navigate('/prijava');
            return;
        }

        const podaciAgencije = JSON.parse(ulogovanKorisnik);
        if (podaciAgencije.lozinka) {
            delete podaciAgencije.lozinka;
            localStorage.setItem('user', JSON.stringify(podaciAgencije));
        }
        setAgencija(podaciAgencije);

        axios.get('http://localhost:8080/api/vestine')
            .then(res => {
                setVestine(res.data);
                if(res.data.length > 0) {
                    setZahtevi([{ vestinaId: res.data[0].id, nivo: 'POCETNI', prioritet: 'NIZAK' }]);
                }
            })
            .catch(err => console.error("Greška pri učitavanju veština:", err));

        axios.get(`http://localhost:8080/api/oglasi/agencija/${podaciAgencije.id}`)
            .then(res => setOglasi(res.data))
            .catch(err => console.error("Greška pri učitavanju oglasa:", err));

        axios.get('http://localhost:8080/api/studenti/aktivni')
            .then(res => setAktivniStudenti(res.data))
            .catch(err => console.error("Greška pri učitavanju aktivnih studenata:", err));

    }, [navigate]);

    const handleOdjava = () => {
        localStorage.removeItem('user');
        localStorage.removeItem('role');
        navigate('/prijava');
    };

    const otvoriPodesavanja = () => {
        setEditNazivAgencije(agencija.nazivAgencije);
        setEditLokacija(agencija.lokacija || '');
        setEditPib(agencija.pib || '');
        setPrikaziPodesavanja(true);
    };

    const handleAzurirajProfil = async (e) => {
        e.preventDefault();
        const azuriranaAgencija = { 
            id: agencija.id,
            email: agencija.email,
            nazivAgencije: editNazivAgencije, 
            lokacija: editLokacija, 
            pib: editPib 
        };
        const menjaLozinku = trenutnaLozinka || novaLozinka || potvrdaLozinke;

        if (menjaLozinku) {
            if (!trenutnaLozinka || !novaLozinka || !potvrdaLozinke) { alert("Popunite sva polja za promenu lozinke."); return; }
            if (novaLozinka !== potvrdaLozinke) { alert("Nove lozinke se ne poklapaju!"); return; }
            if (novaLozinka.length < 5) { alert("Nova lozinka mora imati bar 5 karaktera."); return; }
        }
        
        try {
            if (menjaLozinku) {
                await axios.put('http://localhost:8080/api/agencija/promeni-lozinku', {
                    email: agencija.email,
                    staraLozinka: trenutnaLozinka,
                    novaLozinka
                });
            }

            const res = await axios.put('http://localhost:8080/api/agencija/update', azuriranaAgencija);
            setAgencija(res.data);
            localStorage.setItem('user', JSON.stringify(res.data));
            alert("Podaci agencije su uspešno ažurirani!");
            setTrenutnaLozinka(''); setNovaLozinka(''); setPotvrdaLozinke('');
            setPrikaziPodesavanja(false);
        } catch (error) {
            alert(error.response?.data || "Greška pri ažuriranju agencije.");
        }
    };

    const handleObrisiNalog = async () => {
        const potvrda = window.confirm("Da li ste sigurni da želite trajno da obrišete agenciju? Oglasi agencije će takođe biti nedostupni.");
        if (potvrda) {
            try {
                await axios.delete(`http://localhost:8080/api/agencija/obrisi/${agencija.id}`);
                alert("Nalog agencije je uspešno obrisan iz sistema.");
                handleOdjava();
            } catch {
                alert("Greška pri brisanju naloga agencije.");
            }
        }
    };

    const handleObrisiOglas = async (id) => {
        if (window.confirm("Trajno obrisati ovaj oglas? Sve preporuke će biti izgubljene.")) {
            try {
                await axios.delete(`http://localhost:8080/api/oglasi/obrisi/${id}`);
                setOglasi(oglasi.filter(o => o.id !== id));
                if (prikazanePreporuke && prikazanePreporuke.oglas.id === id) {
                    setPrikazanePreporuke(null);
                }
                alert("Oglas uspešno obrisan.");
            } catch { alert("Greška pri brisanju oglasa."); }
        }
    };

    const handleZahtevChange = (index, polje, vrednost) => {
        const noviZahtevi = [...zahtevi];
        noviZahtevi[index][polje] = vrednost;
        setZahtevi(noviZahtevi);
    };

    const imaDupliranihVestina = (listaZahteva) => {
        const izabraneVestine = listaZahteva
            .map(z => z.vestinaId)
            .filter(Boolean);
        return new Set(izabraneVestine).size !== izabraneVestine.length;
    };

    const prvaSlobodnaVestina = () => {
        const zauzeteVestine = new Set(zahtevi.map(z => z.vestinaId).filter(Boolean));
        return vestine.find(v => !zauzeteVestine.has(v.id))?.id || '';
    };

    const vestinaJeIzabranaUDrugomZahtevu = (vestinaId, trenutniIndex) => {
        return zahtevi.some((z, index) => index !== trenutniIndex && z.vestinaId === vestinaId);
    };

    const dodajNoviZahtev = () => {
        const slobodnaVestina = prvaSlobodnaVestina();
        if (!slobodnaVestina) {
            alert("Sve dostupne veštine su već dodate u ovaj oglas.");
            return;
        }

        setZahtevi([...zahtevi, { vestinaId: slobodnaVestina, nivo: 'POCETNI', prioritet: 'NIZAK' }]);
    };

    const ukloniZahtev = (index) => {
        const noviZahtevi = zahtevi.filter((_, i) => i !== index);
        setZahtevi(noviZahtevi);
    };

    const handleDodajOglas = async (e) => {
        e.preventDefault();
        if (imaDupliranihVestina(zahtevi)) {
            alert("Ista veština ne može biti dodata više puta u jednom oglasu.");
            return;
        }

        const noviOglas = {
            id: `oglas_${Date.now()}`,
            naslov: naslovOglasa,
            agencijaId: agencija.id,
            zahtevaneVestine: zahtevi.map(z => ({
                vestina: { id: z.vestinaId },
                nivo: z.nivo,
                prioritet: z.prioritet
            }))
        };

        try {
            await axios.post('http://localhost:8080/api/oglasi/postavi', noviOglas);
            setOglasi([...oglasi, noviOglas]); 
            setNaslovOglasa('');
            setZahtevi([{ vestinaId: vestine[0]?.id || '', nivo: 'POCETNI', prioritet: 'NIZAK' }]); 
        } catch (error) {
            alert(error.response?.data || "Greška pri dodavanju oglasa.");
        }
    };

    const handleImportOglasa = async (e) => {
        e.preventDefault();
        if (!importFajl) {
            alert("Molimo vas da prvo izaberete JSON ili XML fajl.");
            return;
        }

        const formData = new FormData();
        formData.append("fajl", importFajl); 
        formData.append("agencijaId", agencija.id); 

        setImportLoading(true);
        try {
            await axios.post('http://localhost:8080/api/oglasi/import', formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });
            alert("Oglasi su uspješno importovani!");
            const res = await axios.get(`http://localhost:8080/api/oglasi/agencija/${agencija.id}`);
            setOglasi(res.data);
            setImportFajl(null);
            document.getElementById('importFileInput').value = ''; 
        } catch (error) {
            console.error("Greška pri importu:", error);
            alert(error.response?.data || "Došlo je do greške prilikom importa. Provjerite da li je format fajla ispravan (JSON/XML).");
        } finally {
            setImportLoading(false);
        }
    };

    const handleVidiPreporuke = async (oglas) => {
        try {
            const res = await axios.get(`http://localhost:8080/api/oglasi/${oglas.id}/rang-lista`);
            setPrikazanePreporuke({ oglas: oglas, kandidati: res.data });
            
            setTimeout(() => {
                window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' });
            }, 100);
            
        } catch (error) {
            console.error("Greška:", error);
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
        } catch {
            alert("Greška pri preuzimanju fajla.");
        }
    };

    // 1. Funkcija koja vraća donje crte nazad u '@' i '.'
    const formatirajEmail = (id) => {
        if (!id) return "N/A";
        // Ako već ima @ (iz ArangoDB), vrati ga kako jeste
        if (id.includes('@')) return id; 

        // Pretvara 'marko_student_rs' nazad u 'marko@student.rs'
        const prviDeo = id.indexOf('_');
        const zadnjiDeo = id.lastIndexOf('_');
        
        if (prviDeo !== -1 && zadnjiDeo !== -1 && prviDeo !== zadnjiDeo) {
            return id.substring(0, prviDeo) + '@' + 
                   id.substring(prviDeo + 1, zadnjiDeo) + '.' + 
                   id.substring(zadnjiDeo + 1);
        }
        return id;
    };

    // 2. Funkcija koja određuje vizuelni status studenta na osnovu bodova i broja veština
    const odrediStatusKandidata = (bodovi, brojVestina) => {
        const skor = parseFloat(bodovi) || 0;
        
        // Množilac je broj veština. Ako oglas nema veština (što ne bi trebalo), stavljamo 1.
        const mnozilac = brojVestina > 0 ? brojVestina : 1; 
        
        // Dinamički pragovi bazirani na SPARQL matematici
        const pragSavrsen = 300 * mnozilac;
        const pragDobar = 150 * mnozilac;

        if (skor >= pragSavrsen) {
            return { tekst: "Savršen kandidat", bojaBadga: "bg-success", ikonica: "🔥", bojaReda: "table-success border-success" };
        } else if (skor >= pragDobar) {
            return { tekst: "Dobar kandidat", bojaBadga: "bg-primary", ikonica: "⭐", bojaReda: "" };
        } else {
            return { tekst: "Delimično poklapanje", bojaBadga: "bg-warning text-dark", ikonica: "📈", bojaReda: "table-light text-muted" };
        }
    };

    const nazivVestine = (vestinaId) => {
        const vestina = vestine.find(v => v.id === vestinaId || v._key === vestinaId);
        return vestina?.naziv || vestinaId;
    };

    const formatirajEnum = (vrednost) => {
        if (!vrednost) return '';
        return vrednost.charAt(0).toUpperCase() + vrednost.slice(1).toLowerCase();
    };

    const prikaziKontakt = (studentZaKontakt) => {
        const email = studentZaKontakt.email || formatirajEmail(studentZaKontakt.id);
        alert(`Demo akcija: ovde bi se otvorila komunikacija sa kandidatom ${email}.`);
    };

    if (!agencija) return <div className="text-center mt-5">Učitavanje...</div>;

    return (
        <div className="container mt-4 mb-5">
            {/* ZAGLAVLJE */}
            <div className="d-flex justify-content-between align-items-center mb-4 pb-2 border-bottom">
                <h2 className="text-primary">Panel: {agencija.nazivAgencije}</h2>
                <div>
                    <span className="me-3 fw-bold text-muted">PIB: {agencija.pib} | {agencija.lokacija}</span>
                    {!prikaziPodesavanja ? (
                        <button onClick={otvoriPodesavanja} className="btn btn-outline-secondary btn-sm me-2">⚙️ Podešavanja</button>
                    ) : (
                        <button onClick={() => setPrikaziPodesavanja(false)} className="btn btn-outline-secondary btn-sm me-2">🔙 Nazad na Dashboard</button>
                    )}
                    <button onClick={handleOdjava} className="btn btn-outline-danger btn-sm">Odjavi se</button>
                </div>
            </div>

            {prikaziPodesavanja ? (
                /* PODEŠAVANJA AGENCIJE */
                <div className="row justify-content-center animate__animated animate__fadeIn">
                    <div className="col-md-6">
                        <div className="card shadow-sm border-0">
                            <div className="card-header bg-secondary text-white py-3">
                                <h5 className="mb-0">Podešavanja profila agencije</h5>
                            </div>
                            <div className="card-body">
                                <form onSubmit={handleAzurirajProfil}>
                                    <div className="mb-3">
                                        <label className="form-label fw-bold">Naziv Agencije:</label>
                                        <input type="text" className="form-control" value={editNazivAgencije} onChange={(e) => setEditNazivAgencije(e.target.value)} required />
                                    </div>
                                    <div className="mb-3">
                                        <label className="form-label fw-bold">PIB (Poreski broj):</label>
                                        <input type="text" className="form-control" value={editPib} onChange={(e) => setEditPib(e.target.value)} required />
                                    </div>
                                    <div className="mb-4">
                                        <label className="form-label fw-bold">Lokacija (Sedište):</label>
                                        <input type="text" className="form-control" value={editLokacija} onChange={(e) => setEditLokacija(e.target.value)} required />
                                    </div>
                                    <hr className="my-4"/>
                                    <h6 className="fw-bold mb-3 text-secondary">Promena lozinke (Opciono)</h6>
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
                                    <p className="text-muted small">Ukoliko obrišete nalog, svi podaci vaše agencije biće trajno uklonjeni iz sistema.</p>
                                    <button onClick={handleObrisiNalog} className="btn btn-danger btn-sm">🗑️ Obriši agenciju trajno</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            ) : (
                /* STANDARDNI DASHBOARD */
                <>
                    <div className="row animate__animated animate__fadeIn">
                        
                        {/* Leva kolona: Centralna forma za dodavanje oglasa (Import + Manuelno) */}
                        <div className="col-md-5">
                            <div className="card shadow-sm border-0 mb-4">
                                <div className="card-header bg-primary text-white py-3">
                                    <h5 className="mb-0">Dodaj nove oglase</h5>
                                </div>
                                <div className="card-body">
                                    
                                    {/* DEO 1: IMPORT (Kompaktno na vrhu) */}
                                    <div className="mb-4 bg-light p-3 rounded border border-primary border-opacity-50">
                                        <h6 className="fw-bold text-primary mb-2 small">Masovni unos (JSON/XML)</h6>
                                        <form onSubmit={handleImportOglasa} className="d-flex gap-2">
                                            <input 
                                                id="importFileInput"
                                                type="file" 
                                                className="form-control form-control-sm" 
                                                accept=".json,.xml" 
                                                onChange={(e) => setImportFajl(e.target.files[0])} 
                                            />
                                            <button 
                                                type="submit" 
                                                className="btn btn-sm btn-primary text-white fw-bold px-3 text-nowrap" 
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

                                    {/* DEO 2: MANUELNI UNOS (Ispod) */}
                                    <form onSubmit={handleDodajOglas}>
                                        <div className="mb-4">
                                            <label className="form-label fw-bold">Naslov pozicije:</label>
                                            <input type="text" className="form-control" value={naslovOglasa} onChange={(e) => setNaslovOglasa(e.target.value)} required placeholder="npr. Full Stack Developer" />
                                        </div>

                                        <div className="d-flex justify-content-between align-items-center border-bottom pb-2 mb-3">
                                            <h6 className="mb-0 fw-bold text-secondary">Potrebne veštine</h6>
                                            <button type="button" onClick={dodajNoviZahtev} className="btn btn-sm btn-outline-primary" disabled={zahtevi.length >= vestine.length}>+ Dodaj veštinu</button>
                                        </div>
                                        
                                        {zahtevi.map((zahtev, index) => (
                                            <div key={index} className="p-3 mb-3 bg-light border rounded position-relative">
                                                {zahtevi.length > 1 && (
                                                    <button type="button" onClick={() => ukloniZahtev(index)} className="btn btn-sm btn-danger position-absolute top-0 end-0 m-1">X</button>
                                                )}
                                                
                                                <div className="mb-2">
                                                    <label className="form-label small fw-bold">Veština:</label>
                                                    <select className="form-select form-select-sm" value={zahtev.vestinaId} onChange={(e) => handleZahtevChange(index, 'vestinaId', e.target.value)}>
                                                        <option value="" disabled>Izaberite veštinu...</option>
                                                        {vestine.map(v => (
                                                            <option key={v.id} value={v.id} disabled={vestinaJeIzabranaUDrugomZahtevu(v.id, index)}>{v.naziv}</option>
                                                        ))}
                                                    </select>
                                                </div>

                                                <div className="row g-2">
                                                    <div className="col">
                                                        <label className="form-label small fw-bold">Traženi nivo:</label>
                                                        <select className="form-select form-select-sm" value={zahtev.nivo} onChange={(e) => handleZahtevChange(index, 'nivo', e.target.value)}>
                                                            <option value="POCETNI">Početni</option>
                                                            <option value="SREDNJI">Srednji</option>
                                                            <option value="NAPREDNI">Napredni</option>
                                                        </select>
                                                    </div>
                                                    <div className="col">
                                                        <label className="form-label small fw-bold">Prioritet:</label>
                                                        <select className="form-select form-select-sm" value={zahtev.prioritet} onChange={(e) => handleZahtevChange(index, 'prioritet', e.target.value)}>
                                                            <option value="NIZAK">Nizak</option>
                                                            <option value="VISOK">Visok</option>
                                                        </select>
                                                    </div>
                                                </div>
                                            </div>
                                        ))}

                                        <button type="submit" className="btn btn-primary w-100 mt-3 py-2">Objavi oglas</button>
                                    </form>
                                </div>
                            </div>
                        </div>

                        {/* Desna kolona: Lista aktivnih oglasa */}
                        <div className="col-md-7">
                            <h4 className="mb-3 text-secondary">Moji aktivni oglasi</h4>
                            
                            {oglasi.length === 0 ? (
                                <div className="alert alert-info text-center py-2">Trenutno nemate aktivnih oglasa. Napravite svoj prvi oglas levo!</div>
                            ) : (
                                <div className="d-flex flex-column gap-2 pe-2" style={{ maxHeight: '750px', overflowX: 'hidden', overflowY: 'auto' }}>
                                    {oglasi.map((oglas, oIdx) => (
                                        <div 
                                            key={oglas.id || `oglas_${oIdx}`} 
                                            className={`card shadow-sm border rounded ${prikazanePreporuke?.oglas.id === oglas.id ? 'border-warning bg-warning bg-opacity-10' : ''}`}
                                        >
                                            <div className="card-body p-2">
                                                <div className="d-flex justify-content-between align-items-center mb-2">
                                                    <h6 className="card-title text-primary fw-bold mb-0">{oglas.naslov}</h6>
                                                    <button 
                                                        onClick={() => handleObrisiOglas(oglas.id)} 
                                                        className="btn btn-sm btn-outline-danger py-0 px-2" 
                                                        title="Trajno obriši oglas" 
                                                        style={{ fontSize: '0.75rem' }}
                                                    >
                                                        🗑️ Ukloni
                                                    </button>
                                                </div>
                                                
                                                <div className="mb-2">
                                                    {oglas.zahtevaneVestine && oglas.zahtevaneVestine.map((zv, idx) => (
                                                        <span key={idx} className="badge bg-light text-secondary border me-1 mb-1" style={{ fontSize: '0.7rem', fontWeight: 'normal' }}>
                                                            <strong className="text-dark">{nazivVestine(zv.vestina?.id || zv.vestinaId)}</strong> | {formatirajEnum(zv.nivo)} | {formatirajEnum(zv.prioritet)}
                                                        </span>
                                                    ))}
                                                </div>
                                                
                                                <div className="text-end mt-1">
                                                    <button 
                                                        onClick={() => handleVidiPreporuke(oglas)} 
                                                        className={`btn btn-sm py-0 px-2 ${prikazanePreporuke?.oglas.id === oglas.id ? 'btn-warning text-dark fw-bold' : 'btn-outline-primary'}`}
                                                        style={{ fontSize: '0.8rem' }}
                                                    >
                                                        {prikazanePreporuke?.oglas.id === oglas.id ? '👇 Kandidate vidi dole' : 'Vidi kandidate'}
                                                    </button>
                                                </div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>

                    {/* --- DONJI PANELI (Preporuke i Svi Studenti) --- */}
                    {prikazanePreporuke && (
                        <div className="row mt-5 pt-4 border-top animate__animated animate__fadeInUp">
                            <div className="col-12">
                                <div className="card shadow border-0 border-top border-warning border-4">
                                    <div className="card-header bg-white py-3 d-flex justify-content-between align-items-center">
                                        <h4 className="mb-0 text-dark">
                                            Idealni kandidati za poziciju: <span className="text-primary">{prikazanePreporuke.oglas.naslov}</span>
                                        </h4>
                                        <div>
                                            <button 
                                                onClick={() => preuzmiFajl(`http://localhost:8080/api/oglasi/${prikazanePreporuke.oglas.id}/rang-lista/export`, 'json', 'kandidati')}
                                                className="btn btn-sm btn-outline-secondary me-2"
                                                disabled={prikazanePreporuke.kandidati.length === 0}
                                            >📥 JSON</button>
                                            <button 
                                                onClick={() => preuzmiFajl(`http://localhost:8080/api/oglasi/${prikazanePreporuke.oglas.id}/rang-lista/export`, 'xml', 'kandidati')}
                                                className="btn btn-sm btn-outline-secondary me-3"
                                                disabled={prikazanePreporuke.kandidati.length === 0}
                                            >📥 XML</button>
                                            
                                            <button onClick={() => setPrikazanePreporuke(null)} className="btn btn-sm btn-secondary">Zatvori panel</button>
                                        </div>
                                    </div>
                                    <div className="card-body bg-light">
                                        {prikazanePreporuke.kandidati.length > 0 ? (
                                            <div className="table-responsive">
                                                <table className="table table-hover align-middle bg-white shadow-sm rounded">
                                                    <thead className="table-primary text-white">
                                                        <tr>
                                                            <th>Ime i Prezime</th>
                                                            <th>Email adresa</th>
                                                            <th className="text-center">Ukupno Bodova</th>
                                                            <th className="text-center">Akcija</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        {prikazanePreporuke.kandidati.map((stud, sIdx) => {
                                                            // 1. Izvlačimo bodove
                                                            const bodovi = stud.bodovi || stud.ukupniBodovi || 0;
                                                            
                                                            // 2. NOVO: Izvlačimo broj traženih veština iz trenutno otvorenog oglasa
                                                            const brojTrazenihVestina = prikazanePreporuke.oglas?.zahtevaneVestine?.length || 1;
                                                            
                                                            // 3. NOVO: Proslijeđujemo brojTrazenihVestina u funkciju
                                                            const status = odrediStatusKandidata(bodovi, brojTrazenihVestina);

                                                            return (
                                                                <tr key={sIdx} className={status.bojaReda}>
                                                                    {/* Ostatak reda (td elementi) ostaje POTPUNO ISTI */}
                                                                    <td className="py-3">
                                                                        <span className="fw-bold fs-6 text-dark">
                                                                            {status.ikonica} {stud.ime} {stud.prezime}
                                                                        </span>
                                                                        <br />
                                                                        <small className="text-secondary fw-bold">{status.tekst}</small>
                                                                    </td>
                                                                    
                                                                    <td className="text-muted align-middle">
                                                                        📧 {formatirajEmail(stud.id)}
                                                                    </td>
                                                                    
                                                                    <td className="text-center align-middle">
                                                                        <span className={`badge ${status.bojaBadga} fs-6 py-2 px-3 shadow-sm`}>
                                                                            {bodovi.toFixed(1)} bodova {/* Dodato toFixed(1) da lepše izgleda ako ima decimala */}
                                                                        </span>
                                                                    </td>
                                                                    
                                                                    <td className="text-center align-middle">
                                                                        <button onClick={() => prikaziKontakt({ ...stud, email: formatirajEmail(stud.id) })} className="btn btn-sm btn-dark fw-bold px-3">Kontaktiraj</button>
                                                                    </td>
                                                                </tr>
                                                            );
                                                        })}
                                                    </tbody>
                                                </table>
                                            </div>
                                        ) : (
                                            <div className="alert alert-warning text-center py-4 mb-0">
                                                <h5 className="text-dark">Nema savršenih poklapanja.</h5>
                                                <p className="text-muted mb-0">Trenutno nijedan student ne ispunjava sve visoko-prioritetne uslove za ovaj oglas.</p>
                                            </div>
                                        )}
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}

                    <div className="row mt-5 pt-4 border-top">
                        <div className="col-12 mb-3 d-flex justify-content-between align-items-center">
                            <div>
                                <h4 className="text-secondary d-flex align-items-center mb-1">
                                    Tržište kandidata (Aktivni studenti) 
                                    <span className="badge bg-secondary rounded-pill ms-3 fs-6">{aktivniStudenti.length}</span>
                                </h4>
                                <p className="text-muted small mb-0">Ispod je lista svih studenata na platformi koji trenutno aktivno traže zaposlenje.</p>
                            </div>
                            <div>
                                <button 
                                    onClick={() => preuzmiFajl('http://localhost:8080/api/studenti/aktivni/export', 'json', 'svi_kandidati')}
                                    className="btn btn-sm btn-outline-secondary me-2"
                                    disabled={aktivniStudenti.length === 0}
                                >📥 Export JSON</button>
                                <button 
                                    onClick={() => preuzmiFajl('http://localhost:8080/api/studenti/aktivni/export', 'xml', 'svi_kandidati')}
                                    className="btn btn-sm btn-outline-secondary"
                                    disabled={aktivniStudenti.length === 0}
                                >📥 Export XML</button>
                            </div>
                        </div>
                        
                        <div className="col-12">
                            <div className="pe-2" style={{ maxHeight: '400px', overflowY: 'auto' }}>
                                {aktivniStudenti.length === 0 ? (
                                    <div className="alert alert-light border text-center text-muted py-3">Trenutno nema studenata koji aktivno traže posao.</div>
                                ) : (
                                    <div className="d-flex flex-column">
                                        {aktivniStudenti.map((student, idx) => (
                                            <div 
                                                key={student.id || idx} 
                                                className="d-flex justify-content-between align-items-center p-3 mb-2 bg-white border rounded shadow-sm"
                                            >
                                                <div className="fw-bold text-dark d-flex align-items-center" style={{ flex: '1' }}>
                                                    <span className="fs-5 me-3">🎓</span> 
                                                    {student.ime} {student.prezime}
                                                </div>
                                                <div className="text-muted text-center" style={{ flex: '1' }}>
                                                    📧 {student.email || student.id}
                                                </div>
                                                <div className="text-end" style={{ flex: '1' }}>
                                                    <button onClick={() => setDetaljiStudenta(student)} className="btn btn-sm btn-outline-primary fw-bold px-4">
                                                        Pogledaj profil
                                                    </button>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>

                    {detaljiStudenta && (
                        <div className="modal d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.45)' }}>
                            <div className="modal-dialog modal-dialog-centered">
                                <div className="modal-content">
                                    <div className="modal-header">
                                        <h5 className="modal-title">Profil kandidata</h5>
                                        <button type="button" className="btn-close" onClick={() => setDetaljiStudenta(null)} aria-label="Zatvori"></button>
                                    </div>
                                    <div className="modal-body">
                                        <p className="mb-2"><strong>Ime i prezime:</strong> {detaljiStudenta.ime} {detaljiStudenta.prezime}</p>
                                        <p className="mb-2"><strong>Email:</strong> {detaljiStudenta.email || formatirajEmail(detaljiStudenta.id)}</p>
                                        <p className="mb-2"><strong>Nivo studija:</strong> {detaljiStudenta.nivoStudija || 'Nije navedeno'}</p>
                                        <p className="mb-0"><strong>Status:</strong> {detaljiStudenta.traziZaposlenje ? 'Aktivno traži posao' : 'Nije aktivan kandidat'}</p>
                                    </div>
                                    <div className="modal-footer">
                                        <button type="button" className="btn btn-outline-primary" onClick={() => prikaziKontakt(detaljiStudenta)}>Kontaktiraj</button>
                                        <button type="button" className="btn btn-secondary" onClick={() => setDetaljiStudenta(null)}>Zatvori</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}
                </>
            )}
        </div>
    );
}

export default AgencijaDashboard;
