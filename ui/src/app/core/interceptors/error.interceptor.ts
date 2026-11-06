import {HttpErrorResponse, HttpInterceptorFn} from '@angular/common/http';
import {inject} from '@angular/core';
import {MessageService} from 'primeng/api';
import {catchError, throwError} from 'rxjs';

/** Surfaces any failed request as a PrimeNG toast, then rethrows. */
/**
 * Connection checks against a remote registry are expected to fail; the
 * settings page reports the outcome inline, so a global toast would be
 * noise.
 */
const SILENT_PATTERNS = [
  /\/registry\/\d+\/\$auth$/,
  /\/registry\/\d+\/\$connectivity$/,
  /\/registry\/\d+\/metadata$/,
];

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const messageService = inject(MessageService);
  const silent = SILENT_PATTERNS.some((pattern) => pattern.test(req.url.split('?')[0]));

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      if (silent) {
        return throwError(() => err);
      }
      const body = err.error ?? {};
      messageService.add({
        key: 'globalError',
        severity: 'error',
        summary: body.message ?? err.statusText ?? 'Server error',
        detail: `${err.status} – ${req.url}`,
        data: body,
        life: 6000,
      });
      return throwError(() => err);
    }),
  );
};
