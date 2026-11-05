import {HttpClient, HttpParams} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {ApiBase, TEXT_OPTIONS} from '../api.base';

/** FHIR Bulk Data ($export) kickoff, polling and ndjson ingestion. */
@Injectable({providedIn: 'root'})
export class FhirBulkService extends ApiBase {
  private readonly http = inject(HttpClient);

  groupExportSynch(
    tenantId: number,
    groupId: string,
    paramsString: string,
    registryId: number,
  ): Observable<string> {
    return this.http.get(
      this.tenantUrl(
        tenantId,
        `/fhir-client/Group/${groupId}/$export-synch?${paramsString}&registryId=${registryId}`,
      ),
      TEXT_OPTIONS,
    );
  }

  groupExportAsynch(
    tenantId: number,
    groupId: string,
    paramsString: string,
    registryId: number,
  ): Observable<string> {
    return this.http.get(
      this.tenantUrl(
        tenantId,
        `/fhir-client/Group/${groupId}/$export-asynch?${paramsString}&registryId=${registryId}`,
      ),
      TEXT_OPTIONS,
    );
  }

  groupExportStatus(tenantId: number, contentUrl: string): Observable<string> {
    return this.http.get(this.tenantUrl(tenantId, '/fhir-client/$export-status'), {
      ...TEXT_OPTIONS,
      params: new HttpParams().set('contentUrl', contentUrl),
    });
  }

  groupExportDelete(tenantId: number, contentUrl: string): Observable<string> {
    return this.http.delete(this.tenantUrl(tenantId, '/fhir-client/$export-status'), {
      ...TEXT_OPTIONS,
      params: new HttpParams().set('contentUrl', contentUrl),
    });
  }

  groupNdJson(tenantId: number, contentUrl: string, loadInFacility: boolean): Observable<string> {
    return this.http.get(this.tenantUrl(tenantId, '/fhir-client/$export-result'), {
      ...TEXT_OPTIONS,
      params: new HttpParams().set('contentUrl', contentUrl).set('load', loadInFacility),
    });
  }

  loadNdJson(tenantId: number, facilityId: number, body: string): Observable<string> {
    return this.http.post(
      this.facilityUrl(tenantId, facilityId, '/fhir-client/$loadNdJson'),
      body,
      TEXT_OPTIONS,
    );
  }

  loadJson(tenantId: number, facilityId: number, body: string): Observable<string> {
    return this.http.post(
      this.facilityUrl(tenantId, facilityId, '/fhir-client/$loadJson'),
      body,
      TEXT_OPTIONS,
    );
  }
}
