import {HttpClient, HttpParams} from '@angular/common/http';
import {inject} from '@angular/core';
import {Observable} from 'rxjs';
import {AcknowledgementObject, Feedback} from '../../models';
import {ApiBase, JSON_HEADERS, TEXT_OPTIONS} from '../api.base';
import {IMessagingService, MessagingContext} from './messaging.service';

/**
 * Shared implementation for the two messaging flavors. HL7v2 posts to
 * `.../vxu`, the FHIR-flavored one to `.../vxu/fhir`.
 */
export abstract class MessagingBase extends ApiBase implements IMessagingService {
  protected readonly http = inject(HttpClient);

  /** '' for raw HL7v2, '/fhir' for the FHIR-converted variant. */
  protected abstract readonly suffix: string;

  private ctxUrl({tenantId, facilityId, patientId}: MessagingContext, path: string): string {
    return this.patientUrl(tenantId, facilityId, patientId, `${path}${this.suffix}`);
  }

  getVXU(context: MessagingContext, vaccinationId: number): Observable<string> {
    return this.http.get(this.ctxUrl(context, `/vaccinations/${vaccinationId}/vxu`), TEXT_OPTIONS);
  }

  getVXUAll(context: MessagingContext): Observable<string> {
    return this.http.get(this.ctxUrl(context, '/vxu'), TEXT_OPTIONS);
  }

  getQBP(context: MessagingContext): Observable<string> {
    return this.http.get(this.ctxUrl(context, '/qbp'), TEXT_OPTIONS);
  }

  postVXU(
    context: MessagingContext,
    vaccinationId: number | undefined,
    vxu: string,
    registryId?: number,
  ): Observable<AcknowledgementObject<Feedback>> {
    const path = vaccinationId && vaccinationId > -1 ? `/vaccinations/${vaccinationId}/vxu` : '/vxu';
    return this.http.post<AcknowledgementObject<Feedback>>(this.ctxUrl(context, path), vxu, {
      ...JSON_HEADERS,
      params: this.registryParams(registryId),
    });
  }

  postQBP(
    context: MessagingContext,
    qbp: string,
    registryId?: number,
  ): Observable<AcknowledgementObject<Feedback>> {
    return this.http.post<AcknowledgementObject<Feedback>>(this.ctxUrl(context, '/qbp'), qbp, {
      ...JSON_HEADERS,
      params: this.registryParams(registryId),
    });
  }

  private registryParams(registryId?: number): HttpParams {
    let params = new HttpParams();
    if (registryId && registryId > 0) {
      params = params.set('registryId', registryId);
    }
    return params;
  }
}
