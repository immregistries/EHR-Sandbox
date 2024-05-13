import { HTTP_INTERCEPTORS, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpHandler, HttpRequest } from '@angular/common/http';
import { TokenStorageService } from './_services/token-storage.service';
import { catchError, Observable, of } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { NotificationCheckService } from '../_services/notification-check.service';
import { AuthService } from './_services/auth.service';
const TOKEN_HEADER_KEY = 'Authorization';       // for Spring Boot back-end
@Injectable()
/**
 * Interceptor handling the authentication process for each request to the API
 */
export class AuthInterceptor implements HttpInterceptor {
  constructor(
    private tokenService: TokenStorageService,
    private router: Router,
    private dialog: MatDialog,
    private notificationCheckService: NotificationCheckService,
    private authService: AuthService) { }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let authReq = req;
    const token = this.tokenService.getToken();
    if (token != null) {
      authReq = req.clone({ headers: req.headers.set(TOKEN_HEADER_KEY, 'Bearer ' + token) });
    }
    return next.handle(authReq).pipe(
      catchError(error => this.handleError(error))
    );
  }

  private handleError(error: HttpErrorResponse): Observable<any> {
    if (error.status === 401) {
      this.authService.checkLoggedUser().subscribe({
        next: (res) => {
          if (!res) {
            this.redirectToLogin()
          }
        },
        error: (error2) => {
          this.redirectToLogin()
        }
      })
    }
    throw error
  }


  private redirectToLogin() {
    const pathname = JSON.parse(JSON.stringify(document.location.hash)).split('#')[1].split('?')[0]
    if (pathname === '/home') {
      this.router.navigate(['/home'], {
        queryParams: {
          loginError: 2,
          redirectTo: pathname
        }
      });
    } else {
      this.router.navigate(['/home'], {
        queryParams: {
          loginError: 2,
          redirectTo: pathname
        }
      });
      this.dialog.closeAll()
    }
    this.notificationCheckService.unsubscribe()
    this.tokenService.signOut()
  }
}

export const authInterceptorProviders = [
  { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
];

