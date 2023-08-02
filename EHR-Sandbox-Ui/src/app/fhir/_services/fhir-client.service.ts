import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { SettingsService } from '../../core/_services/settings.service';
import { FacilityService } from '../../core/_services/facility.service';
import { TenantService } from '../../core/_services/tenant.service';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';
import { Identifier } from 'fhir/r5';
import { VaccinationEvent } from 'src/app/core/_model/rest';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
@Injectable({
  providedIn: 'root'
})
/**
 * Fhir service interacting with the API to parse and serialize resources, and interact with IIS's
 */
export class FhirClientService {

  constructor(private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService,
    private immRegistries: ImmunizationRegistryService) { }

  postResource(type: string,resource: string, operation: "Create" | "Update" | "UpdateOrCreate" | "$match", resourceLocalId: number, parentId: number, overridingReferences?: {[reference: string]: string}): Observable<string> {
    if (operation == "$match") {
      return this.matchResource(type,resource,resourceLocalId,parentId);
    }
    switch(type){
      case "Patient": {
        return this.quickPostPatient(resourceLocalId,resource,operation);
        break;
      }
      case "Immunization": {
        return this.quickPostImmunization(
          parentId, resourceLocalId, resource, operation,
          overridingReferences? overridingReferences['patient'] : '');
        break;
      }
      case "Practitionner": {
        break;
      }
      case "Group": {
        return this.postGroup(resource,operation,resourceLocalId);
        break;
      }
    }
    return of("");
  }

  matchResource(type: string,resource: string, resourceId: number,parentId: number): Observable<string> {
    const registryId = this.immRegistries.getCurrentId()
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    switch(type){
      case "Patient": {
        return this.http.post<string>(
          `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${resourceId}/fhir-client/imm-registry/${registryId}/$match`,
          resource,
          httpOptions);
      }
      case "Immunization": {
        return this.http.post<string>(
          `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${parentId}/vaccinations/${resourceId}/fhir-client/imm-registry/${registryId}/$match`,
          resource,
          httpOptions);
      }
    }
    return of("");
  }

  postGroup(resource: string, operation: "Create" | "Update" | "UpdateOrCreate", resourceId: number): Observable<string> {
    const registryId = this.immRegistries.getCurrentId()
    const options = {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
      params: new HttpParams().append("type", "Group")
    };
    return this.http.post<string>(
      `${this.settings.getApiUrl()}/imm-registry/${registryId}`,
      resource,
      options);
  }

  quickPostImmunization(patientId: number, vaccinationId: number, resource: string, operation: "Create" | "Update" | "UpdateOrCreate", patientFhirId?: string): Observable<string> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
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

  postImmunization(tenantId: number, facilityId: number, patientId: number, vaccinationId: number, resource: string, patientFhirId?: string): Observable<string> {
    const registryId = this.immRegistries.getCurrentId()
    return this.http.post<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/${vaccinationId}/fhir-client/imm-registry/${registryId}`,
      resource,
      this.immunizationOptions(patientFhirId));
  }

  putImmunization(tenantId: number, facilityId: number, patientId: number, vaccinationId: number, resource: string, patientFhirId?: string): Observable<string> {
    const registryId = this.immRegistries.getCurrentId()
    return this.http.put<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/${vaccinationId}/fhir-client/imm-registry/${registryId}`,
      resource,
      this.immunizationOptions(patientFhirId),
      );
  }

  quickPostPatient(patientId: number, resource: string, operation: "Create" | "Update" | "UpdateOrCreate" ): Observable<string> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
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
    const registryId = this.immRegistries.getCurrentId()
    return this.http.put<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/fhir-client/imm-registry/${registryId}`,
      resource,
      httpOptions);
  }

  postPatient(tenantId: number, facilityId: number, patientId: number,resource: string): Observable<string> {
    const registryId = this.immRegistries.getCurrentId()
    return this.http.post<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/fhir-client/imm-registry/${registryId}`,
      resource,
      httpOptions);
  }

  loadEverythingFromPatient(patientId: number): Observable<VaccinationEvent[]> {
    const registryId = this.immRegistries.getCurrentId()
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.http.get<VaccinationEvent[]>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/fhir-client/imm-registry/${registryId}/$fetchAndLoad`,
      httpOptions);
  }

  getFromIIS(resourceType: string, identifier: string): Observable<string> {
    const registryId = this.immRegistries.getCurrentId()
    return this.http.get(
      `${this.settings.getApiUrl()}/imm-registry/${registryId}/${resourceType}/${identifier}`,
      { responseType: 'text' });
  }

  // get(urlEnd: string): Observable<string> {
  //   const registryId = this.immRegistries.getregistryId()
  //   return this.http.get(
  //     `${this.settings.getApiUrl()}/imm-registry/${registryId}${urlEnd}`,
  //     { responseType: 'text' });
  // }

  search(resourceType: string, identifier: Identifier): Observable<string> {
    const registryId = this.immRegistries.getCurrentId()
    return this.http.post(
      `${this.settings.getApiUrl()}/imm-registry/${registryId}/${resourceType}/search`,
      identifier,
      { responseType: 'text' });
  }


  operation(operationType: string, target: string ,parameters: string): Observable<string> {
    const registryId = this.immRegistries.getCurrentId()
    return this.http.post(
      `${this.settings.getApiUrl()}/imm-registry/${registryId}/operation/${target}/${operationType}${parameters.length > 0? parameters: ''}`,
      parameters,
      {
        responseType: 'text',
        // params: {
        //   parameters:
        // }
      });
  }

  private immunizationOptions(patientFhirId?: string) {
    const options = {
      headers: httpOptions.headers,
      params: {}
    }
    if (patientFhirId && patientFhirId.length > 0){
      options.params = {
        patientFhirId: patientFhirId
      }
    }
    return options
  }
}
