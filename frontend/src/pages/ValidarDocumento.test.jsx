import { render, screen, waitFor } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import ValidarDocumento from './ValidarDocumento';

const mockNavigate = vi.fn();
let params = { hash: undefined };
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
    useParams: () => params,
  };
});

vi.mock('../components/Navbar', () => ({
  default: () => <div>Navbar Mock</div>,
}));
vi.mock('../components/Footer', () => ({
  default: () => <div>Footer Mock</div>,
}));

vi.mock('html5-qrcode', () => ({
  Html5Qrcode: vi.fn().mockImplementation(() => ({
    scanFile: vi.fn(),
  })),
}));

describe('ValidarDocumento Page', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.stubGlobal('fetch', vi.fn());
    // Establecer un token mock que decodifique con rol ADMIN
    // Payload: {"rol":"ADMIN","sub":"14.567.890-3"}
    localStorage.setItem('token', 'header.eyJyb2wiOiJBRE1JTiIsInN1YiI6IjE0LjU2Ny44OTAtMyJ9.signature');
  });

  it('renders validation menu when no result is requested', () => {
    params = { hash: undefined };

    render(<ValidarDocumento />);

    expect(screen.getByText(/Selecciona el método de validación/i)).toBeInTheDocument();
    expect(screen.getByText(/Cargar Documento/i)).toBeInTheDocument();
  });

  it('renders access denied if role is VECINO', () => {
    params = { hash: undefined };
    // Cambiar token a rol VECINO
    // Payload: {"rol":"VECINO","sub":"12.345.678-9"}
    localStorage.setItem('token', 'header.eyJyb2wiOiJWRUNJTk8iLCJzdWIiOiIxMi4zNDUuNjc4LTkifQ.signature');

    render(<ValidarDocumento />);

    expect(screen.getByText(/No tienes permisos para validar documentos/i)).toBeInTheDocument();
    expect(screen.getByText(/Acceso Denegado/i)).toBeInTheDocument();
  });

  it('validates a document using a hash from the URL and shows success result', async () => {
    params = { hash: '0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef' };
    const fetchMock = fetch;
    fetchMock.mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        valido: true,
        motivo: '¡Documento Verificado!',
        documento: { titulo: 'Certificado de Residencia' },
        blockchain: { hash: '0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef' },
      }),
    });

    render(<ValidarDocumento />);

    expect(await screen.findByText(/Documento Auténtico/i)).toBeInTheDocument();
    expect(screen.getByText(/Certificado de Residencia/i)).toBeInTheDocument();
    expect(screen.getByText(/Registro Criptográfico/i)).toBeInTheDocument();
    expect(screen.getByText(/Footer Mock/i)).toBeInTheDocument();
  });
});
