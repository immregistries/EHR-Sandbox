import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, share, of, BehaviorSubject, switchMap, tap, shareReplay, combineLatest, startWith, windowTime } from 'rxjs';
import { Clinician } from '../_model/rest';
import { SettingsService } from './settings.service';
import { TenantService } from './tenant.service';
import { RefreshService } from './_abstract/refresh.service';
import { SnackBarService } from './snack-bar.service';
import { Console } from 'console';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class ClinicianService extends RefreshService {

  private _cliniciansCached!: Clinician[];
  public get cliniciansCached(): Clinician[] {
    return this._cliniciansCached;
  }
  private set cliniciansCached(value: Clinician[]) {
    this._cliniciansCached = value;
  }

  private readonly quickReadObservable: Observable<Clinician[]> =
    combineLatest([
      this.getRefresh().pipe(startWith(false)), // Start with null to trigger initially
      this.tenantService.getCurrentObservable().pipe(startWith(this.tenantService.getCurrent())) // Start with the initial ID
    ]).pipe(switchMap(([_, tenant]) => tenant?.id > 0 ? this.readClinicians(tenant.id) : of([])))
      .pipe(shareReplay({ bufferSize: 1, refCount: true }), tap((res) => { this.cliniciansCached = res }))



  constructor(private http: HttpClient,
    private tenantService: TenantService,
    private settings: SettingsService,
    snackBarService: SnackBarService
  ) {
    super(snackBarService)
  }

  quickReadClinicians(): Observable<Clinician[]> {
    return this.quickReadObservable
  }

  readClinicians(tenantId: number): Observable<Clinician[]> {
    if (this.idsNotValid(tenantId)) {
      return of([])
    }
    return this.http.get<Clinician[]>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/clinicians`,
      httpOptions).pipe(tap((result) => {
        this._cliniciansCached = result
      }));
  }

  random(tenantId: number): Observable<Clinician> {
    return this.http.get<Clinician>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/clinicians/$random`,
      httpOptions);
  }

  readClinician(tenantId: number, clinicianId: number): Observable<Clinician> {
    return this.http.get<Clinician>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/clinicians/${clinicianId}`,
      httpOptions);
  }

  postClinician(tenantId: number, clinician: Clinician): Observable<Clinician> {
    return this.http.post<Clinician>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/clinicians`,
      clinician, httpOptions)
  }

  putClinician(tenantId: number, clinician: Clinician): Observable<Clinician> {
    clinician.tenant = undefined
    return this.http.put<Clinician>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/clinicians/${clinician.id}`,
      clinician, httpOptions)
  }
}
