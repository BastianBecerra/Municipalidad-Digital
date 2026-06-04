import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';
import Navbar from './Navbar';

// Mock react-router-dom's navigate
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('Navbar Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('renders correctly with default logo and navigation links', () => {
    render(
      <MemoryRouter>
        <Navbar />
      </MemoryRouter>
    );

    expect(screen.getByText('Muni Digital')).toBeInTheDocument();
    expect(screen.getByText('Inicio')).toBeInTheDocument();
    expect(screen.getByText('Trámites')).toBeInTheDocument();
    expect(screen.getByText('Contacto')).toBeInTheDocument();
  });

  it('shows "Ingresar" button when user is not logged in', () => {
    render(
      <MemoryRouter>
        <Navbar />
      </MemoryRouter>
    );

    const loginBtn = screen.getByText('Ingresar');
    expect(loginBtn).toBeInTheDocument();
    expect(screen.queryByText('Mi Perfil')).not.toBeInTheDocument();

    fireEvent.click(loginBtn);
    expect(mockNavigate).toHaveBeenCalledWith('/login');
  });

  it('shows "Mi Perfil" button when user is logged in with a token', () => {
    localStorage.setItem('token', 'mock-token');

    render(
      <MemoryRouter>
        <Navbar />
      </MemoryRouter>
    );

    const profileBtn = screen.getByText('Mi Perfil');
    expect(profileBtn).toBeInTheDocument();
    expect(screen.queryByText('Ingresar')).not.toBeInTheDocument();

    fireEvent.click(profileBtn);
    expect(mockNavigate).toHaveBeenCalledWith('/perfil');
  });
});
