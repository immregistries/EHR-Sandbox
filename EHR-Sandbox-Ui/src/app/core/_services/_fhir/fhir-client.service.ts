import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams, HttpParamsOptions, HttpResponse } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { SettingsService } from '../settings.service';
import { FacilityService } from '../facility.service';
import { TenantService } from '../tenant.service';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';
import { Identifier } from 'fhir/r5';
import { EhrFhirOutcome, VaccinationEvent } from 'src/app/core/_model/rest';
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

  postOperation(type: string, resource: string, operation: "$match" | "$transaction" | "", resourceLocalId: number, parentId: number): Observable<string> {
    if (operation == "$match") {
      return this.matchResource(type, resource, resourceLocalId, parentId);
    } else if (operation == "$transaction") {
      return this.transaction(resource, resourceLocalId);
    } else if (operation == "") {
      return this.transaction(resource, resourceLocalId);
    } else {
      return of("");
    }
  }

  postResource(type: string, resource: string, operation: "Create" | "Update" | "UpdateOrCreate", resourceLocalId: number, parentId: number, overridingReferences?: { [reference: string]: string }): Observable<EhrFhirOutcome> {
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
    return of({});
  }

  matchResource(type: string, resource: string, resourceId: number, parentId: number): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    switch (type) {
      case "Patient": {
        return this.http.post<string>(
          `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${resourceId}/fhir-client/$match`,
          resource,
          {
            ...httpOptions,
            params: {
              registryId: registryId
            }
          });
      }
      case "Immunization": {
        return this.http.post<string>(
          `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${parentId}/vaccinations/${resourceId}/fhir-client/$match`,
          resource,
          {
            ...httpOptions,
            params: {
              registryId: registryId
            }
          });
      }
    }
    return of("");
  }

  shlinkRead(url: string, password?: string, jwk?: string): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    const tenantId: number = this.tenantService.getCurrentId()
    if (this.idsNotValid(tenantId)) {
      return of()
    }
    return this.http.post<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/$read-shlink`,
      url,
      { ...httpOptions, params: { 'password': password ?? "", 'jwk': jwk ?? "" } });
  }

  importShlinkForPatient(patientId: number, url: string, password?: string, jwk?: string): Observable<VaccinationEvent[]> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    if (this.idsNotValid(tenantId, facilityId, patientId)) {
      return of()
    }
    return this.http.post<VaccinationEvent[]>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/$import-shlink`,
      url,
      { ...httpOptions, params: { 'password': password ?? "", 'jwk': jwk ?? "" } });
  }

  transaction(resource: string, facilityId: number): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    const tenantId: number = this.tenantService.getCurrentId()
    if (facilityId < 0) {
      facilityId = this.facilityService.getCurrentId()
    }
    if (this.idsNotValid(tenantId, registryId, facilityId)) {
      return of()
    }
    return this.http.post<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/fhir-client/$transaction`,
      resource,
      {
        ...httpOptions,
        params: {
          registryId: registryId
        }
      });
  }


  immdsForecast(facilityId: number, patientId: number): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    const tenantId: number = this.tenantService.getCurrentId()
    if (facilityId < 0) {
      facilityId = this.facilityService.getCurrentId()
    }
    if (this.idsNotValid(tenantId, registryId, facilityId, patientId)) {
      return of()
    }
    return this.http.get<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/fhir-client/$immds-forecast`,
      {
        ...httpOptions,
        params: {
          registryId: registryId
        }
      });
  }


  sendOrganization(resource: string, operation: "Create" | "Update" | "UpdateOrCreate"): Observable<EhrFhirOutcome> {
    const registryId = this.registryService.getCurrentId()
    const tenantId: number = this.tenantService.getCurrentId()
    if (this.idsNotValid(tenantId, registryId)) {
      return of({})
    }
    switch (operation) {
      case "Create": {
        return this.http.post<EhrFhirOutcome>(
          `${this.settings.getApiUrl()}/tenants/${tenantId}/fhir-client`,
          resource,
          {
            ...httpOptions,
            params: {
              "type": "Organization",
              registryId: registryId
            }
          });
      }
      case "UpdateOrCreate":
      case "Update":
      default:
        return this.http.put<EhrFhirOutcome>(
          `${this.settings.getApiUrl()}/tenants/${tenantId}/fhir-client`,
          resource,
          {
            ...httpOptions,
            params: {
              "type": "Organization",
              registryId: registryId
            }
          });
    }
  }

  postGroup(resource: string, operation: "Create" | "Update" | "UpdateOrCreate", resourceId: number): Observable<EhrFhirOutcome> {
    const registryId = this.registryService.getCurrentId()
    const tenantId: number = this.tenantService.getCurrentId()
    if (this.idsNotValid(tenantId)) {
      return of({})
    }
    return this.http.post<EhrFhirOutcome>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/fhir-client`,
      resource,
      {
        ...httpOptions,
        params: {
          "type": "Group",
          registryId: registryId
        }
      });
  }

  quickPostImmunization(patientId: number, vaccinationId: number, resource: string, operation: "Create" | "Update" | "UpdateOrCreate", patientFhirId?: string): Observable<EhrFhirOutcome> {
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

  quickPostPractitioner(clinicianId: number, resource: string, operation: "Create" | "Update" | "UpdateOrCreate"): Observable<EhrFhirOutcome> {
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

  postPractitioner(tenantId: number, clinicianId: number, resource: string): Observable<EhrFhirOutcome> {
    const registryId = this.registryService.getCurrentId()
    return this.http.post<EhrFhirOutcome>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/clinicians/${clinicianId}/fhir-client`,
      resource,
      {
        ...httpOptions,
        params: {
          registryId: registryId
        }
      }
    );
  }
  putPractitioner(tenantId: number, clinicianId: number, resource: string): Observable<EhrFhirOutcome> {
    const registryId = this.registryService.getCurrentId()
    return this.http.put<EhrFhirOutcome>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/clinicians/${clinicianId}/fhir-client`,
      resource,
      {
        ...httpOptions,
        params: {
          registryId: registryId
        }
      });
  }

  postImmunization(tenantId: number, facilityId: number, patientId: number, vaccinationId: number, resource: string, patientFhirId?: string): Observable<EhrFhirOutcome> {
    const registryId = this.registryService.getCurrentId()
    let options: {} = {
      ...httpOptions,
      params: {
        registryId: registryId
      }
    }
    if (patientFhirId && patientFhirId.length > 0) {
      options = {
        ...httpOptions,
        params: {
          patientFhirId: patientFhirId,
          registryId: registryId
        }
      }
    }
    return this.http.post<EhrFhirOutcome>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/${vaccinationId}/fhir-client`,
      resource,
      options);
  }

  putImmunization(tenantId: number, facilityId: number, patientId: number, vaccinationId: number, resource: string, patientFhirId?: string): Observable<EhrFhirOutcome> {
    const registryId = this.registryService.getCurrentId()
    let options: {} = {
      ...httpOptions,
      params: {
        registryId: registryId
      }
    }
    if (patientFhirId && patientFhirId.length > 0) {
      options = {
        ...httpOptions,
        params: {
          patientFhirId: patientFhirId,
          registryId: registryId
        }
      }
    }
    return this.http.put<EhrFhirOutcome>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/${vaccinationId}/fhir-client`,
      resource,
      options,
    );
  }

  quickPostPatient(patientId: number, resource: string, operation: "Create" | "Update" | "UpdateOrCreate"): Observable<EhrFhirOutcome> {
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

  putPatient(tenantId: number, facilityId: number, patientId: number, resource: string): Observable<EhrFhirOutcome> {
    const registryId = this.registryService.getCurrentId()
    return this.http.put<EhrFhirOutcome>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/fhir-client`,
      resource,
      {
        ...httpOptions,
        params: {
          registryId: registryId
        }
      });
  }

  postPatient(tenantId: number, facilityId: number, patientId: number, resource: string): Observable<EhrFhirOutcome> {
    const registryId = this.registryService.getCurrentId()
    return this.http.post<EhrFhirOutcome>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/fhir-client`,
      resource,
      {
        ...httpOptions,
        params: {
          registryId: registryId
        }
      });
  }

  loadEverythingFromPatient(patientId: number, registryId?: number): Observable<VaccinationEvent[]> {
    if (!registryId) {
      registryId = this.registryService.getCurrentId()
    }
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.http.get<VaccinationEvent[]>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/fhir-client/$fetchAndLoad`,
      {
        ...httpOptions,
        params: {
          registryId: registryId
        }
      });
  }

  getFromIIS(resourceType: string, identifier: string): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    const tenantId = this.tenantService.getCurrentId()
    if (resourceType === 'metadata') {
      return this.registryService.fhirMetadata(registryId)
    }
    if (this.idsNotValid(tenantId)) {
      return of("")
    }
    return this.http.get(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/fhir-client/${resourceType}${identifier ? '/' + identifier : ''}`,
      {
        ...httpOptions,
        responseType: 'text',
        params: { registryId: registryId }
      });
  }

  search(resourceType: string, identifier: Identifier): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    return this.http.post(
      `${this.settings.getApiUrl()}/fhir-client/${resourceType}/search`,
      identifier,
      {
        ...httpOptions,
        responseType: 'text',
        params: { registryId: registryId }
      });
  }


  operation(operationType: string, target: string, parameters: string): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    return this.http.post(
      `${this.settings.getApiUrl()}/fhir-client/operation/${target}/${operationType}${parameters.length > 0 ? parameters : ''}`,
      parameters,
      {
        ...httpOptions,
        responseType: 'text',
        params: { registryId: registryId }
      });
  }

}
