import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Tenant } from '../_model/rest';
import { BehaviorSubject, Observable } from 'rxjs';
import { SettingsService } from './settings.service';
import { FacilityService } from './facility.service';
import { CurrentSelectedWithIdService } from './current-selected-with-id.service';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

/**
 * Service allowing the interactions with the tenant of the API, and providing the global selected tenant as an observable
 */
@Injectable({
  providedIn: 'root'
})
export class TenantService extends CurrentSelectedWithIdService<Tenant> {

  constructor(private http: HttpClient, private settings: SettingsService, private facilityService: FacilityService ) {
    super(new BehaviorSubject<Tenant>({id:-1}))
   }

  readTenants(): Observable<Tenant[]> {
    return this.http.get<Tenant[]>(
      this.settings.getApiUrl() + '/tenants', httpOptions);
  }

  getRandom(): Observable<Tenant> {
    return this.http.get<Tenant>(
      `${this.settings.getApiUrl()}/tenants/$random`, httpOptions);
  }

  readTenant(tenantId: number): Observable<Tenant> {
    return this.http.get<Tenant>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}`, httpOptions);
  }

  postTenant(tenant: Tenant): Observable<HttpResponse<Tenant>> {
    return this.http.post<Tenant>(
      this.settings.getApiUrl()
      + '/tenants',
      tenant, {observe: 'response'});
  }
}
