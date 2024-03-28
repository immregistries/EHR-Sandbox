import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SettingsService } from '../../_services/settings.service';
import { User } from '../../_model/rest';
import { JwtResponse } from '../security';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(private http: HttpClient, private settings: SettingsService ) { }
  login(user: User): Observable<HttpResponse<JwtResponse>> {
    return this.http.post<JwtResponse>(this.settings.getApiUrl() + '/auth', user, {observe: 'response'});
  }
}
