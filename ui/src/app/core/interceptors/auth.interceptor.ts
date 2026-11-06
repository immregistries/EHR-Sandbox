import {HttpInterceptorFn} from '@angular/common/http';
import {inject} from '@angular/core';
import {Store} from '@ngrx/store';
import {catchError, throwError} from 'rxjs';
import {TokenStorageService} from '../services';
import {AuthActions} from '../store/auth/auth.actions';

const TOKEN_HEADER_KEY = 'Authorization';

/**
 * Endpoints that proxy a *remote* IIS. A 401 from these means the registry
 * rejected its configured credentials — not that this app's session ended —
 * so it must not sign the user out. Connection checks on the settings page
 * are expected to fail this way.
 */
const REMOTE_REGISTRY_PATTERNS = [
  /\/registry\/\d+\/\$auth$/,
  /\/registry\/\d+\/\$connectivity$/,
  /\/registry\/\d+\/metadata$/,
  /\/fhir-client(\/|$|\?)/,
];

/** True when a 401 describes the remote registry, not this app's session. */
function isRemoteRegistryCall(url: string): boolean {
  const path = url.split('?')[0];
  return REMOTE_REGISTRY_PATTERNS.some((pattern) => pattern.test(path));
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const store = inject(Store);
  const token = inject(TokenStorageService).getToken();

  const authReq = token
    ? req.clone({headers: req.headers.set(TOKEN_HEADER_KEY, `Bearer ${token}`)})
    : req;

  return next(authReq).pipe(
    catchError((error) => {
      // The auth probe itself must not trigger a redirect loop, and a
      // remote registry's rejection is not this app's session expiring.
      const ownSession =
        !req.url.endsWith('/auth/user') && !isRemoteRegistryCall(req.url);
      if ((error.status === 401 || error.status === 403) && ownSession) {
        store.dispatch(AuthActions.sessionExpired());
      }
      return throwError(() => error);
    }),
  );
};
