import { HTTP_INTERCEPTORS, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpHandler, HttpRequest } from '@angular/common/http';
import { TokenStorageService } from './token-storage.service';
import { catchError, Observable, of } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
const TOKEN_HEADER_KEY = 'Authorization';       // for Spring Boot back-end
@Injectable()
/**
 * Interceptor handling the authentication process for each request to the API
 */
export class AuthInterceptor implements HttpInterceptor {
  constructor(
    private token: TokenStorageService,
    private router: Router,
    private dialog: MatDialog) { }
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let authReq = req;
    const token = this.token.getToken();
    if (token != null) {
      authReq = req.clone({ headers: req.headers.set(TOKEN_HEADER_KEY, 'Bearer ' + token) });
    }
    return next.handle(authReq).pipe(
      catchError(error => this.handleError(error))
    );
  }

  private handleError(error: HttpErrorResponse): Observable<any> {
    if (error.status === 401) {
      // Do your thing here
      const pathname = JSON.parse(JSON.stringify(document.location.pathname))
      // console.log(pathname)
      if( pathname === '/home') {
        this.router.navigate(['/home'], {queryParams: {
          loginError: 2,
          // redirectTo: pathname
        }});
      } else {
        this.router.navigate(['/home'], {queryParams: {
          loginError: 2,
          // redirectTo: pathname
        }});
      }

      // this.token.signOut()
    }
    throw error
    // return of(error)
  }

}
export const authInterceptorProviders = [
  { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
];

