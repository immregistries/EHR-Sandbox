import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';

import { Facility } from '../_model/rest';
import { BehaviorSubject, merge, Observable, of, share, shareReplay } from 'rxjs';
import { SettingsService } from './settings.service';
import { CurrentSelectedWithIdService } from './current-selected-with-id.service';
import { TenantService } from './tenant.service';

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

  private _facilitiesCached!: Facility[];
  public get facilitiesCached(): Facility[] {
    return this._facilitiesCached;
  }

  constructor(private http: HttpClient,
    private settings: SettingsService,
    private tenantService: TenantService,
  ) {
    super(new BehaviorSubject<Facility>({ id: -1 }))
    merge(
      this.getRefresh(),
      tenantService.getCurrentObservable()
    ).subscribe((tenant) => {
      if (typeof tenant === "object") {
        this.readFacilities(tenant.id).pipe(
          shareReplay(1)
        ).subscribe((facilities) => {
          this._facilitiesCached = facilities
        })
      } else {
        this.readAllFacilities().subscribe((facilities) => {
          this._facilitiesCached = facilities
        })
      }

    })
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


  populate(tenantId: number, facilityId: number): Observable<string> {
    return this.http.get<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/$populate`,
      httpOptions)
  }

  putFacility(tenantId: number, facility: Facility): Observable<HttpResponse<Facility>> {
    return this.http.put<Facility>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities`,
      facility, { observe: 'response' })
  }
}
