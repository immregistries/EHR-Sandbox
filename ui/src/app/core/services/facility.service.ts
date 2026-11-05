import {HttpClient, HttpParams, HttpResponse} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Facility} from '../models';
import {ApiBase, JSON_HEADERS, TEXT_OPTIONS} from './api.base';

@Injectable({providedIn: 'root'})
export class FacilityService extends ApiBase {
  private readonly http = inject(HttpClient);

  readAllFacilities(): Observable<Facility[]> {
    return this.http.get<Facility[]>(this.url('/facilities'), JSON_HEADERS);
  }

  readFacilities(tenantId: number): Observable<Facility[]> {
    return this.http.get<Facility[]>(this.tenantUrl(tenantId, '/facilities'), JSON_HEADERS);
  }

  readFacility(tenantId: number, facilityId: number): Observable<Facility> {
    return this.http.get<Facility>(this.facilityUrl(tenantId, facilityId), JSON_HEADERS);
  }

  readFacilityChildren(tenantId: number, facilityId: number): Observable<Facility[]> {
    return this.http.get<Facility[]>(
      this.facilityUrl(tenantId, facilityId, '/$children'),
      JSON_HEADERS,
    );
  }

  getRandom(tenantId: number): Observable<Facility> {
    return this.http.get<Facility>(this.tenantUrl(tenantId, '/facilities/$random'), JSON_HEADERS);
  }

  postFacility(
    tenantId: number,
    facility: Facility,
    populate?: boolean,
  ): Observable<HttpResponse<Facility>> {
    return this.http.post<Facility>(this.tenantUrl(tenantId, '/facilities'), facility, {
      ...JSON_HEADERS,
      observe: 'response',
      params: new HttpParams().set('populate', populate ?? false),
    });
  }

  putFacility(tenantId: number, facility: Facility): Observable<HttpResponse<Facility>> {
    return this.http.put<Facility>(this.tenantUrl(tenantId, '/facilities'), facility, {
      ...JSON_HEADERS,
      observe: 'response',
    });
  }

  populate(tenantId: number, facilityId: number): Observable<string> {
    return this.http.get(this.facilityUrl(tenantId, facilityId, '/$populate'), TEXT_OPTIONS);
  }
}
