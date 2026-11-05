import {inject} from '@angular/core';
import {CanActivateFn, Router} from '@angular/router';
import {Store} from '@ngrx/store';
import {map, take} from 'rxjs';
import {selectIsAuthenticated} from '../store/auth/auth.selectors';

export const authGuard: CanActivateFn = (_route, state) => {
  const store = inject(Store);
  const router = inject(Router);

  return store.select(selectIsAuthenticated).pipe(
    take(1),
    map((authenticated) =>
      authenticated
        ? true
        : router.createUrlTree(['/login'], {queryParams: {redirectTo: state.url}}),
    ),
  );
};
