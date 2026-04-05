import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import ProtectedRoute from './ProtectedRoute';
import { useAuthStore } from '../store/authStore';

describe('ProtectedRoute', () => {
  it('should redirect to login when not authenticated', () => {
    useAuthStore.setState({ isAuthenticated: false, user: null });

    render(
      <MemoryRouter initialEntries={['/protected']}>
        <Routes>
          <Route path="/login" element={<div>Login Page</div>} />
          <Route element={<ProtectedRoute />}>
            <Route path="/protected" element={<div>Protected Content</div>} />
          </Route>
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('Login Page')).toBeInTheDocument();
  });

  it('should render content when authenticated', () => {
    useAuthStore.setState({
      isAuthenticated: true,
      user: { id: 1, username: 'admin', email: 'a@b.com', firstName: 'A', lastName: 'B', role: 'ADMIN' }
    });

    render(
      <MemoryRouter initialEntries={['/protected']}>
        <Routes>
          <Route element={<ProtectedRoute />}>
            <Route path="/protected" element={<div>Protected Content</div>} />
          </Route>
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('Protected Content')).toBeInTheDocument();
  });

  it('should redirect when role not allowed', () => {
    useAuthStore.setState({
      isAuthenticated: true,
      user: { id: 1, username: 'viewer', email: 'v@b.com', firstName: 'V', lastName: 'B', role: 'VIEWER' }
    });

    render(
      <MemoryRouter initialEntries={['/admin']}>
        <Routes>
          <Route path="/dashboard" element={<div>Dashboard</div>} />
          <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
            <Route path="/admin" element={<div>Admin Only</div>} />
          </Route>
        </Routes>
      </MemoryRouter>
    );

    expect(screen.queryByText('Admin Only')).not.toBeInTheDocument();
  });
});
