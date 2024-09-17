import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, of, share, switchMap, throwError } from 'rxjs';
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

  if_valid_parent_ids: Observable<boolean> = this.observables_parent_ids_valid(undefined, this.tenantService, this.facilityService, this.patientService);
  if_valid_tenant_facility_ids: Observable<boolean> = this.observables_parent_ids_valid(undefined, this.tenantService, this.facilityService);

  constructor(private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService,
    private patientService: PatientService,
    snackBarService: SnackBarService
  ) {
    super(snackBarService)
  }

  readRandom(patientId: number): Observable<VaccinationEvent> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.http.get<VaccinationEvent>(
      this.settings.getApiUrl() + `/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/$random`, httpOptions);
  }

  /**
   *
   * @returns list of patients associated to the tenant, facility and patient selected in their respected services
   */
  quickReadVaccinations(): Observable<VaccinationEvent[]> {
    return this.if_valid_parent_ids.pipe(switchMap((value) => {
      if (value === true) {
        return this.http.get<VaccinationEvent[]>(
          `${this.settings.getApiUrl()}/tenants/${this.tenantService.getCurrentId()}/facilities/${this.facilityService.getCurrentId()}/patients/${this.patientService.getCurrentId()}/vaccinations`,
          httpOptions).pipe(share());
      } else {
        return of([])
      }
    }))
  }

  readVaccinations(patientId: number): Observable<VaccinationEvent[]> {
    return this.if_valid_tenant_facility_ids.pipe(switchMap((value) => {
      if (value === true) {
        return this.http.get<VaccinationEvent[]>(
          `${this.settings.getApiUrl()}/tenants/${this.tenantService.getCurrentId()}/facilities/${this.facilityService.getCurrentId()}/patients/${patientId}/vaccinations`,
          httpOptions)
      } else {
        return of([])
      }
    }))
    // const tenantId: number = this.tenantService.getCurrentId()
    // const facilityId: number = this.facilityService.getCurrentId()
    // if (tenantId > 0 && facilityId > 0 && patientId > 0) {
    //   return this.http.get<VaccinationEvent[]>(
    //     `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations`,
    //     httpOptions);
    // } else {
    //   return of([])
    // }
  }

  readVaccination(tenantId: number, facilityId: number, patientId: number, vaccinationId: number): Observable<VaccinationEvent> {
    if (this.idsNotValid(tenantId, facilityId, patientId, vaccinationId)) {
      return of()
    }
    return this.http.get<VaccinationEvent>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/${vaccinationId}`,
      httpOptions);

  }

  quickReadVaccinationFromFacility(vaccinationId: number): Observable<VaccinationEvent> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.readVaccinationFromFacility(tenantId, facilityId, vaccinationId)
  }

  readVaccinationFromFacility(tenantId: number, facilityId: number, vaccinationId: number): Observable<VaccinationEvent> {
    if (this.idsNotValid(tenantId, facilityId, vaccinationId)) {
      return of()
    }
    return this.http.get<VaccinationEvent>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/vaccinations/${vaccinationId}`,
      httpOptions).pipe(share());
  }

  quickPostVaccination(patientId: number, vaccination: VaccinationEvent, params?: HttpParams): Observable<HttpResponse<string>> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.postVaccination(tenantId, facilityId, patientId, vaccination, params)
  }

  postVaccination(tenantId: number, facilityId: number, patientId: number, vaccination: VaccinationEvent, params?: HttpParams): Observable<HttpResponse<string>> {
    if (this.idsNotValid(tenantId, facilityId, patientId)) {
      return of()
    }
    return this.http.post<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations`,
      vaccination,
      { observe: 'response', params: params });

  }

  quickPutVaccination(patientId: number, vaccination: VaccinationEvent): Observable<VaccinationEvent> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.putVaccination(tenantId, facilityId, patientId, vaccination)
  }

  putVaccination(tenantId: number, facilityId: number, patientId: number, vaccination: VaccinationEvent): Observable<VaccinationEvent> {
    if (this.idsNotValid(tenantId, facilityId, patientId)) {
      return of()
    }
    return this.http.put<VaccinationEvent>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations`,
      vaccination, httpOptions);
  }


  readVaccinationHistory(vaccinationId: number): Observable<Revision<VaccinationEvent>[]> {
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

  lotNumberValidation(lotNumber: string, cvx: string, mvx: string): Observable<HttpResponse<string>> {
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



}
