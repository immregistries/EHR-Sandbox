import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, share, of } from 'rxjs';
import { Clinician } from '../_model/rest';
import { SettingsService } from './settings.service';
import { TenantService } from './tenant.service';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class ClinicianService {

  constructor(private http: HttpClient,
    private tenantService: TenantService,
    private settings: SettingsService,
  ) { }


  quickReadClinicians(): Observable<Clinician[]> {
    let tenantId = this.tenantService.getTenantId()
    if (tenantId > 0){
      return this.http.get<Clinician[]>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/clinicians`,
        httpOptions).pipe(share());
    }
    return of([])
  }

  readClinicians(tenantId: number): Observable<Clinician[]> {
    if (tenantId > 0){
      return this.http.get<Clinician[]>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/clinicians`,
        httpOptions).pipe(share());
    }
    return of([])
  }

  readClinician(tenantId: number, clinicianId: number): Observable<Clinician> {
    return this.http.get<Clinician>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/clinicians/${clinicianId}`,
      httpOptions);
  }

  postClinician(tenantId: number, clinician: Clinician): Observable<HttpResponse<Clinician>> {
    return this.http.post<Clinician>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/clinicians`,
      clinician, {observe: 'response'})
  }

  putClinician(tenantId: number, clinician: Clinician): Observable<HttpResponse<Clinician>> {
    return this.http.post<Clinician>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/clinicians/${clinician.id}`,
      clinician, {observe: 'response'})
  }
}
