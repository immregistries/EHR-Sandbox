import {HttpClient} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Clinician} from '../models';
import {ApiBase, JSON_HEADERS} from './api.base';

@Injectable({providedIn: 'root'})
export class ClinicianService extends ApiBase {
  private readonly http = inject(HttpClient);

  readClinicians(tenantId: number): Observable<Clinician[]> {
    return this.http.get<Clinician[]>(this.tenantUrl(tenantId, '/clinicians'), JSON_HEADERS);
  }

  readClinician(tenantId: number, clinicianId: number): Observable<Clinician> {
    return this.http.get<Clinician>(
      this.tenantUrl(tenantId, `/clinicians/${clinicianId}`),
      JSON_HEADERS,
    );
  }

  random(tenantId: number): Observable<Clinician> {
    return this.http.get<Clinician>(this.tenantUrl(tenantId, '/clinicians/$random'), JSON_HEADERS);
  }

  postClinician(tenantId: number, clinician: Clinician): Observable<Clinician> {
    return this.http.post<Clinician>(
      this.tenantUrl(tenantId, '/clinicians'),
      clinician,
      JSON_HEADERS,
    );
  }

  putClinician(tenantId: number, clinician: Clinician): Observable<Clinician> {
    return this.http.put<Clinician>(
      this.tenantUrl(tenantId, `/clinicians/${clinician.id}`),
      clinician,
      JSON_HEADERS,
    );
  }
}
