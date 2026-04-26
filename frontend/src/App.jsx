import React from 'react';
import Navbar from './components/Navbar';
import HeroSection from './components/HeroSection';
import ProcedureCards from './components/ProcedureCards';
import MapSection from './components/MapSection';
import Footer from './components/Footer';

function App() {
  return (
    <>
      <Navbar />
      <main>
        <HeroSection />
        <ProcedureCards />
        <MapSection />
      </main>
      <Footer />
    </>
  );
}

export default App;
