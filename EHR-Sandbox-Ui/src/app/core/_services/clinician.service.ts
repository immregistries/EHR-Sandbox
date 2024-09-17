import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, share, of, BehaviorSubject, switchMap } from 'rxjs';
import { Clinician } from '../_model/rest';
import { SettingsService } from './settings.service';
import { TenantService } from './tenant.service';
import { RefreshService } from './_abstract/refresh.service';
import { SnackBarService } from './snack-bar.service';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class ClinicianService extends RefreshService {

  if_valid_parent_ids: Observable<boolean> = this.observables_parent_ids_valid(undefined, this.tenantService);

  constructor(private http: HttpClient,
    private tenantService: TenantService,
    private settings: SettingsService,
    snackBarService: SnackBarService
  ) {
    super(snackBarService)
  }


  quickReadClinicians(): Observable<Clinician[]> {
    return this.if_valid_parent_ids.pipe(switchMap((value) => {
      if (value === true) {
        return this.http.get<Clinician[]>(
          `${this.settings.getApiUrl()}/tenants/${this.tenantService.getCurrentId()}/clinicians`,
          httpOptions).pipe(share());
      } else {
        return of([])
      }
    }))
  }

  readClinicians(tenantId: number): Observable<Clinician[]> {
    if (this.idsNotValid(tenantId)) {
      return of([])

    }
    return this.http.get<Clinician[]>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/clinicians`,
      httpOptions).pipe(share());
  }

  random(tenantId: number): Observable<Clinician> {
    return this.http.get<Clinician>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/clinicians/$random`,
      httpOptions);
  }

  readClinician(tenantId: number, clinicianId: number): Observable<Clinician> {
    return this.http.get<Clinician>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/clinicians/${clinicianId}`,
      httpOptions);
  }

  postClinician(tenantId: number, clinician: Clinician): Observable<Clinician> {
    return this.http.post<Clinician>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/clinicians`,
      clinician, httpOptions)
  }

  putClinician(tenantId: number, clinician: Clinician): Observable<Clinician> {
    clinician.tenant = undefined
    return this.http.put<Clinician>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/clinicians/${clinician.id}`,
      clinician, httpOptions)
  }
}
