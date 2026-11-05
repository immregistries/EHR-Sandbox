import {HttpClient} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {ApiBase, TEXT_OPTIONS} from '../api.base';

/** Reads the FHIR representation of local EHR resources. */
@Injectable({providedIn: 'root'})
export class FhirResourceService extends ApiBase {
  private readonly http = inject(HttpClient);

  getPatientResource(tenantId: number, facilityId: number, patientId: number): Observable<string> {
    return this.http.get(this.patientUrl(tenantId, facilityId, patientId, '/resource'), TEXT_OPTIONS);
  }

  getImmunizationResource(
    tenantId: number,
    facilityId: number,
    patientId: number,
    vaccinationId: number,
  ): Observable<string> {
    return this.http.get(
      this.patientUrl(tenantId, facilityId, patientId, `/vaccinations/${vaccinationId}/resource`),
      TEXT_OPTIONS,
    );
  }

  getPatientIps(tenantId: number, facilityId: number, patientId: number): Observable<string> {
    return this.http.get(this.patientUrl(tenantId, facilityId, patientId, '/ips'), TEXT_OPTIONS);
  }

  getGroupResource(tenantId: number, facilityId: number, groupId: number): Observable<string> {
    return this.http.get(
      this.facilityUrl(tenantId, facilityId, `/groups/${groupId}/resource`),
      TEXT_OPTIONS,
    );
  }

  getOrganizationResource(tenantId: number, facilityId: number): Observable<string> {
    return this.http.get(this.facilityUrl(tenantId, facilityId, '/resource'), TEXT_OPTIONS);
  }

  getClinicianResource(tenantId: number, clinicianId: number): Observable<string> {
    return this.http.get(
      this.tenantUrl(tenantId, `/clinicians/${clinicianId}/resource`),
      TEXT_OPTIONS,
    );
  }

  getQrCode(tenantId: number, facilityId: number, resource: string): Observable<string[]> {
    return this.http.post<string[]>(this.facilityUrl(tenantId, facilityId, '/$qrCode'), resource);
  }

  getPatientExportBundle(
    tenantId: number,
    facilityId: number,
    patientId: number,
  ): Observable<string> {
    return this.http.get(this.patientUrl(tenantId, facilityId, patientId, '/bundle'), TEXT_OPTIONS);
  }

  getVaccinationExportBundle(
    tenantId: number,
    facilityId: number,
    patientId: number,
    vaccinationId: number,
  ): Observable<string> {
    return this.http.get(
      this.patientUrl(tenantId, facilityId, patientId, `/vaccinations/${vaccinationId}/bundle`),
      TEXT_OPTIONS,
    );
  }

  getFacilityExportBundle(tenantId: number, facilityId: number): Observable<string> {
    return this.http.get(this.facilityUrl(tenantId, facilityId, '/bundle'), TEXT_OPTIONS);
  }
}
