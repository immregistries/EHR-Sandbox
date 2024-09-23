import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { SettingsService } from '../settings.service';
import { FacilityService } from '../facility.service';
import { TenantService } from '../tenant.service';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';
import { Identifier } from 'fhir/r5';
import { VaccinationEvent } from 'src/app/core/_model/rest';
import { SubscriptionService } from './subscription.service';
import { IdUrlVerifyingService } from '../_abstract/id-url-verifying.service';
import { SnackBarService } from '../snack-bar.service';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
@Injectable({
  providedIn: 'root'
})
/**
 * Fhir service interacting with the API to parse and serialize resources, and interact with IIS's
 */
export class FhirClientService extends IdUrlVerifyingService {

  constructor(
    snackBarService: SnackBarService,
    private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService,
    private registryService: ImmunizationRegistryService,
    private subscriptionService: SubscriptionService,
    // private service: Service
  ) {
    super(snackBarService)
  }

  postResource(type: string, resource: string, operation: "Create" | "Update" | "UpdateOrCreate" | "$match" | "$transaction" | "", resourceLocalId: number, parentId: number, overridingReferences?: { [reference: string]: string }): Observable<string> {
    if (operation == "$match") {
      return this.matchResource(type, resource, resourceLocalId, parentId);
    } else if (operation == "$transaction") {
      return this.transaction(resource, resourceLocalId);
    } else if (operation == "") {
      return this.transaction(resource, resourceLocalId);
    }
    switch (type) {
      case "Patient": {
        return this.quickPostPatient(resourceLocalId, resource, operation);
        break;
      }
      case "Immunization": {
        return this.quickPostImmunization(
          parentId, resourceLocalId, resource, operation,
          overridingReferences ? overridingReferences['patient'] : '');
        break;
      }
      case "Practitionner": {
        return this.quickPostPractitioner(
          resourceLocalId, resource, operation);
        break;
      }
      case "Group": {
        return this.postGroup(resource, operation, resourceLocalId);
        break;
      }
      case "Subscription": {
        return this.subscriptionService.postSubscription(resource);
        break;
      }
      case "Organization": {
        return this.sendOrganization(resource, operation);
        break;
      }
    }
    return of("");
  }

