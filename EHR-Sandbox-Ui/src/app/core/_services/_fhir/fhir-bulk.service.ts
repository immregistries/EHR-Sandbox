import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { SettingsService } from '../settings.service';
import { FacilityService } from '../facility.service';
import { TenantService } from '../tenant.service';
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
export class FhirBulkService {

  constructor(private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService,
    private registryService: ImmunizationRegistryService) { }


  groupExportSynch(groupId: string, paramsString: string): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    return this.http.get(
      `${this.settings.getApiUrl()}/registry/${registryId}/Group/${groupId}/$export-synch?${paramsString}`,
      {
        responseType: 'text',
      });
  }

  groupExportAsynch(groupId: string, paramsString: string): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    return this.http.get(
      `${this.settings.getApiUrl()}/registry/${registryId}/Group/${groupId}/$export-asynch?${paramsString}`,
      {
        responseType: 'text',
      });
  }

  groupExportStatus(contentUrl: string): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    return this.http.get(
      `${this.settings.getApiUrl()}/registry/${registryId}/$export-status`,
      {
        responseType: 'text',
        params: {
          contentUrl: contentUrl
        }
      });
  }

  groupExportDelete(contentUrl: string): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    return this.http.delete(
      `${this.settings.getApiUrl()}/registry/${registryId}/$export-status`,
      {
        responseType: 'text',
        params: {
          contentUrl: contentUrl
        }
      });
  }

  groupNdJson(contentUrl: string, loadInFacility: boolean): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    if (loadInFacility) {
      const facilityId = this.facilityService.getCurrentId()
      if ( facilityId > -1) {
        return this.http.get(
          `${this.settings.getApiUrl()}/registry/${registryId}/$export-result`,
          {
            responseType: 'text',
            params: {
              contentUrl: contentUrl,
              loadInFacility: facilityId
            }
          });
        } else {
          return of("")
        }
      } else {
        return this.http.get(
          `${this.settings.getApiUrl()}/registry/${registryId}/$export-result`,
          {
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
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/registry/${registryId}/$loadNdJson`, body);
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
