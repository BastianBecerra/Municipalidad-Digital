import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Home from './pages/Home';
import Login from './pages/Login';
import Contact from './pages/Contact';
import Profile from './pages/Profile';
import BlockchainTest from './pages/BlockchainTest';
import ValidarDocumento from './pages/ValidarDocumento';
import GenerarDocumento from './pages/GenerarDocumento';
import JuntasVecinos from './pages/JuntasVecinos';
import AccessibilityMenu from './components/AccessibilityMenu';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/contacto" element={<Contact />} />
        <Route path="/perfil" element={<Profile />} />
        <Route path="/blockchain-test" element={<BlockchainTest />} />
        <Route path="/tramites/nuevo" element={<GenerarDocumento />} />
        <Route path="/junta-vecinos" element={<JuntasVecinos />} />
        <Route path="/validar" element={<ValidarDocumento />} />
        <Route path="/validar/:hash" element={<ValidarDocumento />} />
      </Routes>
      <AccessibilityMenu />
    </Router>
  );
}

export default App;
