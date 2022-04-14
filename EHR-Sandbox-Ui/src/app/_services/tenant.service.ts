import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Tenant } from '../_model/rest';
import { BehaviorSubject, EMPTY, from, Observable } from 'rxjs';
import { SettingsService } from './settings.service';
import { observeNotification } from 'rxjs/internal/Notification';
import { FacilityService } from './facility.service';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

/**
 * Service allowing the interactions with the tenant of the API, and providing the global selected tenant as an observable
 */
@Injectable({
  providedIn: 'root'
})
export class TenantService {
  private tenant: BehaviorSubject<Tenant>;
  private refresh: BehaviorSubject<boolean>;

  public getRefresh(): Observable<boolean> {
    return this.refresh.asObservable();
  }

  public doRefresh(): void{
    this.refresh.next(!this.refresh.value)
  }


  public getObservableTenant(): Observable<Tenant> {
    return this.tenant.asObservable();
  }

  public getTenant(): Tenant {
    return this.tenant.value
  }

  public getTenantId(): number {
    return this.tenant.value.id
  }

  public setTenant(tenant: Tenant) {
    if (tenant.id != this.tenant.value.id){
      this.facilityService.setFacility({id: -1})
    }
    this.tenant.next(tenant)
  }

  public setTenantEmpty() {
    this.tenant.next({id: -1})
  }

  constructor(private http: HttpClient, private settings: SettingsService, private facilityService: FacilityService ) {
    this.tenant = new BehaviorSubject<Tenant>({id:-1})
    this.refresh = new BehaviorSubject<boolean>(false)
   }

  readTenants(): Observable<Tenant[]> {
    return this.http.get<Tenant[]>(
      this.settings.getApiUrl() + '/tenants', httpOptions);
  }

  readTenant(tenantId: number): Observable<Tenant> {
    return this.http.get<Tenant>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}`, httpOptions);
  }

  postTenant(tenant: Tenant): Observable<HttpResponse<string>> {
    return this.http.post<string>(
      this.settings.getApiUrl()
      + '/tenants',
      tenant, {observe: 'response'});
  }
}
