import {inject} from '@angular/core';
import {CanActivateFn, Router} from '@angular/router';
import {Store} from '@ngrx/store';
import {filter, map, take, tap} from 'rxjs';
import {AuthActions} from '../store/auth/auth.actions';
import {selectSessionStatus} from '../store/auth/auth.selectors';

/**
 * On a reload the store starts empty while the session probe is in flight,
 * so the guard waits for the probe to settle rather than reading
 * `authenticated` immediately — otherwise a signed-in user is redirected to
 * the login page.
 *
 * The probe is only started when the session is still unknown. Re-probing an
 * already-established session would undo a fresh login on backends whose
 * `/auth/user` does not confirm it.
 */
export const authGuard: CanActivateFn = (_route, state) => {
  const store = inject(Store);
  const router = inject(Router);

  return store.select(selectSessionStatus).pipe(
    tap((status) => {
      if (status.unknown) {
        store.dispatch(AuthActions.restoreSession());
      }
    }),
    filter((status) => status.resolved),
    take(1),
    map(({authenticated}) =>
      authenticated
        ? true
        : router.createUrlTree(['/login'], {queryParams: {redirectTo: state.url}}),
    ),
  );
};
