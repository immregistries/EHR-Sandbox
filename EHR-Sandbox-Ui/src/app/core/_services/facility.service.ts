import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';

import { Facility } from '../_model/rest';
import { BehaviorSubject, combineLatest, merge, Observable, of, share, shareReplay, startWith, switchMap, tap } from 'rxjs';
import { SettingsService } from './settings.service';
import { CurrentSelectedWithIdService } from './_abstract/current-selected-with-id.service';
import { TenantService } from './tenant.service';
import { IdUrlVerifyingService } from './_abstract/id-url-verifying.service';
import { SnackBarService } from './snack-bar.service';

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

  private _facilitiesCached?: Facility[] | undefined;
  public get facilitiesCached(): Facility[] | undefined {
    return this._facilitiesCached;
  }
  private set facilitiesCached(value: Facility[] | undefined) {
    this._facilitiesCached = value;
  }


  /**
   * Not destroyed as used in pipes
   */
  private readonly quickReadObservable: Observable<Facility[]> = combineLatest([
    this.getRefresh(),
    this.tenantService.getCurrentObservable()
  ]).pipe(tap(() => this.loading = true))
    .pipe(switchMap(([_, tenant]) => tenant?.id > 0 ? this.readFacilities(tenant.id) : of([])))
    .pipe(shareReplay({ bufferSize: 1, refCount: true }), tap((res) => { this.facilitiesCached = res }))
    .pipe(tap(() => this.loading = false))


  constructor(private http: HttpClient,
    private settings: SettingsService,
    private tenantService: TenantService,
    snackBarService: SnackBarService
  ) {
    super(new BehaviorSubject<Facility>({ id: -1 }), snackBarService)
    // merge(
    //   this.getRefresh(),
    //   tenantService.getCurrentObservable()
    // ).subscribe((tenant) => {
    //   if (typeof tenant === "object") {
    //     this.readFacilities(tenant.id).subscribe((facilities) => {
    //       this._facilitiesCached = facilities
    //     })
    //   } else {
    //     this.readAllFacilities().subscribe((facilities) => {
    //       this._facilitiesCached = facilities
    //     })
    //   }
    // })
  }


  readAllFacilities(): Observable<Facility[]> {
    return this.http.get<Facility[]>(
      `${this.settings.getApiUrl()}/facilities`,
      httpOptions);
  }

  quickReadFacilities(): Observable<Facility[]> {
    return this.quickReadObservable
  }

  readFacilities(tenantId: number): Observable<Facility[]> {
    if (this.idsNotValid(tenantId)) {
      return of([])
    }
    return this.http.get<Facility[]>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities`,
      httpOptions);

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
    let parameters = populate ? { 'populate': populate + "" } : undefined
    return this.http.post<Facility>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities`,
      facility, { ...httpOptions, observe: 'response', params: parameters })
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
