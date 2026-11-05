import {HttpClient, HttpParams, HttpResponse} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {EhrPatient, Revision} from '../models';
import {ApiBase, JSON_HEADERS, TEXT_OPTIONS} from './api.base';

@Injectable({providedIn: 'root'})
export class PatientService extends ApiBase {
  private readonly http = inject(HttpClient);

  readPatients(tenantId: number, facilityId: number): Observable<EhrPatient[]> {
    return this.http.get<EhrPatient[]>(
      this.facilityUrl(tenantId, facilityId, '/patients'),
      JSON_HEADERS,
    );
  }

  readAllPatients(tenantId: number): Observable<EhrPatient[]> {
    return this.http.get<EhrPatient[]>(this.tenantUrl(tenantId, '/patients'), JSON_HEADERS);
  }

  readPatient(tenantId: number, facilityId: number, patientId: number): Observable<EhrPatient> {
    return this.http.get<EhrPatient>(
      this.patientUrl(tenantId, facilityId, patientId),
      JSON_HEADERS,
    );
  }

  readPatientHistory(
    tenantId: number,
    facilityId: number,
    patientId: number,
  ): Observable<Revision<EhrPatient>[]> {
    return this.http.get<Revision<EhrPatient>[]>(
      this.patientUrl(tenantId, facilityId, patientId, '/$history'),
      JSON_HEADERS,
    );
  }

  readRandom(tenantId?: number, facilityId?: number): Observable<EhrPatient> {
    if (tenantId && facilityId && facilityId > 0) {
      return this.http.get<EhrPatient>(
        this.facilityUrl(tenantId, facilityId, '/$random_patient'),
        JSON_HEADERS,
      );
    }
    return this.http.get<EhrPatient>(this.url('/$random_patient'), JSON_HEADERS);
  }

  postPatient(
    tenantId: number,
    facilityId: number,
    patient: EhrPatient,
    params?: HttpParams,
  ): Observable<HttpResponse<string>> {
    return this.http.post(this.facilityUrl(tenantId, facilityId, '/patients'), patient, {
      ...TEXT_OPTIONS,
      observe: 'response',
      params,
    });
  }

  postPatientOnFacilityOnly(
    facilityId: number,
    patient: EhrPatient,
  ): Observable<HttpResponse<string>> {
    return this.http.post(this.url(`/facilities/${facilityId}/patients`), patient, {
      ...TEXT_OPTIONS,
      observe: 'response',
    });
  }

  putPatient(tenantId: number, facilityId: number, patient: EhrPatient): Observable<EhrPatient> {
    return this.http.put<EhrPatient>(
      this.facilityUrl(tenantId, facilityId, '/patients'),
      patient,
      JSON_HEADERS,
    );
  }

  populatePatient(tenantId: number, facilityId: number, patientId: number): Observable<string> {
    return this.http.get(
      this.patientUrl(tenantId, facilityId, patientId, '/$populate'),
      TEXT_OPTIONS,
    );
  }
}
