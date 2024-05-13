import { HTTP_INTERCEPTORS, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpHandler, HttpRequest } from '@angular/common/http';
import { TokenStorageService } from './_services/token-storage.service';
import { BehaviorSubject, catchError, EMPTY, Observable, of, tap } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { NotificationCheckService } from '../_services/notification-check.service';
import { SnackBarService } from '../_services/snack-bar.service';
import { MatSnackBar, MatSnackBarRef, TextOnlySnackBar } from '@angular/material/snack-bar';
import { AuthService } from './_services/auth.service';
const TOKEN_HEADER_KEY = 'Authorization';       // for Spring Boot back-end
@Injectable()
/**
 * Interceptor handling the authentication process for each request to the API
 */
export class HealthCheckInterceptor implements HttpInterceptor {
  private _is_healthy = new BehaviorSubject<Boolean>(true);
  public get is_healthy() {
    return this._is_healthy;
  }
  public set is_healthy(value) {
    this._is_healthy = value;
  }
  private snack?: MatSnackBarRef<TextOnlySnackBar>;
  constructor(
    private router: Router,
    private dialog: MatDialog,
    private notificationCheckService: NotificationCheckService,
    private snackBarservice: SnackBarService,
    private authService: AuthService) { }


  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Check if backend is still not healthy
    if (!this.is_healthy.value && !req.url.endsWith("/healthy")) {
      this.authService.checkBackendHealthy().subscribe({
        next: (res) => {
          this.is_healthy.next(true)
        }, error: (error) => {
          this.is_healthy.next(false)
          this.snack = this.snackBarservice.open404()
          this.notificationCheckService.unsubscribe()
        }
      })
    }
    // close snack when healthy
    if (this.is_healthy.value && this.snack) {
      this.snack.dismiss()
    }
    return next.handle(req).pipe(
        tap(() => {
      // Check if backend is still not healthy

      }),
      catchError(error => this.handleError(error, req))
    );
  }

  private handleError(error: HttpErrorResponse, req: HttpRequest<any>): Observable<any> {

    if (req.url.endsWith('runtime-config.json')) {
      return EMPTY;
    }
    if (req.url.endsWith("/healthy")) {
      throw error
    }
    if (error.status === 0 || error.status === 404) {
      // Check if backend is healthy
      if (this.is_healthy.value) {
        this.authService.checkBackendHealthy().subscribe({
          error: (error) => {
            this.is_healthy.next(false)
            this.snack = this.snackBarservice.open404()
            this.notificationCheckService.unsubscribe()
          }
        })
      }
    }
    throw error
    // return of(error)
  }

}
export const healthCheckInterceptorProviders = [
  { provide: HTTP_INTERCEPTORS, useClass: HealthCheckInterceptor, multi: true }
];

