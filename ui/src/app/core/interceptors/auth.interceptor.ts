import {HttpInterceptorFn} from '@angular/common/http';
import {inject} from '@angular/core';
import {Store} from '@ngrx/store';
import {catchError, throwError} from 'rxjs';
import {TokenStorageService} from '../services';
import {AuthActions} from '../store/auth/auth.actions';

const TOKEN_HEADER_KEY = 'Authorization';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const store = inject(Store);
  const token = inject(TokenStorageService).getToken();

  const authReq = token
    ? req.clone({headers: req.headers.set(TOKEN_HEADER_KEY, `Bearer ${token}`)})
    : req;

  return next(authReq).pipe(
    catchError((error) => {
      // The auth probe itself returning 401 must not trigger a redirect loop.
      if ((error.status === 401 || error.status === 403) && !req.url.endsWith('/auth/user')) {
        store.dispatch(AuthActions.sessionExpired());
      }
      return throwError(() => error);
    }),
  );
};
