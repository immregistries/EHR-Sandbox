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
  private version: string;

  constructor(private http: HttpClient,) {
    // this.apiUrl = "http://localhost:8080/EHR-Api"
    this.apiUrl = "http://localhost:9091/EHR-Sandbox-Api"
    // this.apiUrl = "http://localhost:8080"
    this.version = "1.2.0"
  }

  public getVersion(): string {
    return this.version
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

  public putSettings(i: ImmunizationRegistry): Observable<ImmunizationRegistry>{
    return this.http.put<ImmunizationRegistry>(
      this.getApiUrl() + '/settings', i, httpOptions);
  }

}
