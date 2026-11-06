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

  /**
   * Validates the stored token. The endpoint answers with a bare username —
   * plain text, not JSON — and 204 No Content when not authenticated, so it
   * must be read as text; parsing it as JSON throws on the raw string.
   */
  checkLoggedUser(): Observable<string> {
    return this.http.get(this.url('/auth/user'), {responseType: 'text'});
  }

  checkBackendHealthy(): Observable<boolean> {
    return this.http.get<boolean>(this.url('/healthy'));
  }
}
