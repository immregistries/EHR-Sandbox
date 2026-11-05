import {HttpClient, HttpParams, HttpResponse} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Revision, VaccinationEvent} from '../models';
import {ApiBase, JSON_HEADERS, TEXT_OPTIONS} from './api.base';

@Injectable({providedIn: 'root'})
export class VaccinationService extends ApiBase {
  private readonly http = inject(HttpClient);

  readVaccinations(
    tenantId: number,
    facilityId: number,
    patientId: number,
  ): Observable<VaccinationEvent[]> {
    return this.http.get<VaccinationEvent[]>(
      this.patientUrl(tenantId, facilityId, patientId, '/vaccinations'),
      JSON_HEADERS,
    );
  }

  readVaccinationsFromFacility(
    tenantId: number,
    facilityId: number,
  ): Observable<VaccinationEvent[]> {
    return this.http.get<VaccinationEvent[]>(
      this.facilityUrl(tenantId, facilityId, '/vaccinations'),
      JSON_HEADERS,
    );
  }

  readVaccination(
    tenantId: number,
    facilityId: number,
    patientId: number,
    vaccinationId: number,
  ): Observable<VaccinationEvent> {
    return this.http.get<VaccinationEvent>(
      this.patientUrl(tenantId, facilityId, patientId, `/vaccinations/${vaccinationId}`),
      JSON_HEADERS,
    );
  }

  readVaccinationFromFacility(
    tenantId: number,
    facilityId: number,
    vaccinationId: number,
  ): Observable<VaccinationEvent> {
    return this.http.get<VaccinationEvent>(
      this.facilityUrl(tenantId, facilityId, `/vaccinations/${vaccinationId}`),
      JSON_HEADERS,
    );
  }

  readRandom(tenantId: number, facilityId: number, patientId: number): Observable<VaccinationEvent> {
    return this.http.get<VaccinationEvent>(
      this.patientUrl(tenantId, facilityId, patientId, '/vaccinations/$random'),
      JSON_HEADERS,
    );
  }

  postVaccination(
    tenantId: number,
    facilityId: number,
    patientId: number,
    vaccination: VaccinationEvent,
    params?: HttpParams,
  ): Observable<HttpResponse<string>> {
    return this.http.post(
      this.patientUrl(tenantId, facilityId, patientId, '/vaccinations'),
      this.solveClinicianRedundancy(vaccination),
      {...TEXT_OPTIONS, observe: 'response', params},
    );
  }

  putVaccination(
    tenantId: number,
    facilityId: number,
    patientId: number,
    vaccination: VaccinationEvent,
  ): Observable<VaccinationEvent> {
    return this.http.put<VaccinationEvent>(
      this.patientUrl(tenantId, facilityId, patientId, '/vaccinations'),
      this.solveClinicianRedundancy(vaccination),
      JSON_HEADERS,
    );
  }

  readVaccinationHistory(
    tenantId: number,
    facilityId: number,
    vaccinationId: number,
  ): Observable<Revision<VaccinationEvent>[]> {
    return this.http.get<Revision<VaccinationEvent>[]>(
      this.facilityUrl(tenantId, facilityId, `/vaccinations/${vaccinationId}/$history`),
      JSON_HEADERS,
    );
  }

  lotNumberValidation(
    tenantId: number,
    facilityId: number,
    lotNumber: string,
    cvx: string,
    mvx: string,
  ): Observable<HttpResponse<string>> {
    return this.http.get(this.facilityUrl(tenantId, facilityId, '/vaccinations/$lotNumberValidation'), {
      ...TEXT_OPTIONS,
      observe: 'response',
      params: new HttpParams().set('lotNumber', lotNumber).set('cvx', cvx).set('mvx', mvx),
    });
  }

  /** Clinicians already stored by id must not be re-sent as full objects. */
  private solveClinicianRedundancy(vaccination: VaccinationEvent): VaccinationEvent {
    return vaccination;
  }
}
