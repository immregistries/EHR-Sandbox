import {HttpClient, HttpParams} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {ReceivedHistoryDTO} from '../../models';
import {ApiBase, JSON_HEADERS, TEXT_OPTIONS} from '../api.base';

/** Reads and imports SMART Health Links. */
@Injectable({providedIn: 'root'})
export class SmartHealthLinkService extends ApiBase {
  private readonly http = inject(HttpClient);

  private linkParams(url: string, password?: string, jwk?: string): HttpParams {
    let params = new HttpParams().set('url', url);
    if (password) {
      params = params.set('password', password);
    }
    if (jwk) {
      params = params.set('jwk', jwk);
    }
    return params;
  }

  shlinkRead(
    tenantId: number,
    url: string,
    password?: string,
    jwk?: string,
  ): Observable<string> {
    return this.http.get(this.tenantUrl(tenantId, '/$read-sh-link'), {
      ...TEXT_OPTIONS,
      params: this.linkParams(url, password, jwk),
    });
  }

  importShlink(
    tenantId: number,
    url: string,
    password?: string,
    jwk?: string,
  ): Observable<ReceivedHistoryDTO> {
    return this.http.get<ReceivedHistoryDTO>(this.tenantUrl(tenantId, '/$import-sh-link'), {
      ...JSON_HEADERS,
      params: this.linkParams(url, password, jwk),
    });
  }

  importShlinkForPatient(
    tenantId: number,
    facilityId: number,
    patientId: number,
    url: string,
    password?: string,
    jwk?: string,
  ): Observable<ReceivedHistoryDTO> {
    return this.http.get<ReceivedHistoryDTO>(
      this.patientUrl(tenantId, facilityId, patientId, '/$import-sh-link'),
      {...JSON_HEADERS, params: this.linkParams(url, password, jwk)},
    );
  }
}
