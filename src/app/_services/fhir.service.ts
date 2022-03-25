import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { SettingsService } from './settings.service';
import { FacilityService } from './facility.service';
import { TenantService } from './tenant.service';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
@Injectable({
  providedIn: 'root'
})
export class FhirService {

  constructor(private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService ) { }

  quickGetVaccination(patientId: number, vaccinationId: number): Observable<string> {
    const tenantId: number = this.tenantService.getTenantId()
    const facilityId: number = this.facilityService.getFacilityId()
    return this.getVaccination(tenantId,facilityId,patientId,vaccinationId)
  }

  getVaccination(tenantId: number, facilityId: number, patientId: number, vaccinationId: number): Observable<string> {
    return this.http.get(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/${vaccinationId}/fhir`,
      { responseType: 'text' });
  }

  quickPostVaccination(patientId: number, vaccinationId: number, resource: string): Observable<string> {
    const tenantId: number = this.tenantService.getTenantId()
    const facilityId: number = this.facilityService.getFacilityId()
    return this.postVaccination(tenantId,facilityId,patientId,vaccinationId,resource)
  }

  postVaccination(tenantId: number, facilityId: number, patientId: number, vaccinationId: number, resource: string): Observable<string> {
    return this.http.post<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/${vaccinationId}/vxu`,
      resource,
      httpOptions);
  }

  quickGetPatient(patientId: number): Observable<string> {
    const tenantId: number = this.tenantService.getTenantId()
    const facilityId: number = this.facilityService.getFacilityId()
    return this.getPatient(tenantId,facilityId,patientId)
  }

  getPatient(tenantId: number, facilityId: number, patientId: number): Observable<string> {
    return this.http.get(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/fhir`,
      { responseType: 'text' });
  }

  quickPostPatient(patientId: number, vaccinationId: number, resource: string): Observable<string> {
    const tenantId: number = this.tenantService.getTenantId()
    const facilityId: number = this.facilityService.getFacilityId()
    return this.postPatient(tenantId,facilityId,patientId,resource)
  }

  postPatient(tenantId: number, facilityId: number, patientId: number,resource: string): Observable<string> {
    return this.http.post<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/fhir`,
      resource,
      httpOptions);
  }

}
