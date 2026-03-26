import { useState } from 'react';
import axios from 'axios';
import { useNavigate, Link } from 'react-router-dom';

function Registracija() {
    const [tip, setTip] = useState('student');
    const [email, setEmail] = useState('');
    const [lozinka, setLozinka] = useState('');
    
    // Polja za Studenta
    const [ime, setIme] = useState('');
    const [prezime, setPrezime] = useState('');
    const [nivoStudija, setNivoStudija] = useState('Osnovne');
    const [traziZaposlenje, setTraziZaposlenje] = useState(true);
    
    // Polja za Agenciju
    const [nazivAgencije, setNazivAgencije] = useState('');
    const [pib, setPib] = useState('');
    const [lokacija, setLokacija] = useState('');

    const navigate = useNavigate();

    const handleRegister = async (e) => {
        e.preventDefault();
        const generisaniId = email.replace(/[@.]/g, '_'); 

        let url = tip === 'student' 
            ? 'http://localhost:8080/api/studenti/registracija' 
            : 'http://localhost:8080/api/agencija/registracija';

        let payload = tip === 'student' 
            ? { id: generisaniId, ime, prezime, email, lozinka, nivoStudija, traziZaposlenje }
            : { id: generisaniId, nazivAgencije, pib, lokacija, email, lozinka };

        try {
            await axios.post(url, payload);
            alert('Uspešna registracija! Možete se prijaviti.');
            navigate('/prijava');
        } catch (error) {
            alert('Greška pri registraciji. Moguće je da email već postoji.');
        }
    };

    return (
        <div className="container vh-100 d-flex justify-content-center align-items-center">
            <div className="card shadow p-4" style={{ width: '450px' }}>
                <h2 className="text-center mb-4 text-success">Registracija</h2>
                <form onSubmit={handleRegister}>
                    <div className="mb-3">
                        <label className="form-label fw-bold">Tip naloga:</label>
                        <select className="form-select border-success" value={tip} onChange={(e) => setTip(e.target.value)}>
                            <option value="student">Student</option>
                            <option value="agencija">Agencija za zapošljavanje</option>
                        </select>
                    </div>

                    <hr />

                    {tip === 'student' ? (
                        <>
                            <div className="row mb-3">
                                <div className="col"><label>Ime:</label><input type="text" className="form-control" onChange={(e) => setIme(e.target.value)} required /></div>
                                <div className="col"><label>Prezime:</label><input type="text" className="form-control" onChange={(e) => setPrezime(e.target.value)} required /></div>
                            </div>
                            <div className="mb-3">
                                <label>Nivo studija:</label>
                                <select className="form-select" value={nivoStudija} onChange={(e) => setNivoStudija(e.target.value)}>
                                    <option value="Osnovne">Osnovne studije</option>
                                    <option value="Master">Master studije</option>
                                    <option value="Doktorske">Doktorske studije</option>
                                </select>
                            </div>
                            <div className="mb-3 form-check">
                                <input type="checkbox" className="form-check-input" checked={traziZaposlenje} onChange={(e) => setTraziZaposlenje(e.target.checked)} id="check" />
                                <label className="form-check-label" htmlFor="check">Tražim posao</label>
                            </div>
                        </>
                    ) : (
                        <>
                            <div className="mb-3"><label>Naziv agencije:</label><input type="text" className="form-control" onChange={(e) => setNazivAgencije(e.target.value)} required /></div>
                            <div className="row mb-3">
                                <div className="col"><label>PIB:</label><input type="text" className="form-control" onChange={(e) => setPib(e.target.value)} required /></div>
                                <div className="col"><label>Lokacija:</label><input type="text" className="form-control" onChange={(e) => setLokacija(e.target.value)} required /></div>
                            </div>
                        </>
                    )}

                    <div className="mb-3"><label>Email:</label><input type="email" className="form-control" onChange={(e) => setEmail(e.target.value)} required /></div>
                    <div className="mb-4"><label>Lozinka:</label><input type="password" className="form-control" onChange={(e) => setLozinka(e.target.value)} required /></div>

                    <button type="submit" className="btn btn-success w-100 mb-3">Registruj se</button>
                    <div className="text-center">
                        <small>Imate nalog? <Link to="/prijava">Prijavite se ovde.</Link></small>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default Registracija;