import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { SettingsService } from '../settings.service';
import { FacilityService } from '../facility.service';
import { TenantService } from '../tenant.service';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';
import { Identifier } from 'fhir/r5';
import { VaccinationEvent } from 'src/app/core/_model/rest';
import { SnackBarService } from '../snack-bar.service';
import { IdUrlVerifyingService } from '../_abstract/id-url-verifying.service';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
@Injectable({
  providedIn: 'root'
})
/**
 * Fhir service interacting with the API to parse and serialize resources, and interact with IIS's
 */
export class FhirBulkService extends IdUrlVerifyingService {

  constructor(
    snackBarService: SnackBarService,
    private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService,
    private registryService: ImmunizationRegistryService) {
    super(snackBarService)
  }


  groupExportSynch(groupId: string, paramsString: string): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    const tenantId = this.tenantService.getCurrentId()
    if (this.idsNotValid(tenantId)) {
      return of("")
    }
    return this.http.get(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/registry/${registryId}/Group/${groupId}/$export-synch?${paramsString}`,
      {
        ...httpOptions,
        responseType: 'text',
      });

  }

  groupExportAsynch(groupId: string, paramsString: string): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    const tenantId = this.tenantService.getCurrentId()
    if (this.idsNotValid(tenantId)) {
      return of("")
    }
    return this.http.get(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/registry/${registryId}/Group/${groupId}/$export-asynch?${paramsString}`,
      {
        ...httpOptions,
        responseType: 'text',
      });


  }

  groupExportStatus(contentUrl: string): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    const tenantId = this.tenantService.getCurrentId()
    if (this.idsNotValid(tenantId)) {
      return of("")
    }
    return this.http.get(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/registry/${registryId}/$export-status`,
      {
        ...httpOptions,
        responseType: 'text',
      });


  }

  groupExportDelete(contentUrl: string): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    const tenantId = this.tenantService.getCurrentId()
    if (this.idsNotValid(tenantId)) {
      return of("")
    }
    return this.http.delete(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/registry/${registryId}/$export-status`,
      {
        ...httpOptions,
        responseType: 'text',
        params: {
          contentUrl: contentUrl
        }
      });
  }

  groupNdJson(contentUrl: string, loadInFacility: boolean): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    const tenantId = this.tenantService.getCurrentId()
    if (this.idsNotValid(tenantId)) {
      return of("")
    }
    if (loadInFacility) {
      const facilityId = this.facilityService.getCurrentId()
      if (this.idsNotValid(facilityId)) {
        return of("")
      }
      return this.http.get(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/registry/${registryId}/$export-result`,
        {
          ...httpOptions,
          responseType: 'text',
          params: {
            contentUrl: contentUrl,
            loadInFacility: facilityId
          }
        });
    } else {
      return this.http.get(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/registry/${registryId}/$export-result`,
        {
          ...httpOptions,
          responseType: 'text',
          params: {
            contentUrl: contentUrl
          }
        });
    }
  }

  loadNdJson(body: string): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()

    return this.http.post<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/registry/${registryId}/$loadNdJson`, body, httpOptions);
  }

  // viewResult(url: string): Observable<string> {
  //   const registryId = this.registryService.getCurrentId()
  //   const tenantId: number = this.tenantService.getCurrentId()
  //   const facilityId: number = this.facilityService.getCurrentId()

  //   return this.http.post<string>(
  //     `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/registry/${registryId}/$loadNdJson`, url);
  // }

  loadJson(body: string): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()

    return this.http.post<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/registry/${registryId}/$loadJson`, body);
  }
}
