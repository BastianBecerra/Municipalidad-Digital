import Navbar from '../components/Navbar';
import HeroSection from '../components/HeroSection';
import ProcedureCards from '../components/ProcedureCards';
import MapSection from '../components/MapSection';
import Footer from '../components/Footer';

// Componente Home: Actúa como el contenedor principal de la página de inicio.
// Utiliza una arquitectura basada en componentes para ensamblar la interfaz de forma modular.
const Home = () => {
  return (
    <>
      {/* Barra de navegación constante en la parte superior */}
      <Navbar /> 
      
      <main>
        {/* Sección Hero: Bienvenida y llamados a la acción */}
        <HeroSection />
        
        {/* Sección de Trámites: Grid con servicios municipales */}
        <ProcedureCards />
        
        {/* Sección de Mapa: Ubicación de notarías */}
        <MapSection />
      </main>
      
      {/* Pie de página con enlaces útiles y contacto */}
      <Footer />
    </>
  );
};

export default Home;