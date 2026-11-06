import {Injectable} from '@angular/core';
import {JwtResponse} from './auth.service';

const TOKEN_KEY = 'auth-token';
const USER_KEY = 'auth-user';

/**
 * Persists the JWT that authenticates every request.
 *
 * Stored in localStorage so a session survives closing the browser. That is
 * a deliberate tradeoff: the token is readable by any script on this origin,
 * which matters more than it would for sessionStorage.
 */
@Injectable({providedIn: 'root'})
export class TokenStorageService {
  signOut(): void {
    // Only the auth keys: the theme preference also lives in localStorage.
    this.storage?.removeItem(TOKEN_KEY);
    this.storage?.removeItem(USER_KEY);
  }

  saveToken(token: string): void {
    this.storage?.setItem(TOKEN_KEY, token);
  }

  getToken(): string | null {
    return this.storage?.getItem(TOKEN_KEY) ?? null;
  }

  saveUser(user: JwtResponse): void {
    this.storage?.setItem(USER_KEY, JSON.stringify(user));
  }

  getUser(): JwtResponse | null {
    const user = this.storage?.getItem(USER_KEY);
    if (!user) {
      return null;
    }
    try {
      return JSON.parse(user) as JwtResponse;
    } catch {
      // Corrupt entry: treat it as no session rather than breaking startup.
      this.signOut();
      return null;
    }
  }

  /**
   * localStorage throws when access is blocked (private mode, disabled site
   * data), so every use is guarded: the app then runs without persistence.
   */
  private get storage(): Storage | null {
    try {
      return window.localStorage;
    } catch {
      return null;
    }
  }
}
