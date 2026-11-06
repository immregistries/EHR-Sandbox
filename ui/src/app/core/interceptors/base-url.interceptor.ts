import {HttpInterceptorFn} from '@angular/common/http';

/**
 * Normalises outgoing API requests. Auth is token-based — the Authorization
 * header is added by `authInterceptor` — so no credentials are attached here.
 */
export const baseUrlInterceptor: HttpInterceptorFn = (req, next) => next(req);
