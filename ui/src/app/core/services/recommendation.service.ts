import {HttpClient} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {ImmunizationRecommendation} from 'fhir/r5';
import {Observable} from 'rxjs';
import {ApiBase, JSON_HEADERS} from './api.base';

@Injectable({providedIn: 'root'})
export class RecommendationService extends ApiBase {
  private readonly http = inject(HttpClient);

  readRecommendations(
    tenantId: number,
    facilityId: number,
    patientId: number,
  ): Observable<ImmunizationRecommendation[]> {
    return this.http.get<ImmunizationRecommendation[]>(
      this.patientUrl(tenantId, facilityId, patientId, '/recommendations'),
      JSON_HEADERS,
    );
  }
}
