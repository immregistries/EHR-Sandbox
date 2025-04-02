import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, combineLatest, concat, concatMap, defer, delay, filter, iif, merge, of, share, shareReplay, startWith, switchMap, take, tap, throwError } from 'rxjs';
import { SettingsService } from './settings.service';
import { EhrPatient, Revision } from '../_model/rest';
import { FacilityService } from './facility.service';
import { TenantService } from './tenant.service';
import { CurrentSelectedWithIdService } from './_abstract/current-selected-with-id.service';
import { SnackBarService } from './snack-bar.service';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
/**
 * Patient Service interacting with the API, and providing the global selected patient as an observable
 */
export class PatientService extends CurrentSelectedWithIdService<EhrPatient> {

  private _patientsCached: EhrPatient[] | undefined;
  public get patientsCached(): EhrPatient[] | undefined {
    return this._patientsCached;
  }
  private set patientsCached(value: EhrPatient[] | undefined) {
    this._patientsCached = value;
  }

  private readonly quickReadObservable: Observable<EhrPatient[]> = combineLatest([
    this.getRefresh().pipe(startWith(false)), // Start with null to trigger initially
    this.tenantService.getCurrentObservable(),
    this.facilityService.getCurrentObservable()
  ]).pipe(tap(() => this.loading = true))
    .pipe(switchMap(([_, tenant, facility]) => tenant?.id > 0 && facility?.id && facility.id > 0 ? this.readPatients(tenant.id, facility.id) : of([])))
    .pipe(shareReplay({ bufferSize: 1, refCount: true }), tap((res) => { this.patientsCached = res }))
    .pipe(tap(() => this.loading = false))

  constructor(private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService,
    snackBarService: SnackBarService
  ) {
    super(new BehaviorSubject<EhrPatient>({ id: -1, names: [] }), snackBarService)
    /**
     * Making it so that changing selected globally facility unselects patient
     */
    this.facilityService.getCurrentObservable().subscribe((facility) => {
      this.setCurrent({ names: [] })
    })
  }


  /**
   *
   * @returns Patient object filled with random information
   */
  readRandom(facilityId?: number): Observable<EhrPatient> {
    if (facilityId && facilityId > 0) {
      return this.http.get<EhrPatient>(
        this.settings.getApiUrl() + `/tenants/${this.tenantService.getCurrentId()}/facilities/${facilityId}/$random_patient`, httpOptions);
    } else {
      return this.http.get<EhrPatient>(
        this.settings.getApiUrl() + '/$random_patient', httpOptions);
    }

  }

  /**
   *
   * @returns list of patients associated to the tenant and facility selected in their respected services
   */
  public quickReadPatients(): Observable<EhrPatient[]> {
    return this.quickReadObservable
  }


  /**
   *
   * @param tenantId
   * @param facilityId
   * @returns list of patients associated to the tenant and facility
   */
  public readPatients(tenantId: number, facilityId: number): Observable<EhrPatient[]> {
    if (tenantId > 0 && facilityId > 0) {
      return this.http.get<EhrPatient[]>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients`,
        httpOptions);
    }
    return of([])
  }

  /**
   *
   * @param tenantId
   * @returns list of patients associated to the tenant
   */
  public readAllPatients(tenantId: number): Observable<EhrPatient[]> {
    if (tenantId > 0) {
      return this.http.get<EhrPatient[]>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/patients`,
        httpOptions);
    }
    return of([])
  }

  public quickReadPatient(patientId: number): Observable<EhrPatient> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.readPatient(tenantId, facilityId, patientId)
  }

  public populatePatient(patientId: number): Observable<string> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.http.get<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/$populate`,
      httpOptions);
  }

  /**
   *
   * @param tenantId
   * @param facilityId
   * @param patientId
   * @returns patient Get Response
   */
  public readPatient(tenantId: number, facilityId: number, patientId: number): Observable<EhrPatient> {
    return this.http.get<EhrPatient>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}`,
      httpOptions).pipe(share());
  }

  public readPatientHistory(patientId: number): Observable<Revision<EhrPatient>[]> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.http.get<Revision<EhrPatient>[]>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/$history`,
      httpOptions);
  }

  /**
   * Helping function for posting patient, automatically retrieving current facility and tenantId
   * @param patient
   * @returns Post patient Response
   */
  public quickPostPatient(patient: EhrPatient): Observable<HttpResponse<string>> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.postPatient(tenantId, facilityId, patient)
  }

  public postPatient(tenantId: number, facilityId: number, patient: EhrPatient, params?: HttpParams): Observable<HttpResponse<string>> {
    if (tenantId > 0 && facilityId > 0) {
      return this.http.post<string>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients`,
        patient,
        { ...httpOptions, observe: 'response', params: params });
    } else {
      throw throwError(() => new Error("No facility selected"))
    }
  }

  public postPatientOnFacilityOnly(facilityId: number, patient: EhrPatient): Observable<HttpResponse<string>> {
    if (facilityId > 0) {
      return this.http.post<string>(
        `${this.settings.getApiUrl()}/facilities/${facilityId}/patients`,
        patient,
        { ...httpOptions, observe: 'response' });
    } else {
      throw throwError(() => new Error("No facility selected"))
    }
  }

  public quickPutPatient(patient: EhrPatient): Observable<EhrPatient> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.putPatient(tenantId, facilityId, patient)
  }

  public putPatient(tenantId: number, facilityId: number, patient: EhrPatient,): Observable<EhrPatient> {
    if (tenantId > 0 && facilityId > 0) {
      return this.http.put<EhrPatient>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients`,
        patient, httpOptions);
    } else {
      throw throwError(() => new Error("No facility selected"))
    }
  }

}
