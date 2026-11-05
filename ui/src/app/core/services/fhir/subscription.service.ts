import {HttpClient} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {EhrFhirOutcome, EhrSubscription} from '../../models';
import {ApiBase, JSON_HEADERS, TEXT_OPTIONS} from '../api.base';

/** FHIR Subscription management for a facility. */
@Injectable({providedIn: 'root'})
export class SubscriptionService extends ApiBase {
  private readonly http = inject(HttpClient);

  readSubscription(tenantId: number, facilityId: number): Observable<EhrSubscription> {
    return this.http.get<EhrSubscription>(
      this.facilityUrl(tenantId, facilityId, '/subscription'),
      JSON_HEADERS,
    );
  }

  readSample(tenantId: number, facilityId: number): Observable<string> {
    return this.http.get(
      this.facilityUrl(tenantId, facilityId, '/subscription/sample'),
      TEXT_OPTIONS,
    );
  }

  createSubscription(
    tenantId: number,
    facilityId: number,
    topic: string,
  ): Observable<boolean | null> {
    return this.http.get<boolean | null>(
      this.facilityUrl(tenantId, facilityId, '/fhir-client/subscription'),
      {...JSON_HEADERS, params: {topic}},
    );
  }

  postSubscription(
    tenantId: number,
    facilityId: number,
    subscription: string,
  ): Observable<EhrFhirOutcome> {
    return this.http.post<EhrFhirOutcome>(
      this.facilityUrl(tenantId, facilityId, '/fhir-client/subscription'),
      subscription,
      JSON_HEADERS,
    );
  }

  putSubscription(
    tenantId: number,
    facilityId: number,
    subscription: string,
  ): Observable<EhrFhirOutcome> {
    return this.http.put<EhrFhirOutcome>(
      this.facilityUrl(tenantId, facilityId, '/fhir-client/subscription'),
      subscription,
      JSON_HEADERS,
    );
  }
}
