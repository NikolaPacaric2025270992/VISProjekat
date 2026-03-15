import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

function AgencijaDashboard() {
    const navigate = useNavigate();

    const [agencija, setAgencija] = useState(null);
    const [vestine, setVestine] = useState([]);
    const [oglasi, setOglasi] = useState([]); 
    const [rangLista, setRangLista] = useState({}); // Čuvamo liste preporuka po ID-u oglasa
    
    const [naslovOglasa, setNaslovOglasa] = useState('');
    
    // NOVO: Stanje koje čuva niz zahteva (veština, nivo, prioritet)
    const [zahtevi, setZahtevi] = useState([
        { vestinaId: '', nivo: 'POCETNI', prioritet: 'NIZAK' }
    ]);

    useEffect(() => {
        const ulogovanKorisnik = localStorage.getItem('user');
        const rola = localStorage.getItem('role');

        if (!ulogovanKorisnik || rola !== 'agencija') {
            navigate('/prijava');
            return;
        }

        const podaciAgencije = JSON.parse(ulogovanKorisnik);
        setAgencija(podaciAgencije);

        axios.get('http://localhost:8080/api/vestine')
            .then(res => {
                setVestine(res.data);
                // Inicijalno setujemo prvu veštinu na prvi padajući meni
                if(res.data.length > 0) {
                    setZahtevi([{ vestinaId: res.data[0].id, nivo: 'POCETNI', prioritet: 'NIZAK' }]);
                }
            })
            .catch(err => console.error("Greška pri učitavanju veština:", err));

        axios.get(`http://localhost:8080/api/oglasi/agencija/${podaciAgencije.id}`)
            .then(res => setOglasi(res.data))
            .catch(err => console.error("Greška pri učitavanju oglasa:", err));

    }, [navigate]);

    const handleOdjava = () => {
        localStorage.removeItem('user');
        localStorage.removeItem('role');
        navigate('/prijava');
    };

    // NOVO: Funkcije za manipulaciju nizom zahteva
    const handleZahtevChange = (index, polje, vrednost) => {
        const noviZahtevi = [...zahtevi];
        noviZahtevi[index][polje] = vrednost;
        setZahtevi(noviZahtevi);
    };

    const dodajNoviZahtev = () => {
        setZahtevi([...zahtevi, { vestinaId: vestine[0]?.id || '', nivo: 'POCETNI', prioritet: 'NIZAK' }]);
    };

    const ukloniZahtev = (index) => {
        const noviZahtevi = zahtevi.filter((_, i) => i !== index);
        setZahtevi(noviZahtevi);
    };

    const handleDodajOglas = async (e) => {
        e.preventDefault();
        
        // NOVO: Mapiramo niz za backend
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
            // Resetujemo formu na samo jedan početni zahtev
            setZahtevi([{ vestinaId: vestine[0]?.id || '', nivo: 'POCETNI', prioritet: 'NIZAK' }]); 
        } catch (error) {
            console.error(error);
            alert("Greška pri dodavanju oglasa.");
        }
    };

    const handleVidiPreporuke = async (oglasId) => {
        try {
            const res = await axios.get(`http://localhost:8080/api/oglasi/${oglasId}/rang-lista`);
            setRangLista(prev => ({ ...prev, [oglasId]: res.data }));
        } catch (error) {
            console.error("Greška:", error);
        }
    };

    if (!agencija) return <div className="text-center mt-5">Učitavanje...</div>;

    return (
        <div className="container mt-4">
            <div className="d-flex justify-content-between align-items-center mb-4 pb-2 border-bottom">
                <h2 className="text-primary">Panel: {agencija.nazivAgencije}</h2>
                <div>
                    <span className="me-3 fw-bold text-muted">PIB: {agencija.pib} | {agencija.lokacija}</span>
                    <button onClick={handleOdjava} className="btn btn-outline-danger btn-sm">Odjavi se</button>
                </div>
            </div>

            <div className="row">
                {/* Leva kolona: Forma sa dinamičkim veštinama */}
                <div className="col-md-5">
                    <div className="card shadow-sm border-0">
                        <div className="card-header bg-primary text-white py-3">
                            <h5 className="mb-0">Kreiraj novi oglas</h5>
                        </div>
                        <div className="card-body">
                            <form onSubmit={handleDodajOglas}>
                                <div className="mb-4">
                                    <label className="form-label fw-bold">Naslov pozicije:</label>
                                    <input type="text" className="form-control" value={naslovOglasa} onChange={(e) => setNaslovOglasa(e.target.value)} required placeholder="npr. Full Stack Developer" />
                                </div>

                                <div className="d-flex justify-content-between align-items-center border-bottom pb-2 mb-3">
                                    <h6 className="mb-0 fw-bold text-secondary">Potrebne veštine</h6>
                                    <button type="button" onClick={dodajNoviZahtev} className="btn btn-sm btn-outline-primary">+ Dodaj veštinu</button>
                                </div>
                                
                                {/* Dinamičko iscrtavanje niza zahteva */}
                                {zahtevi.map((zahtev, index) => (
                                    <div key={index} className="p-3 mb-3 bg-light border rounded position-relative">
                                        {zahtevi.length > 1 && (
                                            <button type="button" onClick={() => ukloniZahtev(index)} className="btn btn-sm btn-danger position-absolute top-0 end-0 m-1">X</button>
                                        )}
                                        
                                        <div className="mb-2">
                                            <label className="form-label small fw-bold">Veština:</label>
                                            <select 
                                                className="form-select form-select-sm" 
                                                value={zahtev.vestinaId} 
                                                onChange={(e) => handleZahtevChange(index, 'vestinaId', e.target.value)}
                                            >
                                                <option value="" disabled>Izaberite veštinu...</option>
                                                {vestine.map(v => (
                                                    <option key={v.id} value={v.id}>{v.naziv}</option>
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
                        <div className="alert alert-info text-center">Trenutno nemate aktivnih oglasa. Napravite svoj prvi oglas levo!</div>
                    ) : (
                        <div className="row">
                            {oglasi.map((oglas, idx) => (
                                <div key={oglas.id || `oglas_${idx}`} className="col-12 mb-3">
                                    <div className="card shadow-sm border-start border-primary border-4">
                                        <div className="card-body">
                                            <h5 className="card-title text-primary mb-3">{oglas.naslov}</h5>
                                            
                                            {/* Prikaz zahtevanih veština */}
                                            {oglas.zahtevaneVestine && oglas.zahtevaneVestine.map((zv, idx) => (
                                                <span key={idx} className="badge bg-light text-dark border me-2 mb-2 p-2">
                                                    <strong>{zv.vestina.id}</strong> | {zv.nivo} | {zv.prioritet}
                                                </span>
                                            ))}
                                            
                                            <div className="mt-3 text-end">
                                                <button onClick={() => handleVidiPreporuke(oglas.id)} className="btn btn-sm btn-outline-success fw-bold">
                                                    Vidi preporučene studente
                                                </button>
                                            </div>

                                            {/* Prikaz rang liste za ovaj oglas */}
                                            {rangLista[oglas.id] && (
                                                <div className="mt-3 animate__animated animate__fadeIn">
                                                    <h6 className="text-muted border-bottom pb-1">Najbolji kandidati:</h6>
                                                    {rangLista[oglas.id].length > 0 ? (
                                                        <table className="table table-sm table-borderless align-middle">
                                                            <thead className="table-light">
                                                                <tr style={{ fontSize: '0.85rem' }}>
                                                                    <th>Ime i Prezime</th>
                                                                    <th className="text-center">Bodovi</th>
                                                                </tr>
                                                            </thead>
                                                            <tbody>
                                                                {rangLista[oglas.id].map((stud, sIdx) => (
                                                                    <tr key={sIdx} style={{ fontSize: '0.9rem' }}>
                                                                        <td>{stud.ime} {stud.prezime}</td>
                                                                        <td className="text-center">
                                                                            <span className="badge bg-warning text-dark fs-6">{stud.bodovi || stud.ukupniBodovi}</span>
                                                                        </td>
                                                                    </tr>
                                                                ))}
                                                            </tbody>
                                                        </table>
                                                    ) : (
                                                        <div className="alert alert-light text-center py-2 text-muted">Nema kandidata koji ispunjavaju ove uslove.</div>
                                                    )}
                                                </div>
                                            )}

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