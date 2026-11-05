import {HttpClient, HttpResponse} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Flavor, Tenant} from '../models';
import {ApiBase, JSON_HEADERS} from './api.base';

@Injectable({providedIn: 'root'})
export class TenantService extends ApiBase {
  private readonly http = inject(HttpClient);

  readTenants(): Observable<Tenant[]> {
    return this.http.get<Tenant[]>(this.url('/tenants'), JSON_HEADERS);
  }

  readTenant(tenantId: number): Observable<Tenant> {
    return this.http.get<Tenant>(this.tenantUrl(tenantId), JSON_HEADERS);
  }

  getRandom(): Observable<Tenant> {
    return this.http.get<Tenant>(this.url('/tenants/$random'), JSON_HEADERS);
  }

  postTenant(tenant: Tenant): Observable<HttpResponse<Tenant>> {
    return this.http.post<Tenant>(this.url('/tenants'), tenant, {
      ...JSON_HEADERS,
      observe: 'response',
    });
  }

  readAllFlavors(): Observable<Flavor[]> {
    return this.http.get<Flavor[]>(this.url('/flavors'), JSON_HEADERS);
  }

  flavorActivated(tenant: Tenant, flavor: Flavor): boolean {
    return tenant.nameDisplay?.includes(flavor.key) ?? false;
  }
}
