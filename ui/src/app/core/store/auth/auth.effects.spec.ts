import {of, throwError} from 'rxjs';
import {createEffectsHarness, expectEmission} from '../../../../testing';
import {AuthService, TokenStorageService} from '../../services';
import {AuthActions} from './auth.actions';
import {AuthEffects} from './auth.effects';

/**
 * Auth is token-based: the token in session storage is what a session is.
 * These cover restoring from it, and discarding it when the API rejects it.
 */
describe('AuthEffects session restore', () => {
  let authService: jasmine.SpyObj<AuthService>;
  let tokenStorage: jasmine.SpyObj<TokenStorageService>;

  beforeEach(() => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['checkLoggedUser', 'login']);
    tokenStorage = jasmine.createSpyObj<TokenStorageService>('TokenStorageService', [
      'getToken',
      'getUser',
      'saveToken',
      'saveUser',
      'signOut',
    ]);
  });

  function harness() {
    return createEffectsHarness(AuthEffects, {
      actions: of(AuthActions.restoreSession()),
      providers: [
        {provide: AuthService, useValue: authService},
        {provide: TokenStorageService, useValue: tokenStorage},
      ],
    });
  }

  it('restores the session when the stored token is accepted', async () => {
    tokenStorage.getToken.and.returnValue('jwt-valid');
    tokenStorage.getUser.and.returnValue(null);
    authService.checkLoggedUser.and.returnValue(of('tester'));

    const {effects} = harness();

    await expectEmission(
      effects.restoreSession$,
      AuthActions.restoreSessionSuccess({username: 'tester'}),
    );
  });

  it('makes no request when there is no stored token', async () => {
    tokenStorage.getToken.and.returnValue(null);

    const {effects} = harness();

    await expectEmission(effects.restoreSession$, AuthActions.restoreSessionFailure());
    expect(authService.checkLoggedUser).not.toHaveBeenCalled();
  });

  it('discards a token the API rejects', async () => {
    tokenStorage.getToken.and.returnValue('jwt-expired');
    tokenStorage.getUser.and.returnValue(null);
    authService.checkLoggedUser.and.returnValue(throwError(() => ({status: 401})));

    const {effects} = harness();

    await expectEmission(effects.restoreSession$, AuthActions.restoreSessionFailure());
    expect(tokenStorage.signOut).toHaveBeenCalled();
  });

  it('treats an empty probe body as no session, even with a stored user', async () => {
    tokenStorage.getToken.and.returnValue('jwt-valid');
    tokenStorage.getUser.and.returnValue({
      id: 1,
      username: 'stored-user',
      accessToken: 'jwt-valid',
      tokenType: 'Bearer',
    });
    // The backend answers 204 No Content when the token is not accepted, so
    // an empty body must not be mistaken for a live session.
    authService.checkLoggedUser.and.returnValue(of(''));

    const {effects} = harness();

    await expectEmission(effects.restoreSession$, AuthActions.restoreSessionFailure());
    expect(tokenStorage.signOut).toHaveBeenCalled();
  });
});

describe('AuthEffects restore with a 204 response', () => {
  let authService: jasmine.SpyObj<AuthService>;
  let tokenStorage: jasmine.SpyObj<TokenStorageService>;

  beforeEach(() => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['checkLoggedUser', 'login']);
    tokenStorage = jasmine.createSpyObj<TokenStorageService>('TokenStorageService', [
      'getToken',
      'getUser',
      'saveToken',
      'saveUser',
      'signOut',
    ]);
  });

  function harness() {
    return createEffectsHarness(AuthEffects, {
      actions: of(AuthActions.restoreSession()),
      providers: [
        {provide: AuthService, useValue: authService},
        {provide: TokenStorageService, useValue: tokenStorage},
      ],
    });
  }

  it('treats an empty body as not authenticated and clears the token', async () => {
    // Spring answers 204 No Content when the token is not accepted.
    tokenStorage.getToken.and.returnValue('jwt-stale');
    tokenStorage.getUser.and.returnValue(null);
    authService.checkLoggedUser.and.returnValue(of(''));

    const {effects} = harness();

    await expectEmission(effects.restoreSession$, AuthActions.restoreSessionFailure());
    expect(tokenStorage.signOut).toHaveBeenCalled();
  });

  it('restores from a plain-text username', async () => {
    tokenStorage.getToken.and.returnValue('jwt-valid');
    tokenStorage.getUser.and.returnValue(null);
    authService.checkLoggedUser.and.returnValue(of('tester'));

    const {effects} = harness();

    await expectEmission(
      effects.restoreSession$,
      AuthActions.restoreSessionSuccess({username: 'tester'}),
    );
    expect(tokenStorage.signOut).not.toHaveBeenCalled();
  });
});
