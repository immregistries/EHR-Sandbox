import {HttpClient, HttpParams} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {EhrFhirOutcome} from '../../models';
import {ApiBase, JSON_HEADERS, TEXT_OPTIONS} from '../api.base';

export type FhirOperation = 'Create' | 'Update' | 'UpdateOrCreate';

/** Sends local resources to the configured immunization registry over FHIR. */
@Injectable({providedIn: 'root'})
export class FhirClientService extends ApiBase {
  private readonly http = inject(HttpClient);

  private registryParams(registryId?: number, extra?: Record<string, string | number>): HttpParams {
    let params = new HttpParams();
    if (registryId && registryId > 0) {
      params = params.set('registryId', registryId);
    }
    for (const [key, value] of Object.entries(extra ?? {})) {
      params = params.set(key, value);
    }
    return params;
  }

  matchPatient(
    tenantId: number,
    facilityId: number,
    patientId: number,
    registryId?: number,
  ): Observable<string> {
    return this.http.post(this.patientUrl(tenantId, facilityId, patientId, '/fhir-client/$match'), null, {
      ...TEXT_OPTIONS,
      params: this.registryParams(registryId),
    });
  }

  matchImmunization(
    tenantId: number,
    facilityId: number,
    patientId: number,
    vaccinationId: number,
    registryId?: number,
  ): Observable<string> {
    return this.http.post(
      this.patientUrl(tenantId, facilityId, patientId, `/vaccinations/${vaccinationId}/fhir-client/$match`),
      null,
      {...TEXT_OPTIONS, params: this.registryParams(registryId)},
    );
  }

  transaction(
    tenantId: number,
    facilityId: number,
    resource: string,
    registryId?: number,
  ): Observable<string> {
    return this.http.post(
      this.facilityUrl(tenantId, facilityId, '/fhir-client/$transaction'),
      resource,
      {...TEXT_OPTIONS, params: this.registryParams(registryId)},
    );
  }

  immdsForecast(
    tenantId: number,
    facilityId: number,
    patientId: number,
    registryId?: number,
  ): Observable<string> {
    return this.http.post(
      this.patientUrl(tenantId, facilityId, patientId, '/fhir-client/$immds-forecast'),
      null,
      {...TEXT_OPTIONS, params: this.registryParams(registryId)},
    );
  }

  sendPatient(
    tenantId: number,
    facilityId: number,
    patientId: number,
    resource: string,
    operation: FhirOperation,
    registryId?: number,
  ): Observable<EhrFhirOutcome> {
    return this.send(
      this.patientUrl(tenantId, facilityId, patientId, '/fhir-client'),
      resource,
      operation,
      registryId,
    );
  }

  sendImmunization(
    tenantId: number,
    facilityId: number,
    patientId: number,
    vaccinationId: number,
    resource: string,
    operation: FhirOperation,
    registryId?: number,
    patientFhirId?: string,
  ): Observable<EhrFhirOutcome> {
    return this.send(
      this.patientUrl(tenantId, facilityId, patientId, `/vaccinations/${vaccinationId}/fhir-client`),
      resource,
      operation,
      registryId,
      patientFhirId ? {patientFhirId} : undefined,
    );
  }

  sendOrganization(
    tenantId: number,
    resource: string,
    operation: FhirOperation,
    registryId?: number,
  ): Observable<EhrFhirOutcome> {
    return this.send(this.tenantUrl(tenantId, '/fhir-client'), resource, operation, registryId);
  }

  sendGroup(
    tenantId: number,
    resource: string,
    operation: FhirOperation,
    registryId?: number,
  ): Observable<EhrFhirOutcome> {
    return this.send(this.tenantUrl(tenantId, '/fhir-client'), resource, operation, registryId);
  }

  sendPractitioner(
    tenantId: number,
    clinicianId: number,
    resource: string,
    operation: FhirOperation,
    registryId?: number,
  ): Observable<EhrFhirOutcome> {
    return this.send(
      this.tenantUrl(tenantId, `/clinicians/${clinicianId}/fhir-client`),
      resource,
      operation,
      registryId,
    );
  }

  /** POST creates, PUT updates; `UpdateOrCreate` is resolved server-side on PUT. */
  private send(
    url: string,
    resource: string,
    operation: FhirOperation,
    registryId?: number,
    extra?: Record<string, string | number>,
  ): Observable<EhrFhirOutcome> {
    const options = {...JSON_HEADERS, params: this.registryParams(registryId, extra)};
    return operation === 'Create'
      ? this.http.post<EhrFhirOutcome>(url, resource, options)
      : this.http.put<EhrFhirOutcome>(url, resource, options);
  }
}
