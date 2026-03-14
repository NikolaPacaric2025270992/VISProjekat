import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

function AgencijaDashboard() {
    const navigate = useNavigate();

    // Osnovna stanja
    const [agencija, setAgencija] = useState(null);
    const [vestine, setVestine] = useState([]);
    const [oglasi, setOglasi] = useState([]); // NOVO: Stanje za listu oglasa
    
    // Stanje za formu novog oglasa (ispravljeno u prazan string umesto null)
    const [naslovOglasa, setNaslovOglasa] = useState('');
    const [izabranaVestina, setIzabranaVestina] = useState(''); 
    const [izabranNivo, setIzabranNivo] = useState('POCETNI');
    const [izabranPrioritet, setIzabranPrioritet] = useState('NIZAK'); // Ispravljen ENUM

    useEffect(() => {
        const ulogovanKorisnik = localStorage.getItem('user');
        const rola = localStorage.getItem('role');

        if (!ulogovanKorisnik || rola !== 'agencija') {
            navigate('/prijava');
            return;
        }

        const podaciAgencije = JSON.parse(ulogovanKorisnik);
        setAgencija(podaciAgencije);

        // 1. Povlačimo sve veštine
        axios.get('http://localhost:8080/api/vestine')
            .then(res => {
                setVestine(res.data);
                if(res.data.length > 0) setIzabranaVestina(res.data[0].id);
            })
            .catch(err => console.error("Greška pri učitavanju veština:", err));

        // 2. NOVO: Povlačimo sve oglase za ovu agenciju
        console.log("Tražim oglase za agenciju sa ID:", podaciAgencije.id); // <--- DODAJ OVO
        
        axios.get(`http://localhost:8080/api/oglasi/agencija/${podaciAgencije.id}`)
            .then(res => {
                console.log("Stigli oglasi iz baze:", res.data); // <--- I OVO
                setOglasi(res.data);
            })
            .catch(err => console.error("Greška pri učitavanju oglasa:", err));

    }, [navigate]);

    const handleOdjava = () => {
        localStorage.removeItem('user');
        localStorage.removeItem('role');
        navigate('/prijava');
    };

    const handleDodajOglas = async (e) => {
        e.preventDefault();
        
        const noviOglas = {
            id: `oglas_${Date.now()}`,
            naslov: naslovOglasa,
            agencijaId: agencija.id,
            zahtevaneVestine: [ // Promenjeno u 'zahtevaneVestine' da se gađa sa tvojim Java modelom!
                {
                    vestina: { id: izabranaVestina },
                    nivo: izabranNivo,
                    prioritet: izabranPrioritet
                }
            ]
        };

        try {
            await axios.post('http://localhost:8080/api/oglasi/postavi', noviOglas);
            alert("Oglas je uspešno kreiran!");
            
            // Dodajemo novi oglas direktno u UI da ne moramo da osvežavamo stranicu
            setOglasi([...oglasi, noviOglas]); 
            setNaslovOglasa(''); 
        } catch (error) {
            console.error(error);
            alert("Greška pri dodavanju oglasa.");
        }
    };

    const handleVidiPreporuke = async (oglasId) => {
        try {
            console.log(`Tražim preporuke za oglas: ${oglasId}...`);
            // Gađamo tvoju rutu u OglasController-u
            const res = await axios.get(`http://localhost:8080/api/oglasi/${oglasId}/rang-lista`);
            
            console.log("Stigli su preporučeni studenti:", res.data);
            
            if(res.data.length === 0) {
                alert("Trenutno nema studenata koji odgovaraju ovom oglasu.");
            } else {
                alert(`Pronađeno ${res.data.length} kandidata! Pogledaj konzolu za detalje.`);
            }
        } catch (error) {
            console.error("Greška pri povlačenju preporuka:", error);
            alert("Došlo je do greške pri rangiranju studenata.");
        }
    };

    if (!agencija) return <div className="text-center mt-5">Učitavanje...</div>;

    return (
        <div className="container mt-4">
            {/* Navigacioni bar */}
            <div className="d-flex justify-content-between align-items-center mb-4 pb-2 border-bottom">
                <h2 className="text-primary">Panel: {agencija.nazivAgencije}</h2>
                <div>
                    <span className="me-3 fw-bold text-muted">PIB: {agencija.pib} | {agencija.lokacija}</span>
                    <button onClick={handleOdjava} className="btn btn-outline-danger btn-sm">Odjavi se</button>
                </div>
            </div>

            <div className="row">
                {/* Leva kolona: Forma */}
                <div className="col-md-5">
                    <div className="card shadow-sm">
                        <div className="card-header bg-primary text-white">
                            <h5 className="mb-0">Kreiraj novi oglas</h5>
                        </div>
                        <div className="card-body">
                            <form onSubmit={handleDodajOglas}>
                                <div className="mb-3">
                                    <label className="form-label">Naslov pozicije:</label>
                                    <input type="text" className="form-control" value={naslovOglasa} onChange={(e) => setNaslovOglasa(e.target.value)} required placeholder="npr. Junior Java Developer" />
                                </div>

                                <h6 className="mt-4 border-bottom pb-2">Glavni zahtev (Veština)</h6>
                                
                                <div className="mb-3">
                                    <label className="form-label">Potrebna veština:</label>
                                    <select 
                                        className="form-select" 
                                        value={izabranaVestina || ""} // Osiguravamo da NIKAD nije null
                                        onChange={(e) => setIzabranaVestina(e.target.value)}
                                    >
                                        {/* Privremena opcija dok se ne učitaju podaci sa bekenda */}
                                        <option value="" disabled>Izaberite veštinu...</option>
                                        
                                        {vestine.map(v => (
                                            <option key={v.id} value={v.id}>{v.naziv}</option>
                                        ))}
                                    </select>
                                </div>

                                <div className="row mb-3">
                                    <div className="col">
                                        <label className="form-label">Traženi nivo:</label>
                                        <select className="form-select" value={izabranNivo} onChange={(e) => setIzabranNivo(e.target.value)}>
                                            <option value="POCETNI">Početni</option>
                                            <option value="SREDNJI">Srednji</option>
                                            <option value="NAPREDNI">Napredni</option>
                                        </select>
                                    </div>
                                    <div className="col">
                                        <label className="form-label">Prioritet:</label>
                                        <select className="form-select" value={izabranPrioritet} onChange={(e) => setIzabranPrioritet(e.target.value)}>
                                            <option value="NIZAK">Nizak</option>
                                            <option value="VISOK">Visok</option>
                                        </select>
                                    </div>
                                </div>

                                <button type="submit" className="btn btn-primary w-100 mt-3">Objavi oglas</button>
                            </form>
                        </div>
                    </div>
                </div>

                {/* Desna kolona: Lista aktivnih oglasa */}
                <div className="col-md-7">
                    <h4 className="mb-3 text-secondary">Moji aktivni oglasi</h4>
                    
                    {oglasi.length === 0 ? (
                        <div className="alert alert-info text-center">Trenutno nemate aktivnih oglasa. Napravite svoj prvi oglas levo!</div>
                    ) : (
                        <div className="row">
                            {oglasi.map(oglas => (
                                <div key={oglas.id} className="col-12 mb-3">
                                    <div className="card shadow-sm border-start border-primary border-4">
                                        <div className="card-body">
                                            <h5 className="card-title text-primary mb-3">{oglas.naslov}</h5>
                                            
                                            {/* Prikaz zahtevanih veština ako postoje */}
                                            {oglas.zahtevaneVestine && oglas.zahtevaneVestine.map((zv, idx) => (
                                                <span key={idx} className="badge bg-light text-dark border me-2 p-2">
                                                    <strong>Veština ID:</strong> {zv.vestina.id} | <strong>Nivo:</strong> {zv.nivo} | <strong>Prioritet:</strong> {zv.prioritet}
                                                </span>
                                            ))}
                                            
                                            <div className="mt-3 text-end">
                                                <button onClick={() => handleVidiPreporuke(oglas.id)} className="btn btn-sm btn-outline-success">
                                                    Vidi preporučene studente
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

export default AgencijaDashboard;