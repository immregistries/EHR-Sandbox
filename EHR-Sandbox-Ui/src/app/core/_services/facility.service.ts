import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';

import { Facility } from '../_model/rest';
import { BehaviorSubject, Observable, of, share } from 'rxjs';
import { SettingsService } from './settings.service';
import { CurrentSelectedWithIdService } from './current-selected-with-id.service';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
/**
 * Facility Service interacting with the API, and providing the global selected facility as an observable
 */
export class FacilityService extends CurrentSelectedWithIdService<Facility> {


  constructor(private http: HttpClient,
    private settings: SettingsService,
  ) {
    super(new BehaviorSubject<Facility>({ id: -1 }))
  }

  readAllFacilities(): Observable<Facility[]> {
    return this.http.get<Facility[]>(
      `${this.settings.getApiUrl()}/facilities`,
      httpOptions);
  }

  readFacilities(tenantId: number): Observable<Facility[]> {
    if (tenantId > 0) {
      return this.http.get<Facility[]>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities`,
        httpOptions).pipe(share());
    }
    return of([])
  }

  getRandom(tenantId: number): Observable<Facility> {
    return this.http.get<Facility>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/$random`,
      httpOptions);
  }

  readFacility(tenantId: number, facilityId: number): Observable<Facility> {
    return this.http.get<Facility>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}`,
      httpOptions);
  }

  readFacilityChildren(tenantId: number, facilityId: number): Observable<Facility[]> {
    if (facilityId == -1) {
      return of([])
    } else {
      return this.http.get<Facility[]>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/$children`,
        httpOptions);
    }

  }

  postFacility(tenantId: number, facility: Facility, populate?: boolean): Observable<HttpResponse<Facility>> {
    return this.http.post<Facility>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities${populate ? `?populate=${populate}` : ''
      }`,
      facility, { observe: 'response' })
  }

  putFacility(tenantId: number, facility: Facility): Observable<HttpResponse<Facility>> {
    return this.http.put<Facility>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities`,
      facility, { observe: 'response' })
  }
}
