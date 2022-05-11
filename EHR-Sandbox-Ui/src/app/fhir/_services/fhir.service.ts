import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { SettingsService } from '../../core/_services/settings.service';
import { FacilityService } from '../../core/_services/facility.service';
import { TenantService } from '../../core/_services/tenant.service';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
@Injectable({
  providedIn: 'root'
})
/**
 * Fhir service interacting with the API to parse and serialize resources, and interact with IIS's
 */
export class FhirService {

  constructor(private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService ) { }

  /**
   *
   * @param patientId
   * @param vaccinationId
   * @returns Vaccination information converted to XML FHIR Immunization resource
   */
  quickGetImmunizationResource(patientId: number, vaccinationId: number): Observable<string> {
    const tenantId: number = this.tenantService.getTenantId()
    const facilityId: number = this.facilityService.getFacilityId()
    return this.getImmunizationResource(tenantId,facilityId,patientId,vaccinationId)
  }

  getImmunizationResource(tenantId: number, facilityId: number, patientId: number, vaccinationId: number): Observable<string> {
    return this.http.get(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/${vaccinationId}/resource`,
      { responseType: 'text' });
  }

  quickPostImmunization(patientId: number, vaccinationId: number, resource: string): Observable<string> {
    const tenantId: number = this.tenantService.getTenantId()
    const facilityId: number = this.facilityService.getFacilityId()
    return this.postImmunization(tenantId,facilityId,patientId,vaccinationId,resource)
  }

  postImmunization(tenantId: number, facilityId: number, patientId: number, vaccinationId: number, resource: string): Observable<string> {
    return this.http.post<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/${vaccinationId}/fhir`,
      resource,
      httpOptions);
  }

  quickGetPatientResource(patientId: number): Observable<string> {
    const tenantId: number = this.tenantService.getTenantId()
    const facilityId: number = this.facilityService.getFacilityId()
    return this.getPatientResource(tenantId,facilityId,patientId)
  }

  getPatientResource(tenantId: number, facilityId: number, patientId: number): Observable<string> {
    if (tenantId < 0 || facilityId < 0 || patientId < 0 ){
      return of('')
    } else {
      return this.http.get(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/resource`,
        { responseType: 'text' });
    }

  }

  quickPostPatient(patientId: number, resource: string): Observable<string> {
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

  getFromIIS(resourceType: string, identifier: string): Observable<string> {
    return this.http.get(
      `${this.settings.getApiUrl()}/iis/${resourceType}/${identifier}`,
      { responseType: 'text' });
  }

}
