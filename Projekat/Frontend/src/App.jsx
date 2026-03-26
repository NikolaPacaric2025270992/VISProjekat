import { Routes, Route, Navigate } from 'react-router-dom';
import Prijava from './pages/Prijava';
import Registracija from './pages/Registracija';
import AgencijaDashboard from './pages/AgencijaDashboard';
import StudentDashboard from './pages/StudentDashboard';

function App() {
  return (
    <Routes>
      <Route path="/prijava" element={<Prijava />} />
      <Route path="/registracija" element={<Registracija />} />
      <Route path="/student-dashboard" element={<StudentDashboard />} />
      <Route path="/agencija-dashboard" element={<AgencijaDashboard />} />
      <Route path="/" element={<Navigate to="/prijava" />} />
    </Routes>
  );
}

export default App;