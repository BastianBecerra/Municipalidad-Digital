import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import Home from './Home';

vi.mock('../components/Navbar', () => ({
  default: () => <div>Navbar Mock</div>,
}));
vi.mock('../components/HeroSection', () => ({
  default: () => <section>HeroSection Mock</section>,
}));
vi.mock('../components/ProcedureCards', () => ({
  default: () => <section>ProcedureCards Mock</section>,
}));
vi.mock('../components/MapSection', () => ({
  default: () => <section>MapSection Mock</section>,
}));
vi.mock('../components/Footer', () => ({
  default: () => <footer>Footer Mock</footer>,
}));

describe('Home Page', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders the main home sections', () => {
    render(
      <MemoryRouter>
        <Home />
      </MemoryRouter>
    );

    expect(screen.getByText('Navbar Mock')).toBeInTheDocument();
    expect(screen.getByText('HeroSection Mock')).toBeInTheDocument();
    expect(screen.getByText('ProcedureCards Mock')).toBeInTheDocument();
    expect(screen.getByText('MapSection Mock')).toBeInTheDocument();
    expect(screen.getByText('Footer Mock')).toBeInTheDocument();
  });
});
