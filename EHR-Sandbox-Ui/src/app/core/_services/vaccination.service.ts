import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { BehaviorSubject, combineLatest, Observable, of, share, shareReplay, startWith, switchMap, tap, throwError } from 'rxjs';
import { SettingsService } from './settings.service';
import { FacilityService } from './facility.service';
import { TenantService } from './tenant.service';
import { Revision, VaccinationEvent } from '../_model/rest';
import { RefreshService } from './_abstract/refresh.service';
import { PatientService } from './patient.service';
import { SnackBarService } from './snack-bar.service';


const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
/**
 * Service allowing the use of the functionnalities related to vaccination provided by the API
 */
@Injectable({
  providedIn: 'root'
})
export class VaccinationService extends RefreshService {

  private _vaccinationsCached?: VaccinationEvent[] | undefined;
  public get vaccinationsCached(): VaccinationEvent[] | undefined {
    return this._vaccinationsCached;
  }
  private set vaccinationsCached(value: VaccinationEvent[] | undefined) {
    this._vaccinationsCached = value;
  }

  private readonly quickReadObservableFromFacility: Observable<VaccinationEvent[]> = combineLatest([
    this.getRefresh(),
    this.tenantService.getCurrentObservable(),
    this.facilityService.getCurrentObservable()
  ]).pipe(tap(() => this.loading = true))
    .pipe(switchMap(([_, tenant, facility]) => tenant?.id > 0 && facility.id && facility.id > 0 ? this.readVaccinationsFromFacility(tenant.id, facility.id) : of([])))
    .pipe(shareReplay({ bufferSize: 1, refCount: true }), tap((res) => { this.vaccinationsCached = res }))
    .pipe(tap(() => this.loading = false))


  private readonly quickReadObservable: Observable<VaccinationEvent[]> = combineLatest([
    this.getRefresh(),
    this.tenantService.getCurrentObservable(),
    this.facilityService.getCurrentObservable(),
    this.patientService.getCurrentObservable()
  ]).pipe(tap(() => this.loading = true))
    .pipe(switchMap(([_, tenant, facility, patient]) => tenant?.id > 0 && facility.id && facility.id > 0 && patient.id && patient.id > 0 ? this.readVaccinations(tenant.id, facility.id, patient.id) : of([])))
    .pipe(shareReplay({ bufferSize: 1, refCount: true }), tap((res) => { this.vaccinationsCached = res }))
    .pipe(tap(() => this.loading = false))


  constructor(private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService,
    private patientService: PatientService,
    snackBarService: SnackBarService
  ) {
    super(snackBarService)
  }

