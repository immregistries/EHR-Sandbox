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

  quickPostImmunization(patientId: number, vaccinationId: number, resource: string, operation: "Create" | "Update" | "UpdateOrCreate", patientFhirId: string): Observable<string> {
    const tenantId: number = this.tenantService.getTenantId()
    const facilityId: number = this.facilityService.getFacilityId()
    switch(operation) {
      case "Create" : {
        return this.postImmunization(tenantId,facilityId,patientId,vaccinationId,resource,patientFhirId)
      }
      case "UpdateOrCreate" :
      case "Update" :
      default :
        return this.putImmunization(tenantId,facilityId,patientId,vaccinationId,resource,patientFhirId)
    }
  }

  postImmunization(tenantId: number, facilityId: number, patientId: number, vaccinationId: number, resource: string, patientFhirId: string): Observable<string> {
    let options = {
      headers: httpOptions.headers,
      params: {
        patientFhirId: patientFhirId
      }
    }
    if (patientFhirId.length>0){
      options.params = {
        patientFhirId: patientFhirId
      }
    }
    return this.http.post<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/${vaccinationId}/fhir`,
      resource,
      options);
  }

  putImmunization(tenantId: number, facilityId: number, patientId: number, vaccinationId: number, resource: string, patientFhirId: string): Observable<string> {
    let options = {
      headers: httpOptions.headers,
      params: {
        patientFhirId: patientFhirId
      }
    }
    if (patientFhirId.length>0){
      options.params = {
        patientFhirId: patientFhirId
      }
    }
    return this.http.put<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/${vaccinationId}/fhir`,
      resource,
      options,
      );
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

  quickPostPatient(patientId: number, resource: string, operation: "Create" | "Update" | "UpdateOrCreate" ): Observable<string> {
    const tenantId: number = this.tenantService.getTenantId()
    const facilityId: number = this.facilityService.getFacilityId()
    switch(operation) {
      case "Create" : {
        return this.postPatient(tenantId,facilityId,patientId,resource)
      }
      case "UpdateOrCreate" :
      case "Update" :
      default :
        return this.putPatient(tenantId,facilityId,patientId,resource)
    }
  }

  putPatient(tenantId: number, facilityId: number, patientId: number,resource: string): Observable<string> {
    return this.http.put<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/fhir`,
      resource,
      httpOptions);
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

readOperationOutcome(): Observable<any> {
    const tenantId: number = this.tenantService.getTenantId()
    const facilityId: number = this.facilityService.getFacilityId()
    return this.http.get<any>(
   `${this.settings.getApiUrl()}/fhir/${facilityId}`,
      httpOptions);
  }



}
