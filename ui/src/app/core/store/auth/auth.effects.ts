import {inject, Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {Actions, createEffect, ofType} from '@ngrx/effects';
import {catchError, exhaustMap, map, of, switchMap, tap} from 'rxjs';
import {AuthService, TokenStorageService} from '../../services';
import {FacilityActions} from '../facility/facility.actions';
import {PatientActions} from '../patient/patient.actions';
import {VaccinationActions} from '../vaccination/vaccination.actions';
import {AuthActions} from './auth.actions';

@Injectable()
export class AuthEffects {
  private readonly actions$ = inject(Actions);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly tokenStorage = inject(TokenStorageService);

  login$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.login),
      switchMap(({user, redirectTo}) =>
        this.authService.login(user).pipe(
          map((response) =>
            AuthActions.loginSuccess({
              username: response.body?.username ?? user.username ?? '',
              token: response.body?.accessToken ?? '',
              redirectTo,
            }),
          ),
          catchError((error) =>
            of(AuthActions.loginFailure({error: error.error?.message ?? 'Login failed'})),
          ),
        ),
      ),
    ),
  );

  loginSuccess$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(AuthActions.loginSuccess),
        tap(({username, token, redirectTo}) => {
          if (token) {
            this.tokenStorage.saveToken(token);
            this.tokenStorage.saveUser({id: -1, username, accessToken: token, tokenType: 'Bearer'});
          }
          this.router.navigateByUrl(redirectTo ?? '/tenants');
        }),
      ),
    {dispatch: false},
  );

  /**
   * Both the app shell and the auth guard ask for a restore, so ignore the
   * request while one is already outstanding rather than probing twice.
   */
  restoreSession$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.restoreSession),
      // exhaustMap drops requests arriving while a probe is in flight.
      exhaustMap(() => {
        // Auth is token-based: no stored token means no session, so there is
        // nothing to validate and no request worth making.
        if (!this.tokenStorage.getToken()) {
          return of(AuthActions.restoreSessionFailure());
        }
        const stored = this.tokenStorage.getUser();
        return this.authService.checkLoggedUser().pipe(
          map((username) => {
            // 204 No Content answers with an empty body: not authenticated.
            const name = (username ?? '').trim();
            if (!name) {
              this.tokenStorage.signOut();
              return AuthActions.restoreSessionFailure();
            }
            return AuthActions.restoreSessionSuccess({
              username: name || stored?.username || '',
            });
          }),
          // The token is present but rejected, so it is stale: clear it.
          catchError(() => {
            this.tokenStorage.signOut();
            return of(AuthActions.restoreSessionFailure());
          }),
        );
      }),
    ),
  );

  /** Signing out must also drop every tenant-scoped slice. */
  logout$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.logout, AuthActions.sessionExpired),
      tap(() => {
        this.tokenStorage.signOut();
        this.router.navigate(['/login']);
      }),
      switchMap(() => [
        FacilityActions.clear(),
        PatientActions.clear(),
        VaccinationActions.clear(),
      ]),
    ),
  );
}
