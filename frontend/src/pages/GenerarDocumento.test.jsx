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
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal();
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
  });

  it('renders all form fields for default residency trámite', () => {
    render(
      <MemoryRouter>
        <GenerarDocumento />
      </MemoryRouter>
    );

    expect(screen.getByLabelText('Tipo de Trámite')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('12.345.678-9')).toBeInTheDocument(); // RUT
    expect(screen.getByLabelText('Nombre Completo')).toBeInTheDocument();
    expect(screen.getByLabelText('Dirección')).toBeInTheDocument();
    expect(screen.getByLabelText('Comuna')).toBeInTheDocument();
    expect(screen.getByLabelText('Motivo del Trámite')).toBeInTheDocument();
  });

  it('hides comuna field when switching to salvoconducto', async () => {
    render(
      <MemoryRouter>
        <GenerarDocumento />
      </MemoryRouter>
    );

    const select = screen.getByLabelText('Tipo de Trámite');
    fireEvent.change(select, { target: { value: 'salvoconducto' } });

    expect(screen.queryByLabelText('Comuna')).not.toBeInTheDocument();
  });

  it('validates empty inputs and displays error messages', async () => {
    render(
      <MemoryRouter>
        <GenerarDocumento />
      </MemoryRouter>
    );

    const submitBtn = screen.getByRole('button', { name: /Solicitar Documento/i });
    fireEvent.click(submitBtn);

    expect(await screen.findByText('El RUT es obligatorio.')).toBeInTheDocument();
    expect(screen.getByText('El nombre es obligatorio.')).toBeInTheDocument();
    expect(screen.getByText('La dirección es obligatoria.')).toBeInTheDocument();
    expect(screen.getByText('La comuna es obligatoria.')).toBeInTheDocument();
    expect(screen.getByText('El motivo es obligatorio.')).toBeInTheDocument();
  });

  it('shows error for invalid RUT formats', async () => {
    render(
      <MemoryRouter>
        <GenerarDocumento />
      </MemoryRouter>
    );

    const rutInput = screen.getByPlaceholderText('12.345.678-9');
    
    // Test with short string
    fireEvent.change(rutInput, { target: { value: '1' } });
    fireEvent.blur(rutInput);
    expect(await screen.findByText('El RUT ingresado no es válido.')).toBeInTheDocument();

    // Test with invalid digits (rut validador should fail)
    fireEvent.change(rutInput, { target: { value: '11.111.111-2' } });
    fireEvent.blur(rutInput);
    expect(await screen.findByText('El RUT ingresado no es válido.')).toBeInTheDocument();
  });

  it('successfully submits form for residency and displays success message', async () => {
    const mockSuccessResponse = {
      id: 'DOC-12345',
      estado: 'BORRADOR',
    };

    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockSuccessResponse,
    });

    render(
      <MemoryRouter>
        <GenerarDocumento />
      </MemoryRouter>
    );

    // Fill valid data
    // RUT 19.123.456-1 has a valid checksum
    fireEvent.change(screen.getByPlaceholderText('12.345.678-9'), { target: { value: '19.123.456-1' } });
    fireEvent.change(screen.getByLabelText('Nombre Completo'), { target: { value: 'Juan Pérez' } });
    fireEvent.change(screen.getByLabelText('Dirección'), { target: { value: 'Av. Siempre Viva 742' } });
    fireEvent.change(screen.getByLabelText('Comuna'), { target: { value: 'Providencia' } });
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

    // Check success output
    expect(await screen.findByText('¡Solicitud Recibida!')).toBeInTheDocument();
    expect(screen.getByText('Documento generado exitosamente como BORRADOR.')).toBeInTheDocument();
  });
});
