import { create } from 'zustand';

interface ThemeState {
  darkMode: boolean;
  toggleDarkMode: () => void;
  hydrate: () => void;
}

export const useThemeStore = create<ThemeState>((set) => ({
  darkMode: false,

  toggleDarkMode: () =>
    set((state) => {
      const newMode = !state.darkMode;
      localStorage.setItem('darkMode', String(newMode));
      return { darkMode: newMode };
    }),

  hydrate: () => {
    const saved = localStorage.getItem('darkMode');
    if (saved === 'true') {
      set({ darkMode: true });
    }
  },
}));
