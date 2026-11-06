import {TestBed} from '@angular/core/testing';
import {TokenStorageService} from './token-storage.service';

describe('TokenStorageService', () => {
  let service: TokenStorageService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
    service = TestBed.inject(TokenStorageService);
  });

  afterEach(() => localStorage.clear());

  it('persists the token in localStorage so it survives a browser restart', () => {
    service.saveToken('jwt-abc');

    // sessionStorage would be cleared when the tab closes.
    expect(localStorage.getItem('auth-token')).toBe('jwt-abc');
    expect(sessionStorage.getItem('auth-token')).toBeNull();
    expect(service.getToken()).toBe('jwt-abc');
  });

  it('round-trips the signed-in user', () => {
    service.saveUser({id: 1, username: 'tester', accessToken: 'jwt-abc', tokenType: 'Bearer'});

    expect(service.getUser()?.username).toBe('tester');
  });

  it('reports no session before anything is stored', () => {
    expect(service.getToken()).toBeNull();
    expect(service.getUser()).toBeNull();
  });

  it('clears the auth keys on sign out', () => {
    service.saveToken('jwt-abc');
    service.saveUser({id: 1, username: 'tester', accessToken: 'jwt-abc', tokenType: 'Bearer'});

    service.signOut();

    expect(service.getToken()).toBeNull();
    expect(service.getUser()).toBeNull();
  });

  it('leaves other localStorage entries alone on sign out', () => {
    // The colour scheme shares localStorage; signing out must not reset it.
    localStorage.setItem('ehr-color-scheme', 'dark');
    service.saveToken('jwt-abc');

    service.signOut();

    expect(localStorage.getItem('ehr-color-scheme')).toBe('dark');
  });

  it('treats a corrupt user entry as no session', () => {
    localStorage.setItem('auth-user', '{not json');

    expect(service.getUser()).toBeNull();
    // And clears it, so it cannot keep failing.
    expect(localStorage.getItem('auth-user')).toBeNull();
  });
});