  public readRandom(patientId: number): Observable<VaccinationEvent> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.http.get<VaccinationEvent>(
      this.settings.getApiUrl() + `/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/$random`, httpOptions);
  }

  /**
   *
   * @returns list of patients associated to the tenant, facility and patient selected in their respected services, automatically latest
   */
  public quickReadVaccinations(): Observable<VaccinationEvent[]> {
    return this.quickReadObservable
  }

  public quickReadVaccinationsFromFacility(): Observable<VaccinationEvent[]> {
    return this.quickReadObservableFromFacility
  }

  public readVaccinations(tenantId: number, facilityId: number, patientId: number): Observable<VaccinationEvent[]> {
    return this.http.get<VaccinationEvent[]>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations`,
      httpOptions)
  }

  public readVaccinationsFromFacility(tenantId: number, facilityId: number): Observable<VaccinationEvent[]> {
    return this.http.get<VaccinationEvent[]>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/vaccinations`,
      httpOptions)
  }

  public readVaccination(tenantId: number, facilityId: number, patientId: number, vaccinationId: number): Observable<VaccinationEvent> {
    if (this.idsNotValid(tenantId, facilityId, patientId, vaccinationId)) {
      return of()
    }
    return this.http.get<VaccinationEvent>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/${vaccinationId}`,
      httpOptions);

  }

  public quickReadVaccinationFromFacility(vaccinationId: number): Observable<VaccinationEvent> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.readVaccinationFromFacility(tenantId, facilityId, vaccinationId)
  }

  public readVaccinationFromFacility(tenantId: number, facilityId: number, vaccinationId: number): Observable<VaccinationEvent> {
    if (this.idsNotValid(tenantId, facilityId, vaccinationId)) {
      return of()
    }
    return this.http.get<VaccinationEvent>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/vaccinations/${vaccinationId}`,
      httpOptions).pipe(share());
  }

  public quickPostVaccination(patientId: number, vaccination: VaccinationEvent, params?: HttpParams): Observable<HttpResponse<string>> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.postVaccination(tenantId, facilityId, patientId, vaccination, params)
  }

  public postVaccination(tenantId: number, facilityId: number, patientId: number, vaccination: VaccinationEvent, params?: HttpParams): Observable<HttpResponse<string>> {
    if (this.idsNotValid(tenantId, facilityId, patientId)) {
      return of()
    }
    return this.http.post<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations`,
      this.solveClinicianRedundancy(vaccination),
      { observe: 'response', params: params });

  }

  public quickPutVaccination(patientId: number, vaccination: VaccinationEvent): Observable<VaccinationEvent> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.putVaccination(tenantId, facilityId, patientId, vaccination)
  }

  public putVaccination(tenantId: number, facilityId: number, patientId: number, vaccination: VaccinationEvent): Observable<VaccinationEvent> {
    if (this.idsNotValid(tenantId, facilityId, patientId)) {
      return of()
    }
    return this.http.put<VaccinationEvent>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations`,
      this.solveClinicianRedundancy(vaccination),
      httpOptions);
  }


  public readVaccinationHistory(vaccinationId: number): Observable<Revision<VaccinationEvent>[]> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    if (this.idsNotValid(tenantId, facilityId, vaccinationId)) {
      return of()
    }
    return this.http.get<Revision<VaccinationEvent>[]>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/vaccinations/${vaccinationId}/$history`,
      httpOptions);
  }
  // readVaccinationHistory(patientId: number, vaccinationId: number): Observable<Revision<VaccinationEvent>[]> {
  //   const tenantId: number = this.tenantService.getCurrentId()
  //   const facilityId: number = this.facilityService.getCurrentId()
  //   return this.http.get<Revision<VaccinationEvent>[]>(
  //     `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/${vaccinationId}/$history`,
  //     httpOptions);
  // }

  public lotNumberValidation(lotNumber: string, cvx: string, mvx: string): Observable<HttpResponse<string>> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    // return this.http.get<boolean>(
    //   `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/vaccinations/${vaccinationId}/$lotNumberValidation`,
    //   httpOptions);
    let params: HttpParams = new HttpParams()
      .set("lotNumber", lotNumber)
      .set("cvx", cvx)
      .set("mvx", mvx)
    if (this.idsNotValid(tenantId, facilityId)) {
      return of()
    }
    return this.http.get(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/vaccinations/$lotNumberValidation`,
      { ...httpOptions, params: params, responseType: 'text', observe: 'response' });
    // return this.http.get<any>(
    //   `https://sabbia.westus2.cloudapp.azure.com/lot`,
    //   {
    //     headers: new HttpHeaders({ 'accept': 'application/json', "Access-Control-Allow-Origin": "*" }),
    //     params: params
    //   });
  }

  /**
   * IN PROGRESS
   * @param vaccination
   * @returns
   */
  private solveClinicianRedundancy(vaccination: VaccinationEvent): VaccinationEvent {
    let administeringId = vaccination.administeringClinician?.id
    let enteringId = vaccination.enteringClinician?.id
    let orderingId = vaccination.orderingClinician?.id
    let serializingVaccination = JSON.parse(JSON.stringify(vaccination));
    if (administeringId && administeringId === enteringId) {
      // @ts-ignore
      serializingVaccination.enteringClinician = enteringId
    }
    if (enteringId && enteringId === orderingId) {
      // @ts-ignore
      serializingVaccination.orderingClinician = orderingId
    }
    if (administeringId && administeringId === orderingId) {
      // @ts-ignore
      serializingVaccination.orderingClinician = orderingId
    }

    // if vaccination
    return serializingVaccination;
  }


}
