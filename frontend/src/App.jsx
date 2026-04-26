import React from 'react';
import Navbar from './components/Navbar';
import HeroSection from './components/HeroSection';
import ProcedureCards from './components/ProcedureCards';
import Footer from './components/Footer';

function App() {
  return (
    <>
      <Navbar />
      <main>
        <HeroSection />
        <ProcedureCards />
      </main>
      <Footer />
    </>
  );
}

export default App;
