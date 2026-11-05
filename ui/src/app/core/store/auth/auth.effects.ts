import {inject, Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {Actions, createEffect, ofType} from '@ngrx/effects';
import {catchError, map, of, switchMap, tap} from 'rxjs';
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
        tap(({token, redirectTo}) => {
          if (token) {
            this.tokenStorage.saveToken(token);
          }
          this.router.navigateByUrl(redirectTo ?? '/tenants');
        }),
      ),
    {dispatch: false},
  );

  restoreSession$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.restoreSession),
      switchMap(() =>
        this.authService.checkLoggedUser().pipe(
          map((username) =>
            username
              ? AuthActions.restoreSessionSuccess({username: String(username)})
              : AuthActions.restoreSessionFailure(),
          ),
          catchError(() => of(AuthActions.restoreSessionFailure())),
        ),
      ),
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
