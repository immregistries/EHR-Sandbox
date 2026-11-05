import {HttpClient, HttpParams, HttpResponse} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {EhrGroup} from '../models';
import {ApiBase, JSON_HEADERS, TEXT_OPTIONS} from './api.base';

@Injectable({providedIn: 'root'})
export class GroupService extends ApiBase {
  private readonly http = inject(HttpClient);

  private groupsUrl(tenantId: number, facilityId: number, path = ''): string {
    return this.facilityUrl(tenantId, facilityId, `/groups${path}`);
  }

  readGroups(tenantId: number, facilityId: number): Observable<EhrGroup[]> {
    return this.http.get<EhrGroup[]>(this.groupsUrl(tenantId, facilityId), JSON_HEADERS);
  }

  getGroup(tenantId: number, facilityId: number, groupId: number): Observable<EhrGroup> {
    return this.http.get<EhrGroup>(
      this.groupsUrl(tenantId, facilityId, `/${groupId}`),
      JSON_HEADERS,
    );
  }

  getGroupFromName(tenantId: number, facilityId: number, name: string): Observable<EhrGroup> {
    return this.http.get<EhrGroup>(this.groupsUrl(tenantId, facilityId), {
      ...JSON_HEADERS,
      params: new HttpParams().set('name', name),
    });
  }

  getRandom(tenantId: number, facilityId: number): Observable<EhrGroup> {
    return this.http.get<EhrGroup>(this.groupsUrl(tenantId, facilityId, '/$random'), JSON_HEADERS);
  }

  postGroup(
    tenantId: number,
    facilityId: number,
    group: EhrGroup,
  ): Observable<HttpResponse<string>> {
    return this.http.post(this.groupsUrl(tenantId, facilityId), group, {
      ...TEXT_OPTIONS,
      observe: 'response',
    });
  }

  putGroup(tenantId: number, facilityId: number, group: EhrGroup): Observable<EhrGroup> {
    return this.http.put<EhrGroup>(this.groupsUrl(tenantId, facilityId), group, JSON_HEADERS);
  }

  refreshGroup(tenantId: number, facilityId: number, groupId: number): Observable<EhrGroup> {
    return this.http.get<EhrGroup>(
      this.groupsUrl(tenantId, facilityId, `/${groupId}/$refresh`),
      JSON_HEADERS,
    );
  }

  addMember(
    tenantId: number,
    facilityId: number,
    groupId: number,
    patientId: string | number,
  ): Observable<EhrGroup> {
    return this.http.post<EhrGroup>(
      this.groupsUrl(tenantId, facilityId, `/${groupId}/$add`),
      null,
      {...JSON_HEADERS, params: new HttpParams().set('patientId', patientId)},
    );
  }

  removeMember(
    tenantId: number,
    facilityId: number,
    groupId: number,
    patientId: number,
  ): Observable<EhrGroup> {
    return this.http.post<EhrGroup>(
      this.groupsUrl(tenantId, facilityId, `/${groupId}/$remove`),
      null,
      {...JSON_HEADERS, params: new HttpParams().set('patientId', patientId)},
    );
  }

  getGroupBulkImportStatus(
    tenantId: number,
    facilityId: number,
    groupId: number,
  ): Observable<object> {
    return this.http.get<object>(
      this.groupsUrl(tenantId, facilityId, `/${groupId}/$import-status`),
      JSON_HEADERS,
    );
  }

  groupBulkImportStatusForceRefresh(
    tenantId: number,
    facilityId: number,
    groupId: number,
  ): Observable<object> {
    return this.http.get<object>(
      this.groupsUrl(tenantId, facilityId, `/${groupId}/$import-status-refresh`),
      JSON_HEADERS,
    );
  }

  groupBulkImportKickoff(
    tenantId: number,
    facilityId: number,
    groupId: number,
  ): Observable<object> {
    return this.http.get<object>(
      this.groupsUrl(tenantId, facilityId, `/${groupId}/$import`),
      JSON_HEADERS,
    );
  }

  getGroupBulkViewResult(
    tenantId: number,
    facilityId: number,
    groupId: number,
    body: string,
  ): Observable<[]> {
    return this.http.post<[]>(
      this.groupsUrl(tenantId, facilityId, `/${groupId}/$import-view-result`),
      body,
      JSON_HEADERS,
    );
  }
}
