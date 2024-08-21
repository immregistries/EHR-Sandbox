import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, of, share, switchMap, throwError } from 'rxjs';
import { SettingsService } from './settings.service';
import { FacilityService } from './facility.service';
import { TenantService } from './tenant.service';
import { Revision, VaccinationEvent } from '../_model/rest';
import { RefreshService } from './refresh.service';
import { PatientService } from './patient.service';


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

  if_valid_parent_ids: Observable<boolean> = new Observable((subscriber) => subscriber.next(this.tenantService.getCurrentId() > 0 && this.facilityService.getCurrentId() > 0 && this.patientService.getCurrentId() > 0))
  if_valid_tenant_facility_ids: Observable<boolean> = new Observable((subscriber) => subscriber.next(this.tenantService.getCurrentId() > 0 && this.facilityService.getCurrentId() > 0))



  constructor(private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService,
    private patientService: PatientService) {
    super()
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
      if (value) {
        return this.http.get<VaccinationEvent[]>(
          `${this.settings.getApiUrl()}/tenants/${this.tenantService.getCurrentId()}/facilities/${this.facilityService.getCurrentId()}/patients/${this.patientService.getCurrentId()}/vaccinations`,
          httpOptions).pipe(share());
      } else {
        return of([])
      }
    }))
  }

  readVaccinations(patientId: number): Observable<VaccinationEvent[]> {
    return this.if_valid_parent_ids.pipe(switchMap((value) => {
      if (value) {
        return this.http.get<VaccinationEvent[]>(
          `${this.settings.getApiUrl()}/tenants/${this.tenantService.getCurrentId()}/facilities/${this.facilityService.getCurrentId()}/patients/${patientId}/vaccinations`,
          httpOptions)
      } else {
        return of([])
      }
    }))
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    if (tenantId > 0 && facilityId > 0 && patientId > 0) {
      return this.http.get<VaccinationEvent[]>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations`,
        httpOptions);
    } else {
      return of([])
    }
  }

  readVaccination(tenantId: number, facilityId: number, patientId: number, vaccinationId: number): Observable<VaccinationEvent> {
    if (tenantId > 0 && facilityId > 0 && patientId > 0 && vaccinationId > 0) {
      return this.http.get<VaccinationEvent>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/${vaccinationId}`,
        httpOptions);
    } else {
      return of()
    }
  }

  quickReadVaccinationFromFacility(vaccinationId: number): Observable<VaccinationEvent> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.readVaccinationFromFacility(tenantId, facilityId, vaccinationId)
  }

  readVaccinationFromFacility(tenantId: number, facilityId: number, vaccinationId: number): Observable<VaccinationEvent> {
    if (tenantId > 0 && facilityId > 0 && vaccinationId > 0) {
      return this.http.get<VaccinationEvent>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/vaccinations/${vaccinationId}`,
        httpOptions).pipe(share());
    } else {
      return of()
    }
  }

  quickPostVaccination(patientId: number, vaccination: VaccinationEvent, params?: HttpParams): Observable<HttpResponse<string>> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.postVaccination(tenantId, facilityId, patientId, vaccination, params)
  }

  postVaccination(tenantId: number, facilityId: number, patientId: number, vaccination: VaccinationEvent, params?: HttpParams): Observable<HttpResponse<string>> {
    if (tenantId > 0 && facilityId > 0 && patientId > 0) {
      return this.http.post<string>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations`,
        vaccination,
        { observe: 'response', params: params });
    } else {
      throw throwError(() => new Error("No patient selected"))
    }
  }

  quickPutVaccination(patientId: number, vaccination: VaccinationEvent): Observable<VaccinationEvent> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.putVaccination(tenantId, facilityId, patientId, vaccination)
  }

  putVaccination(tenantId: number, facilityId: number, patientId: number, vaccination: VaccinationEvent): Observable<VaccinationEvent> {
    if (tenantId > 0 && facilityId > 0 && patientId > 0) {
      return this.http.put<VaccinationEvent>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations`,
        vaccination, httpOptions);
    } else {
      throw throwError(() => new Error("No patient selected"))
    }
  }


  readVaccinationHistory(vaccinationId: number): Observable<Revision<VaccinationEvent>[]> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
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
