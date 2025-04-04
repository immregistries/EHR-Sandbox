import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ImmunizationRecommendation } from 'fhir/r5';
import { combineLatest, map, Observable, of, share, shareReplay, startWith, switchMap, tap } from 'rxjs';
import { FacilityService } from './facility.service';
import { SettingsService } from './settings.service';
import { TenantService } from './tenant.service';
import { RefreshService } from './_abstract/refresh.service';
import { SnackBarService } from './snack-bar.service';
import { PatientService } from './patient.service';


const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
@Injectable({
  providedIn: 'root'
})
export class RecommendationService extends RefreshService {

  private readonly quickReadObservable: Observable<ImmunizationRecommendation[]> = combineLatest([
    this.getRefresh(),
    this.tenantService.getCurrentObservable(),
    this.facilityService.getCurrentObservable(),
    this.patientService.getCurrentObservable(),
  ]).pipe(tap(() => this.loading = true))
    .pipe(switchMap(([_, tenant, facility, patient]) => tenant?.id > 0 && facility?.id && facility.id > 0 && patient.id && patient.id > 0 ? this.readRecommendations(tenant.id, facility.id, patient.id) : of([])))
    .pipe(shareReplay({ bufferSize: 1, refCount: true }))
    .pipe(tap(() => this.loading = false))



  constructor(private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private patientService: PatientService,
    private tenantService: TenantService,
    snackBarService: SnackBarService
  ) {
    super(snackBarService)
  }

  public quickReadRecommendations(): Observable<ImmunizationRecommendation[]> {
    return this.quickReadObservable
  }

  public readRecommendations(tenantId: number, facilityId: number, patientId: number): Observable<ImmunizationRecommendation[]> {
    if (tenantId > 0 && facilityId > 0 && patientId > 0) {
      return this.http.get<string[]>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/recommendations`,
        httpOptions)
        .pipe(share(), map((array: string[]) => { return array.map((json) => { return (JSON.parse(json) as ImmunizationRecommendation) }) }));
    } else {
      return of([])
    }
  }

  // readRecommendations(patientId: number): Observable<ImmunizationRecommendation[]> {
  //   const tenantId: number = this.tenantService.getCurrentId()
  //   const facilityId: number = this.facilityService.getCurrentId()
  //   if (tenantId > 0 && facilityId > 0 && patientId > 0) {
  //     return this.http.get<string[]>(
  //       `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/recommendations`,
  //       httpOptions)
  //       .pipe(share(), map((array: string[]) => { return array.map((json) => { return (JSON.parse(json) as ImmunizationRecommendation) }) }));
  //   } else {
  //     return of([])
  //   }
  // }



}
