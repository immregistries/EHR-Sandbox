import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { SettingsService } from './settings.service';
import { Patient, VaccinationEvent} from '../_model/rest';
import { FacilityService } from './facility.service';
import { TenantService } from './tenant.service';


const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class VaccinationService {

  constructor(private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService ) { }

  readRandom(): Observable<VaccinationEvent> {
    return this.http.get<VaccinationEvent>(
      this.settings.getApiUrl() + '/random_vaccination', httpOptions);
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
}
