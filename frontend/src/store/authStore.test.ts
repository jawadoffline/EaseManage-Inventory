import { describe, it, expect, beforeEach } from 'vitest';
import { useAuthStore } from './authStore';

describe('authStore', () => {
  beforeEach(() => {
    useAuthStore.getState().logout();
    localStorage.clear();
  });

  it('should start unauthenticated', () => {
    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(false);
    expect(state.user).toBeNull();
  });

  it('should set auth data', () => {
    const user = { id: 1, username: 'admin', email: 'admin@test.com', firstName: 'Admin', lastName: 'User', role: 'ADMIN' as const };
    useAuthStore.getState().setAuth(user, 'access-token', 'refresh-token');

    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(true);
    expect(state.user?.username).toBe('admin');
    expect(state.accessToken).toBe('access-token');
    expect(localStorage.getItem('accessToken')).toBe('access-token');
  });

  it('should logout and clear storage', () => {
    const user = { id: 1, username: 'admin', email: 'admin@test.com', firstName: 'Admin', lastName: 'User', role: 'ADMIN' as const };
    useAuthStore.getState().setAuth(user, 'token', 'refresh');
    useAuthStore.getState().logout();

    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(false);
    expect(state.user).toBeNull();
    expect(localStorage.getItem('accessToken')).toBeNull();
  });

  it('should hydrate from localStorage', () => {
    localStorage.setItem('accessToken', 'saved-token');
    localStorage.setItem('refreshToken', 'saved-refresh');
    localStorage.setItem('user', JSON.stringify({ id: 1, username: 'test', email: 'e', firstName: 'F', lastName: 'L', role: 'VIEWER' }));

    useAuthStore.getState().hydrate();
    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(true);
    expect(state.user?.username).toBe('test');
  });
});
