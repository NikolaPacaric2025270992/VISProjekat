import { useState } from 'react';
import axios from 'axios';
import { useNavigate, Link } from 'react-router-dom';

function Prijava() {
    const [email, setEmail] = useState('');
    const [lozinka, setLozinka] = useState('');
    const [tip, setTip] = useState('student');
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        const url = `http://localhost:8080/api/${tip === 'student' ? 'studenti' : 'agencija'}/login`;
        
        try {
            const res = await axios.post(url, { email, lozinka });
            localStorage.setItem('user', JSON.stringify(res.data));
            localStorage.setItem('role', tip);
            navigate(`/${tip}-dashboard`);
        } catch (err) {
            alert("Pogrešan email ili lozinka!");
        }
    };

    return (
        <div className="container vh-100 d-flex justify-content-center align-items-center">
            <div className="card shadow p-4" style={{ width: '400px' }}>
                <h2 className="text-center mb-4 text-primary">Prijava</h2>
                <form onSubmit={handleLogin}>
                    <div className="mb-3">
                        <label>Prijavljujem se kao:</label>
                        <select className="form-select border-primary" value={tip} onChange={(e) => setTip(e.target.value)}>
                            <option value="student">Student</option>
                            <option value="agencija">Agencija</option>
                        </select>
                    </div>
                    <div className="mb-3">
                        <label>Email:</label>
                        <input type="email" className="form-control" value={email} onChange={(e) => setEmail(e.target.value)} required />
                    </div>
                    <div className="mb-3">
                        <label>Lozinka:</label>
                        <input type="password" className="form-control" value={lozinka} onChange={(e) => setLozinka(e.target.value)} required />
                    </div>
                    <button type="submit" className="btn btn-primary w-100">Prijavi se</button>
                    <div className="text-center mt-3">
                        <small>Nemate nalog? <Link to="/registracija">Registrujte se ovde.</Link></small>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default Prijava;