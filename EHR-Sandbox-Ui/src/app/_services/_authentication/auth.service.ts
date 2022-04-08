import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SettingsService } from '../settings.service';
import { User } from '../../_model/rest';
import { JwtResponse } from '../../_model/security';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(private http: HttpClient, private settings: SettingsService ) { }
  login(user: User): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(this.settings.getApiUrl() + '/auth', user, httpOptions);
  }
}
