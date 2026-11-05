import {DOCUMENT} from '@angular/common';
import {computed, inject, Injectable, signal} from '@angular/core';

const STORAGE_KEY = 'ehr-color-scheme';
const DARK_CLASS = 'app-dark';

export type ColorScheme = 'light' | 'dark' | 'system';

/**
 * Owns the light/dark switch. The `.app-dark` class on <html> drives both the
 * PrimeNG theme preset and the project scss tokens.
 */
@Injectable({providedIn: 'root'})
export class ThemeService {
  private readonly document = inject(DOCUMENT);
  private readonly mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');

  readonly scheme = signal<ColorScheme>(this.readStoredScheme());
  private readonly systemPrefersDark = signal(this.mediaQuery.matches);

  readonly darkMode = computed(() => {
    const scheme = this.scheme();
    return scheme === 'system' ? this.systemPrefersDark() : scheme === 'dark';
  });

  constructor() {
    this.mediaQuery.addEventListener('change', (event) => {
      this.systemPrefersDark.set(event.matches);
      this.apply();
    });
    this.apply();
  }

  setScheme(scheme: ColorScheme): void {
    this.scheme.set(scheme);
    if (scheme === 'system') {
      localStorage.removeItem(STORAGE_KEY);
    } else {
      localStorage.setItem(STORAGE_KEY, scheme);
    }
    this.apply();
  }

  toggleDarkMode(): void {
    this.setScheme(this.darkMode() ? 'light' : 'dark');
  }

  private apply(): void {
    this.document.documentElement.classList.toggle(DARK_CLASS, this.darkMode());
  }

  private readStoredScheme(): ColorScheme {
    const stored = localStorage.getItem(STORAGE_KEY);
    return stored === 'dark' || stored === 'light' ? stored : 'system';
  }
}
