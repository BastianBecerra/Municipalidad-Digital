import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import Login from './Login';

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('Login Page', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    vi.stubGlobal('fetch', vi.fn());
  });

  it('renders login inputs and updates password label for email', () => {
    render(<Login />);

    expect(screen.getByLabelText(/RUN o Email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/ClaveÚnica/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Autenticar/i })).toBeInTheDocument();

    fireEvent.change(screen.getByLabelText(/RUN o Email/i), {
      target: { value: 'usuario@ejemplo.cl' },
    });

    expect(screen.getByLabelText(/Contraseña/i)).toBeInTheDocument();
  });

  it('submits the form and navigates on successful login', async () => {
    const fetchMock = fetch;
    fetchMock.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ token: 'mock-token' }),
    });

    render(<Login />);

    fireEvent.change(screen.getByLabelText(/RUN o Email/i), {
      target: { value: 'usuario@ejemplo.cl' },
    });
    fireEvent.change(screen.getByLabelText(/Contraseña/i), {
      target: { value: 'password123' },
    });

    fireEvent.click(screen.getByRole('button', { name: /Autenticar/i }));

    await waitFor(() => {
      expect(fetchMock).toHaveBeenCalledWith('/auth/login', expect.objectContaining({
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
      }));
    });

    expect(localStorage.getItem('token')).toBe('mock-token');
    expect(mockNavigate).toHaveBeenCalledWith('/');
  });

  it('shows an error message when authentication fails', async () => {
    const fetchMock = fetch;
    fetchMock.mockResolvedValueOnce({ ok: false, text: async () => 'Unauthorized' });

    render(<Login />);

    fireEvent.change(screen.getByLabelText(/RUN o Email/i), {
      target: { value: '12345678-9' },
    });
    fireEvent.change(screen.getByLabelText(/ClaveÚnica/i), {
      target: { value: 'wrong-password' },
    });

    fireEvent.click(screen.getByRole('button', { name: /Autenticar/i }));

    expect(await screen.findByText(/RUN o ClaveÚnica incorrectos./i)).toBeInTheDocument();
    expect(mockNavigate).not.toHaveBeenCalled();
  });
});
