import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ImmunizationRegistry } from '../_model/rest';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
@Injectable({
  providedIn: 'root'
})
export class SettingsService {
  private apiUrl: string;

  constructor(private http: HttpClient,) {
    this.apiUrl = "http://localhost:8080"
  }

  public getApiUrl(): string {
    return this.apiUrl
  }

  public setApiUrl(url: string) {
    this.apiUrl = url
    return this.apiUrl
  }

  public getSettings(): Observable<ImmunizationRegistry>{
    return this.http.get<ImmunizationRegistry>(
      this.getApiUrl() + '/settings', httpOptions);
  }

  public postSettings(i: ImmunizationRegistry): Observable<ImmunizationRegistry>{
    return this.http.put<ImmunizationRegistry>(
      this.getApiUrl() + '/settings', i, httpOptions);
  }

}
