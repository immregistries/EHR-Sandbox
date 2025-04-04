import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Flavor, Tenant } from '../_model/rest';
import { BehaviorSubject, combineLatest, map, mergeMap, Observable, shareReplay, tap } from 'rxjs';
import { SettingsService } from './settings.service';
import { FacilityService } from './facility.service';
import { CurrentSelectedWithIdService } from './_abstract/current-selected-with-id.service';
import { SnackBarService } from './snack-bar.service';

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

  private readonly quickReadObservable = this.getRefresh()
    .pipe(tap(() => this.loading = true))
    .pipe(mergeMap(() => this.readTenants()))
    .pipe(shareReplay({ bufferSize: 1, refCount: false }))
    .pipe(tap(() => this.loading = false))

  constructor(private http: HttpClient, private settings: SettingsService,
    snackBarService: SnackBarService
  ) {
    super(new BehaviorSubject<Tenant>({ id: -1 }), snackBarService)
  }

  public quickReadTenants(): Observable<Tenant[]> {
    return this.quickReadObservable
  }

  public readTenants(): Observable<Tenant[]> {
    return this.http.get<Tenant[]>(
      this.settings.getApiUrl() + '/tenants', httpOptions);
  }

  public getRandom(): Observable<Tenant> {
    return this.http.get<Tenant>(
      `${this.settings.getApiUrl()}/tenants/$random`, httpOptions);
  }

  public readTenant(tenantId: number): Observable<Tenant> {
    return this.http.get<Tenant>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}`, httpOptions);
  }

  public postTenant(tenant: Tenant): Observable<HttpResponse<Tenant>> {
    return this.http.post<Tenant>(
      this.settings.getApiUrl()
      + '/tenants',
      tenant, { observe: 'response' });
  }

  public readAllFlavors(): Observable<Flavor[]> {
    return this.http.get<Flavor[]>(
      this.settings.getApiUrl() + '/flavors', httpOptions);
  }

  public flavorActivated(tenant: Tenant, flavor: Flavor): boolean {
    const key = flavor.key
    if (!tenant.nameDisplay) {
      return false
    } else if (tenant.nameDisplay.startsWith(key + " ")
      || tenant.nameDisplay.endsWith(" " + key)
      || tenant.nameDisplay.indexOf(" " + key + " ") > 0) {
      return true
    } else if (tenant.nameDisplay.startsWith(key + "_")
      || tenant.nameDisplay.endsWith("_" + key)
      || tenant.nameDisplay.indexOf("_" + key + "_") > 0) {
      return true
    } else if (tenant.nameDisplay === key) {
      return true
    } else return false
  }

}
