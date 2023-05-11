import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Tenant } from '../_model/rest';
import { BehaviorSubject, EMPTY, from, Observable } from 'rxjs';
import { SettingsService } from './settings.service';
import { observeNotification } from 'rxjs/internal/Notification';
import { FacilityService } from './facility.service';
import { RefreshService } from './refresh.service';
import { CurrentSelectedService } from './current-selected.service';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

/**
 * Service allowing the interactions with the tenant of the API, and providing the global selected tenant as an observable
 */
@Injectable({
  providedIn: 'root'
})
export class TenantService extends CurrentSelectedService<Tenant> {

  constructor(private http: HttpClient, private settings: SettingsService, private facilityService: FacilityService ) {
    super(new BehaviorSubject<Tenant>({id:-1}))
   }

  readTenants(): Observable<Tenant[]> {
    return this.http.get<Tenant[]>(
      this.settings.getApiUrl() + '/tenants', httpOptions);
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
