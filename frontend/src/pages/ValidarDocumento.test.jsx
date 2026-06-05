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
  Html5Qrcode: {
    getCameras: vi.fn(),
  },
}));

describe('ValidarDocumento Page', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.stubGlobal('fetch', vi.fn());
  });

  it('renders validation menu when no result is requested', () => {
    params = { hash: undefined };

    render(<ValidarDocumento />);

    expect(screen.getByText(/Selecciona el método de validación/i)).toBeInTheDocument();
    expect(screen.getByText(/📁 Cargar Archivo/i)).toBeInTheDocument();
  });

  it('validates a document using a hash from the URL and shows success result', async () => {
    params = { hash: '0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef' };
    const fetchMock = fetch;
    fetchMock.mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        valido: true,
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
