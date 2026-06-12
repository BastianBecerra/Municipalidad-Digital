import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import JuntasVecinos from './JuntasVecinos';

// Mock components to isolate the page tests
vi.mock('../components/Navbar', () => ({
  default: () => <div data-testid="navbar">Navbar Mock</div>
}));
vi.mock('../components/Footer', () => ({
  default: () => <div data-testid="footer">Footer Mock</div>
}));

// Mock useNavigate
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('JuntasVecinos Page', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.stubGlobal('fetch', vi.fn());
    localStorage.clear();
    localStorage.setItem('token', 'mock_token');
  });

  it('renders loading state initially', async () => {
    fetch.mockImplementation(() => new Promise(() => {})); // Never resolves
    
    render(
      <MemoryRouter>
        <JuntasVecinos />
      </MemoryRouter>
    );

    expect(screen.getByText('Cargando información municipal...')).toBeInTheDocument();
  });

  it('renders active Junta de Vecinos info when user belongs to one', async () => {
    fetch.mockImplementation((url) => {
      if (url === '/usuarios/me') {
        return Promise.resolve({
          status: 200,
          ok: true,
          json: async () => ({
            id: 1,
            rut: '19.123.456-1',
            nombres: 'Juan',
            apellidoPaterno: 'Pérez',
            rol: 'VECINO',
            territorio: {
              id: 4,
              nombre: 'Junta de Vecinos Villa Los Aromos',
              numeroUnidadVecinal: 'UV-01',
              presidente: 'Carmen Gloria Muñoz',
              direccionSede: 'Av. Los Aromos 1250',
              telefono: '+56222341100',
              email: 'jjvv.losaromos@providencia.cl',
              descripcion: 'Junta del sector norte',
              limiteNorte: 'Av. Providencia',
              limiteSur: 'Calle Los Olmos',
              limiteEste: 'Av. Irarrázaval',
              limiteOeste: 'Av. Pedro de Valdivia'
            }
          })
        });
      }
      if (url === '/territorios') {
        return Promise.resolve({
          status: 200,
          ok: true,
          json: async () => [
            { id: 4, nombre: 'Junta de Vecinos Villa Los Aromos', comuna: 'Providencia', presidente: 'Carmen Gloria Muñoz' },
            { id: 5, nombre: 'Junta de Vecinos Los Quillayes', comuna: 'Providencia', presidente: 'Roberto Fuentes' }
          ]
        });
      }
      return Promise.resolve({ status: 404, ok: false });
    });

    render(
      <MemoryRouter>
        <JuntasVecinos />
      </MemoryRouter>
    );

    // Wait for loader to disappear and data to render
    await waitFor(() => {
      expect(screen.getByText('Tu Junta de Vecinos Activa')).toBeInTheDocument();
    });

    expect(screen.getAllByText('Junta de Vecinos Villa Los Aromos').length).toBeGreaterThan(0);
    expect(screen.getByText('Unidad Vecinal: UV-01')).toBeInTheDocument();
    expect(screen.getAllByText('Carmen Gloria Muñoz').length).toBeGreaterThan(0);
    expect(screen.getByText('Av. Los Aromos 1250')).toBeInTheDocument();

    // Verify directory has both items
    expect(screen.getByText('Directorio Municipal de Organizaciones')).toBeInTheDocument();
    expect(screen.getByText('Junta de Vecinos Los Quillayes')).toBeInTheDocument();
  });

  it('renders notice if user does not belong to any Junta de Vecinos', async () => {
    fetch.mockImplementation((url) => {
      if (url === '/usuarios/me') {
        return Promise.resolve({
          status: 200,
          ok: true,
          json: async () => ({
            id: 1,
            rut: '19.123.456-1',
            rol: 'VECINO',
            territorio: null
          })
        });
      }
      if (url === '/territorios') {
        return Promise.resolve({
          status: 200,
          ok: true,
          json: async () => []
        });
      }
      return Promise.resolve({ status: 404, ok: false });
    });

    render(
      <MemoryRouter>
        <JuntasVecinos />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/Sin Junta de Vecinos Asignada/i)).toBeInTheDocument();
    });
  });

  it('filters neighborhood directory correctly', async () => {
    fetch.mockImplementation((url) => {
      if (url === '/usuarios/me') {
        return Promise.resolve({ status: 401, ok: false });
      }
      if (url === '/territorios') {
        return Promise.resolve({
          status: 200,
          ok: true,
          json: async () => [
            { id: 4, nombre: 'Junta de Vecinos Villa Los Aromos', comuna: 'Providencia', presidente: 'Carmen' },
            { id: 5, nombre: 'Junta de Vecinos Los Quillayes', comuna: 'Ñuñoa', presidente: 'Roberto' }
          ]
        });
      }
      return Promise.resolve({ status: 404, ok: false });
    });

    render(
      <MemoryRouter>
        <JuntasVecinos />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Junta de Vecinos Villa Los Aromos')).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText('Buscar por nombre, comuna o presidente...');
    
    // Search for Ñuñoa
    fireEvent.change(searchInput, { target: { value: 'Ñuñoa' } });

    expect(screen.queryByText('Junta de Vecinos Villa Los Aromos')).not.toBeInTheDocument();
    expect(screen.getByText('Junta de Vecinos Los Quillayes')).toBeInTheDocument();
  });
});
