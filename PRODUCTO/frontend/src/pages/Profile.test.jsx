import { render, screen, waitFor } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import Profile from './Profile';

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('Profile Page', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    vi.stubGlobal('fetch', vi.fn());
  });

  it('redirects to login if token is missing', () => {
    render(<Profile />);
    expect(mockNavigate).toHaveBeenCalledWith('/login');
  });

  it('renders profile data when token is valid', async () => {
    localStorage.setItem('token', 'valid-token');

    const fetchMock = fetch;
    fetchMock.mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        nombres: 'María',
        apellidoPaterno: 'González',
        apellidoMaterno: 'Rojas',
        rol: 'Ciudadana',
        rut: '12.345.678-5',
        email: 'maria@correo.cl',
        telefono: '+56912345678',
        direccion: 'Calle Falsa 123',
        comuna: 'Providencia',
      }),
    });

    render(<Profile />);

    expect(await screen.findByText(/María González Rojas/i)).toBeInTheDocument();
    expect(screen.getByText(/Ciudadana/i)).toBeInTheDocument();
    expect(screen.getByText('12.345.678-5')).toBeInTheDocument();
    expect(screen.getByText('maria@correo.cl')).toBeInTheDocument();
    expect(screen.getByText('Calle Falsa 123')).toBeInTheDocument();
    expect(screen.getByText('Providencia')).toBeInTheDocument();
  });
});
