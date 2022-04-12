import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ImmunizationRegistry } from '../_model/rest';
import { environment } from '../../environments/environment';
import packageJson from '../../../package.json';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

/**
 * Service responsible for :
 *  - fetching global variables and making them accessible to the components,
 *  - interacting with the User's settings in the API
 */
@Injectable({
  providedIn: 'root'
})
export class SettingsService {
  private apiUrl: string = environment.apiUrl;
  private version: string = packageJson.version;

  constructor(private http: HttpClient,) {
    this.apiUrl = "http://localhost:9091/EHR-Sandbox-Api"
    // this.apiUrl = "http://localhost:8080"
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
