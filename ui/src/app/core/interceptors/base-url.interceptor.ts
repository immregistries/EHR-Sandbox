import {HttpInterceptorFn} from '@angular/common/http';

/** Sends credentials with every relative (same-origin API) request. */
export const baseUrlInterceptor: HttpInterceptorFn = (req, next) =>
  req.url.startsWith('http') ? next(req) : next(req.clone({withCredentials: true}));
