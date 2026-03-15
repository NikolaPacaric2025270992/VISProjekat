import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

function StudentDashboard() {
    const navigate = useNavigate();
    const [student, setStudent] = useState(null);
    const [predmeti, setPredmeti] = useState([]);
    
    // Stanja za Search
    const [searchQuery, setSearchQuery] = useState('');
    const [filtriraniPredmeti, setFiltriraniPredmeti] = useState([]);
    const [prikaziDropdown, setPrikaziDropdown] = useState(false);
    
    const [izabranPredmet, setIzabranPredmet] = useState(null); 
    const [ocena, setOcena] = useState(6);
    const [polaganja, setPolaganja] = useState([]);

    useEffect(() => {
        const ulogovanKorisnik = localStorage.getItem('user');
        const rola = localStorage.getItem('role');
        if (!ulogovanKorisnik || rola !== 'student') { navigate('/prijava'); return; }

        setStudent(JSON.parse(ulogovanKorisnik));

        axios.get('http://localhost:8080/api/predmeti')
            .then(res => setPredmeti(res.data))
            .catch(err => console.error("Greška:", err));
    }, [navigate]);

    const handleOdjava = () => {
        localStorage.removeItem('user');
        localStorage.removeItem('role');
        navigate('/prijava');
    };

    const handleSearchChange = (e) => {
        const query = e.target.value;
        setSearchQuery(query);
        if (query.length > 1) {
            const filtrirano = predmeti.filter(p => 
                p.nazivPredmeta.toLowerCase().includes(query.toLowerCase())
            );
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

        const novoPolaganje = {
            id: `polaganje_${Date.now()}`,
            studentId: student.id,
            predmetId: izabranPredmet.id,
            ocena: parseInt(ocena)
        };

        try {
            await axios.post('http://localhost:8080/api/polaganja/dodaj', novoPolaganje);
            alert("Ispit uspešno evidentiran!");
            setPolaganja([...polaganja, { ...novoPolaganje, nazivPredmeta: izabranPredmet.nazivPredmeta }]);
            setSearchQuery('');
            setIzabranPredmet(null);
        } catch (error) { alert("Greška pri čuvanju."); }
    };

    if (!student) return <div className="text-center mt-5">Učitavanje...</div>;

    return (
        <div className="container mt-4">
            {/* Navigacioni bar (Sada identičan kao kod Agencije) */}
            <div className="d-flex justify-content-between align-items-center mb-4 pb-2 border-bottom">
                <h2 className="text-success">Profil: {student.ime} {student.prezime}</h2>
                <div>
                    <span className="me-3 fw-bold text-muted">Status: {student.traziZaposlenje ? 'Traži posao 🟢' : 'Nije aktivan 🔴'}</span>
                    <button onClick={handleOdjava} className="btn btn-outline-danger btn-sm">Odjavi se</button>
                </div>
            </div>
            
            <div className="row">
                {/* Leva kolona: Forma */}
                <div className="col-md-5">
                    <div className="card shadow-sm border-0">
                        <div className="card-header bg-success text-white py-3">
                            <h5 className="mb-0">Evidentiraj novi ispit</h5>
                        </div>
                        <div className="card-body">
                            <form onSubmit={handleDodajPolaganje} className="position-relative">
                                <label className="form-label fw-bold">Pretraži predmet:</label>
                                <input 
                                    type="text" 
                                    className="form-control" 
                                    placeholder="Kucaj naziv predmeta..."
                                    value={searchQuery}
                                    onChange={handleSearchChange}
                                />
                                
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
                                    polaganja.map(p => (
                                        <tr key={p.id}>
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
        </div>
    );
}

export default StudentDashboard;