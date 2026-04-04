import { describe, it, expect, beforeEach } from 'vitest';
import { useThemeStore } from './themeStore';

describe('themeStore', () => {
  beforeEach(() => {
    localStorage.clear();
    useThemeStore.setState({ darkMode: false });
  });

  it('should default to light mode', () => {
    expect(useThemeStore.getState().darkMode).toBe(false);
  });

  it('should toggle dark mode', () => {
    useThemeStore.getState().toggleDarkMode();
    expect(useThemeStore.getState().darkMode).toBe(true);
    expect(localStorage.getItem('darkMode')).toBe('true');
  });

  it('should toggle back to light mode', () => {
    useThemeStore.getState().toggleDarkMode();
    useThemeStore.getState().toggleDarkMode();
    expect(useThemeStore.getState().darkMode).toBe(false);
  });

  it('should hydrate from localStorage', () => {
    localStorage.setItem('darkMode', 'true');
    useThemeStore.getState().hydrate();
    expect(useThemeStore.getState().darkMode).toBe(true);
  });
});
