import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Facility, Tenant } from '../_model/rest';
import { Observable } from 'rxjs';
import { SettingsService } from './settings.service';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
@Injectable({
  providedIn: 'root'
})
export class TenantService {

  constructor(private http: HttpClient, private settings: SettingsService ) { }

  getTenants(): Observable<Tenant> {
    return this.http.get<Tenant>(
      this.settings.getApiUrl() + '/tenants', httpOptions);
  }

  getTenant(tenantId: number): Observable<Tenant> {
    return this.http.get<Tenant>(
      this.settings.getApiUrl()
      + '/tenants/' + tenantId, httpOptions);
  }

  postTenant(tenant: Tenant): Observable<Tenant> {
    return this.http.post<Tenant>(
      this.settings.getApiUrl()
      + '/tenants',
      tenant, httpOptions);
  }
}
