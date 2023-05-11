import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, of, share, throwError } from 'rxjs';
import { SettingsService } from './settings.service';
import { FacilityService } from './facility.service';
import { TenantService } from './tenant.service';
import { Revision, VaccinationEvent } from '../_model/rest';
import { RefreshService } from './refresh.service';


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


  constructor(private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService ) {
      super()
     }

  readRandom(patientId: number): Observable<VaccinationEvent> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.http.get<VaccinationEvent>(
      this.settings.getApiUrl() + `/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/$random`, httpOptions);
  }

  quickReadVaccinations(patientId: number): Observable<VaccinationEvent[]>{
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.readVaccinations(tenantId,facilityId,patientId)
  }

  readVaccinations(tenantId: number, facilityId: number, patientId: number): Observable<VaccinationEvent[]>{
    if (tenantId > 0 && facilityId > 0 && patientId > 0){
      return this.http.get<VaccinationEvent[]>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations`,
        httpOptions);
    } else {
      return of([])
    }
  }

  readVaccination(tenantId: number, facilityId: number, patientId: number, vaccinationId: number): Observable<VaccinationEvent>{
    if (tenantId > 0 && facilityId > 0 && patientId > 0 && vaccinationId > 0){
      return this.http.get<VaccinationEvent>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/${vaccinationId}`,
        httpOptions);
    } else {
      return of()
    }
  }

  quickReadVaccinationFromFacility(vaccinationId: number): Observable<VaccinationEvent>{
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.readVaccinationFromFacility(tenantId,facilityId,vaccinationId)
  }

  readVaccinationFromFacility(tenantId: number, facilityId: number, vaccinationId: number): Observable<VaccinationEvent>{
    if (tenantId > 0 && facilityId > 0 && vaccinationId > 0){
      return this.http.get<VaccinationEvent>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/vaccinations/${vaccinationId}`,
        httpOptions).pipe(share());
    } else {
      return of()
    }
  }

  quickPostVaccination(patientId: number, vaccination: VaccinationEvent): Observable<HttpResponse<string>>  {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.postVaccination(tenantId,facilityId,patientId,vaccination)
  }

  postVaccination(tenantId: number, facilityId: number, patientId: number, vaccination: VaccinationEvent): Observable<HttpResponse<string>>  {
    if (tenantId > 0 && facilityId > 0 && patientId > 0){
      return this.http.post<string>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations`,
        vaccination,
        {observe: 'response'});
    } else {
      throw throwError(() => new Error("No patient selected"))
    }
  }

  quickPutVaccination(patientId: number, vaccination: VaccinationEvent): Observable<VaccinationEvent>  {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.putVaccination(tenantId,facilityId,patientId,vaccination)
  }

  putVaccination(tenantId: number, facilityId: number, patientId: number, vaccination: VaccinationEvent): Observable<VaccinationEvent>   {
    if (tenantId > 0 && facilityId > 0 && patientId > 0){
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



}
