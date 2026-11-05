import {HttpClient, HttpResponse} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {User} from '../models';
import {ApiBase, JSON_HEADERS} from './api.base';

export interface JwtResponse {
  id: number;
  username: string;
  accessToken: string;
  tokenType: string;
}

@Injectable({providedIn: 'root'})
export class AuthService extends ApiBase {
  private readonly http = inject(HttpClient);

  login(user: User): Observable<HttpResponse<JwtResponse>> {
    return this.http.post<JwtResponse>(this.url('/auth'), user, {
      ...JSON_HEADERS,
      observe: 'response',
    });
  }

  checkLoggedUser(): Observable<string> {
    return this.http.get<string>(this.url('/auth/user'));
  }

  checkBackendHealthy(): Observable<boolean> {
    return this.http.get<boolean>(this.url('/healthy'));
  }
}
