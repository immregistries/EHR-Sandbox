import {HttpClient, HttpParams} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {AcknowledgementObject, Feedback} from '../models';
import {ApiBase, JSON_HEADERS, TEXT_OPTIONS} from './api.base';

@Injectable({providedIn: 'root'})
export class FeedbackService extends ApiBase {
  private readonly http = inject(HttpClient);

  readFacilityFeedback(tenantId: number, facilityId: number): Observable<Feedback[]> {
    return this.http.get<Feedback[]>(
      this.facilityUrl(tenantId, facilityId, '/feedbacks'),
      JSON_HEADERS,
    );
  }

  readPatientFeedback(
    tenantId: number,
    facilityId: number,
    patientId: number,
  ): Observable<Feedback[]> {
    return this.http.get<Feedback[]>(
      this.patientUrl(tenantId, facilityId, patientId, '/feedbacks'),
      JSON_HEADERS,
    );
  }

  readVaccinationFeedback(
    tenantId: number,
    facilityId: number,
    vaccinationId: number,
  ): Observable<Feedback[]> {
    return this.http.get<Feedback[]>(
      this.facilityUrl(tenantId, facilityId, `/vaccinations/${vaccinationId}/feedbacks`),
      JSON_HEADERS,
    );
  }

  readAcks(
    tenantId: number,
    facilityId: number,
  ): Observable<AcknowledgementObject<Feedback>[]> {
    return this.http.get<AcknowledgementObject<Feedback>[]>(
      this.facilityUrl(tenantId, facilityId, '/acks'),
      JSON_HEADERS,
    );
  }

  cleanPatientAcks(tenantId: number, facilityId: number, patientId: number): Observable<string> {
    return this.http.delete(this.patientUrl(tenantId, facilityId, patientId, '/acks'), TEXT_OPTIONS);
  }

  /**
   * Extracts feedbacks from a raw acknowledgement. The url is scoped as deeply
   * as the provided ids allow (facility, then patient, then vaccination).
   */
  convertAck(
    ack: string,
    context: {
      tenantId?: number;
      facilityId?: number;
      patientId?: number;
      vaccinationId?: number;
      registryId?: number;
    } = {},
  ): Observable<AcknowledgementObject<Feedback>> {
    const {tenantId, facilityId, patientId, vaccinationId, registryId} = context;
    let path = '';
    if (tenantId && tenantId > 0 && facilityId && facilityId > 0) {
      path = `/tenants/${tenantId}/facilities/${facilityId}`;
      if (patientId && patientId > 0) {
        path += `/patients/${patientId}`;
        if (vaccinationId && vaccinationId > 0) {
          path += `/vaccinations/${vaccinationId}`;
        }
      }
    }
    let params = new HttpParams();
    if (registryId && registryId > 0) {
      params = params.set('registryId', registryId);
    }
    return this.http.post<AcknowledgementObject<Feedback>>(
      this.url(`${path}/feedbacks/$extract-ack`),
      ack,
      {...JSON_HEADERS, params},
    );
  }
}
