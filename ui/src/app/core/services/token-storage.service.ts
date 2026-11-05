import {Injectable} from '@angular/core';
import {JwtResponse} from './auth.service';

const TOKEN_KEY = 'auth-token';
const USER_KEY = 'auth-user';

/**
 * Persists the JWT in session storage. Clearing selected tenant/facility on
 * sign-out is handled by the auth store, not here.
 */
@Injectable({providedIn: 'root'})
export class TokenStorageService {
  signOut(): void {
    window.sessionStorage.clear();
  }

  saveToken(token: string): void {
    window.sessionStorage.setItem(TOKEN_KEY, token);
  }

  getToken(): string | null {
    return window.sessionStorage.getItem(TOKEN_KEY);
  }

  saveUser(user: JwtResponse): void {
    window.sessionStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  getUser(): JwtResponse | null {
    const user = window.sessionStorage.getItem(USER_KEY);
    return user ? JSON.parse(user) : null;
  }
}
