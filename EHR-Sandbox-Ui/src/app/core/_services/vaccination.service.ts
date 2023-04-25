import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, of, share, throwError } from 'rxjs';
import { SettingsService } from './settings.service';
import { FacilityService } from './facility.service';
import { TenantService } from './tenant.service';
import { Revision, VaccinationEvent } from '../_model/rest';


const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
/**
 * Service allowing the use of the functionnalities related to vaccination provided by the API
 */
@Injectable({
  providedIn: 'root'
})
export class VaccinationService {
  /**
   * Global observable used to trigger a refresh for all the lists of vaccination
   */
  private refresh: BehaviorSubject<boolean>;

  public getRefresh(): Observable<boolean> {
    return this.refresh.asObservable();
  }

  public doRefresh(): void{
    this.refresh.next(!this.refresh.value)
  }


  constructor(private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService ) {
      this.refresh = new BehaviorSubject<boolean>(false)
     }

  readRandom(): Observable<VaccinationEvent> {
    return this.http.get<VaccinationEvent>(
      this.settings.getApiUrl() + '/$random_vaccination', httpOptions);
  }

  quickReadVaccinations(patientId: number): Observable<VaccinationEvent[]>{
    const tenantId: number = this.tenantService.getTenantId()
    const facilityId: number = this.facilityService.getFacilityId()
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
    const tenantId: number = this.tenantService.getTenantId()
    const facilityId: number = this.facilityService.getFacilityId()
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
    const tenantId: number = this.tenantService.getTenantId()
    const facilityId: number = this.facilityService.getFacilityId()
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
    const tenantId: number = this.tenantService.getTenantId()
    const facilityId: number = this.facilityService.getFacilityId()
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
    const tenantId: number = this.tenantService.getTenantId()
    const facilityId: number = this.facilityService.getFacilityId()
    return this.http.get<Revision<VaccinationEvent>[]>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/vaccinations/${vaccinationId}/$history`,
      httpOptions);
  }
  // readVaccinationHistory(patientId: number, vaccinationId: number): Observable<Revision<VaccinationEvent>[]> {
  //   const tenantId: number = this.tenantService.getTenantId()
  //   const facilityId: number = this.facilityService.getFacilityId()
  //   return this.http.get<Revision<VaccinationEvent>[]>(
  //     `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/${vaccinationId}/$history`,
  //     httpOptions);
  // }



}
