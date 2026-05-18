import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Home from './pages/Home';
import Login from './pages/Login';
import Contact from './pages/Contact';
import Profile from './pages/Profile';
import BlockchainTest from './pages/BlockchainTest';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/contacto" element={<Contact />} />
        <Route path="/perfil" element={<Profile />} />
        <Route path="/blockchain" element={<BlockchainTest />} />
      </Routes>
    </Router>
  );
}

export default App;
