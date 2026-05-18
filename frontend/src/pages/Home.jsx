import Navbar from '../components/Navbar';
import HeroSection from '../components/HeroSection';
import ProcedureCards from '../components/ProcedureCards';
import MapSection from '../components/MapSection';
import Footer from '../components/Footer';

const Home = () => {
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
};

export default Home;