  matchResource(type: string, resource: string, resourceId: number, parentId: number): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    switch (type) {
      case "Patient": {
        return this.http.post<string>(
          `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${resourceId}/fhir-client/registry/${registryId}/$match`,
          resource,
          httpOptions);
      }
      case "Immunization": {
        return this.http.post<string>(
          `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${parentId}/vaccinations/${resourceId}/fhir-client/registry/${registryId}/$match`,
          resource,
          httpOptions);
      }
    }
    return of("");
  }

  shlink(url: string): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    const tenantId: number = this.tenantService.getCurrentId()
    return this.http.post<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/registry/${registryId}/$import-shlink`,
      url,
      httpOptions);
  }

  transaction(resource: string, facilityId: number): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    const tenantId: number = this.tenantService.getCurrentId()
    return this.http.post<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/fhir-client/registry/${registryId}/$transaction`,
      resource,
      httpOptions);
  }

  sendOrganization(resource: string, operation: "Create" | "Update" | "UpdateOrCreate"): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    if (this.idsNotValid(tenantId)) {
      return of("")
    }
    switch (operation) {
      case "Create": {
        return this.http.post<string>(
          `${this.settings.getApiUrl()}/tenants/${tenantId}/registry/${registryId}`,
          resource,
          {
            ...httpOptions,
            params: { "type": "Organization" }
          });
      }
      case "UpdateOrCreate":
      case "Update":
      default:
        return this.http.put<string>(
          `${this.settings.getApiUrl()}/tenants/${tenantId}/registry/${registryId}`,
          resource,
          {
            ...httpOptions,
            params: { "type": "Organization" }
          });
    }
  }

  postGroup(resource: string, operation: "Create" | "Update" | "UpdateOrCreate", resourceId: number): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    const tenantId: number = this.tenantService.getCurrentId()
    if (this.idsNotValid(tenantId)) {
      return of("")
    }
    return this.http.post<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/registry/${registryId}`,
      resource,
      { ...httpOptions, params: new HttpParams().append("type", "Group") });
  }

  quickPostImmunization(patientId: number, vaccinationId: number, resource: string, operation: "Create" | "Update" | "UpdateOrCreate", patientFhirId?: string): Observable<string> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    switch (operation) {
      case "Create": {
        return this.postImmunization(tenantId, facilityId, patientId, vaccinationId, resource, patientFhirId)
      }
      case "UpdateOrCreate":
      case "Update":
      default:
        return this.putImmunization(tenantId, facilityId, patientId, vaccinationId, resource, patientFhirId)
    }
  }

  quickPostPractitioner(clinicianId: number, resource: string, operation: "Create" | "Update" | "UpdateOrCreate"): Observable<string> {
    const tenantId: number = this.tenantService.getCurrentId()
    switch (operation) {
      case "Create": {
        return this.postPractitioner(tenantId, clinicianId, resource)
      }
      case "UpdateOrCreate":
      case "Update":
      default:
        return this.putPractitioner(tenantId, clinicianId, resource)
    }
  }

  postPractitioner(tenantId: number, clinicianId: number, resource: string): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    return this.http.post<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/clinicians/${clinicianId}/fhir-client/registry/${registryId}`,
      resource);
  }
  putPractitioner(tenantId: number, clinicianId: number, resource: string): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    return this.http.put<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/clinicians/${clinicianId}/fhir-client/registry/${registryId}`,
      resource);
  }

  postImmunization(tenantId: number, facilityId: number, patientId: number, vaccinationId: number, resource: string, patientFhirId?: string): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    return this.http.post<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/${vaccinationId}/fhir-client/registry/${registryId}`,
      resource,
      this.immunizationOptions(patientFhirId));
  }

  putImmunization(tenantId: number, facilityId: number, patientId: number, vaccinationId: number, resource: string, patientFhirId?: string): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    return this.http.put<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/${vaccinationId}/fhir-client/registry/${registryId}`,
      resource,
      this.immunizationOptions(patientFhirId),
    );
  }

  quickPostPatient(patientId: number, resource: string, operation: "Create" | "Update" | "UpdateOrCreate"): Observable<string> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    switch (operation) {
      case "Create": {
        return this.postPatient(tenantId, facilityId, patientId, resource)
      }
      case "UpdateOrCreate":
      case "Update":
      default:
        return this.putPatient(tenantId, facilityId, patientId, resource)
    }
  }

  putPatient(tenantId: number, facilityId: number, patientId: number, resource: string): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    return this.http.put<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/fhir-client/registry/${registryId}`,
      resource,
      httpOptions);
  }

  postPatient(tenantId: number, facilityId: number, patientId: number, resource: string): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    return this.http.post<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/fhir-client/registry/${registryId}`,
      resource,
      httpOptions);
  }

  loadEverythingFromPatient(patientId: number, registryId?: number): Observable<VaccinationEvent[]> {
    if (!registryId) {
      registryId = this.registryService.getCurrentId()
    }
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.http.get<VaccinationEvent[]>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/fhir-client/registry/${registryId}/$fetchAndLoad`,
      httpOptions);
  }

  getFromIIS(resourceType: string, identifier: string): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    const tenantId = this.tenantService.getCurrentId()
    if (this.idsNotValid(tenantId)) {
      return of("")
    }
    return this.http.get(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/registry/${registryId}/${resourceType}${identifier ? '/' + identifier : ''}`,
      { responseType: 'text' });
  }

  // get(urlEnd: string): Observable<string> {
  //   const registryId = this.immRegistries.getregistryId()
  //   return this.http.get(
  //     `${this.settings.getApiUrl()}/registry/${registryId}${urlEnd}`,
  //     { responseType: 'text' });
  // }

  search(resourceType: string, identifier: Identifier): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    return this.http.post(
      `${this.settings.getApiUrl()}/registry/${registryId}/${resourceType}/search`,
      identifier,
      { responseType: 'text' });
  }


  operation(operationType: string, target: string, parameters: string): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    return this.http.post(
      `${this.settings.getApiUrl()}/registry/${registryId}/operation/${target}/${operationType}${parameters.length > 0 ? parameters : ''}`,
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
    if (patientFhirId && patientFhirId.length > 0) {
      return {
        ...httpOptions,
        params: {
          patientFhirId: patientFhirId
        }
      }
    } else {
      return httpOptions
    }
  }
}
