import {HttpErrorResponse, HttpInterceptorFn} from '@angular/common/http';
import {inject} from '@angular/core';
import {MessageService} from 'primeng/api';
import {catchError, throwError} from 'rxjs';

/** Surfaces any failed request as a PrimeNG toast, then rethrows. */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const messageService = inject(MessageService);

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
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
