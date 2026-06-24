import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import GenerarDocumento from './GenerarDocumento';

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

describe('GenerarDocumento Page', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.stubGlobal('fetch', vi.fn());
    localStorage.clear();
    localStorage.setItem('token', 'mock_token');
    
    // Default mock for profile endpoint
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
            apellidoMaterno: '',
            direccion: 'Av. Siempre Viva 742',
            comuna: 'Providencia',
            rol: 'VECINO'
          })
        });
      }
      return Promise.resolve({ status: 404, ok: false });
    });
  });

  it('renders all form fields for default residency trámite', async () => {
    render(
      <MemoryRouter>
        <GenerarDocumento />
      </MemoryRouter>
    );

    // Wait for profile fetch to complete and populate form
    await waitFor(() => {
      expect(screen.getByLabelText('RUT del Vecino')).toHaveValue('19.123.456-1');
    });

    expect(screen.getByLabelText('Tipo de Trámite')).toBeInTheDocument();
    expect(screen.getByLabelText('Nombre Completo')).toHaveValue('Juan Pérez');
    expect(screen.getByLabelText('Dirección de Residencia')).toHaveValue('Av. Siempre Viva 742');
    expect(screen.getByLabelText('Comuna')).toHaveValue('Providencia');
    expect(screen.getByLabelText('Motivo del Trámite')).toBeInTheDocument();
  });

  it('hides comuna field when switching to salvoconducto', async () => {
    render(
      <MemoryRouter>
        <GenerarDocumento />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByLabelText('RUT del Vecino')).toHaveValue('19.123.456-1');
    });

    const select = screen.getByLabelText('Tipo de Trámite');
    fireEvent.change(select, { target: { value: 'salvoconducto' } });

    expect(screen.queryByLabelText('Comuna')).not.toBeInTheDocument();
    // In salvoconducto, the label changes to "Dirección Destino"
    expect(screen.getByLabelText('Dirección Destino')).toHaveValue('');
  });

  it('validates empty inputs and displays error messages', async () => {
    render(
      <MemoryRouter>
        <GenerarDocumento />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByLabelText('RUT del Vecino')).toHaveValue('19.123.456-1');
    });

    const submitBtn = screen.getByRole('button', { name: /Solicitar Documento/i });
    fireEvent.click(submitBtn);

    // Only "Motivo" should show error because other fields are pre-populated
    expect(await screen.findByText('El motivo es obligatorio.')).toBeInTheDocument();
  });

  it('successfully submits form for residency and displays success message', async () => {
    const mockSuccessResponse = {
      id: 12345,
      estado: 'BORRADOR',
      estadoBlockchain: 'PENDIENTE',
    };

    fetch.mockImplementation((url, config) => {
      if (url === '/usuarios/me') {
        return Promise.resolve({
          status: 200,
          ok: true,
          json: async () => ({
            id: 1,
            rut: '19.123.456-1',
            nombres: 'Juan',
            apellidoPaterno: 'Pérez',
            apellidoMaterno: '',
            direccion: 'Av. Siempre Viva 742',
            comuna: 'Providencia',
            rol: 'VECINO'
          })
        });
      }
      if (url === '/documentos/residencia' && config.method === 'POST') {
        return Promise.resolve({
          status: 200,
          ok: true,
          json: async () => mockSuccessResponse,
        });
      }
      return Promise.resolve({ status: 404, ok: false });
    });

    render(
      <MemoryRouter>
        <GenerarDocumento />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByLabelText('RUT del Vecino')).toHaveValue('19.123.456-1');
    });

    // Write reason
    fireEvent.change(screen.getByLabelText('Motivo del Trámite'), { target: { value: 'Presentar en el trabajo' } });

    // Submit
    const submitBtn = screen.getByRole('button', { name: /Solicitar Documento/i });
    fireEvent.click(submitBtn);

    // Verify fetch arguments
    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith('/documentos/residencia', expect.objectContaining({
        method: 'POST',
        headers: expect.any(Object),
        body: expect.stringContaining('"usuarioNombreCompleto":"Juan Pérez"'),
      }));
    });

    // Check success modal contents
    expect(await screen.findByText('Solicitud Generada')).toBeInTheDocument();
    expect(screen.getByText('El documento ha sido generado exitosamente.')).toBeInTheDocument();
    expect(screen.getByText(/ID Documento:/)).toBeInTheDocument();
  });

  it('successfully submits form for junta-vecinal and displays success message', async () => {
    const mockSuccessResponse = {
      id: 67890,
      estado: 'BORRADOR',
      estadoBlockchain: 'PENDIENTE',
    };

    fetch.mockImplementation((url, config) => {
      if (url === '/usuarios/me') {
        return Promise.resolve({
          status: 200,
          ok: true,
          json: async () => ({
            id: 1,
            rut: '19.123.456-1',
            nombres: 'Juan',
            apellidoPaterno: 'Pérez',
            apellidoMaterno: '',
            direccion: 'Av. Siempre Viva 742',
            comuna: 'Providencia',
            rol: 'VECINO',
            territorio: {
              id: 4,
              nombre: 'Junta de Vecinos Villa Los Aromos'
            }
          })
        });
      }
      if (url === '/documentos/junta-vecinal' && config.method === 'POST') {
        return Promise.resolve({
          status: 200,
          ok: true,
          json: async () => mockSuccessResponse,
        });
      }
      return Promise.resolve({ status: 404, ok: false });
    });

    render(
      <MemoryRouter>
        <GenerarDocumento />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByLabelText('RUT del Vecino')).toHaveValue('19.123.456-1');
    });

    // Switch to junta-vecinal
    const select = screen.getByLabelText('Tipo de Trámite');
    fireEvent.change(select, { target: { value: 'junta-vecinal' } });

    // Verify Juntas de Vecinos specific fields render
    expect(screen.getByLabelText('Junta de Vecinos')).toHaveValue('Junta de Vecinos Villa Los Aromos');
    expect(screen.getByLabelText('ID Registro')).toHaveValue('4');
    expect(screen.getByLabelText('Tipo de Acta')).toHaveValue('ACTA_ASAMBLEA');

    // Fill in Ministro de Fe and Motivo
    fireEvent.change(screen.getByLabelText('RUT Ministro de Fe'), { target: { value: '19.123.456-1' } });
    fireEvent.change(screen.getByLabelText('Motivo del Trámite'), { target: { value: 'Renovación de directiva' } });

    // Submit
    const submitBtn = screen.getByRole('button', { name: /Solicitar Documento/i });
    fireEvent.click(submitBtn);

    // Verify fetch arguments
    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith('/documentos/junta-vecinal', expect.objectContaining({
        method: 'POST',
        headers: expect.any(Object),
        body: expect.stringContaining('"nombreJuntaVecinal":"Junta de Vecinos Villa Los Aromos"'),
      }));
    });

    // Check success modal contents
    expect(await screen.findByText('Solicitud Generada')).toBeInTheDocument();
    expect(screen.getByText('El documento ha sido generado exitosamente.')).toBeInTheDocument();
    expect(screen.getAllByText('Acta de Junta de Vecinos').length).toBeGreaterThan(0);
  });
});
