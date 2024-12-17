import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

import { Feedback } from '../_model/rest';
import { BehaviorSubject, Observable, of, share, switchMap } from 'rxjs';
import { SettingsService } from './settings.service';
import { FacilityService } from './facility.service';
import { TenantService } from './tenant.service';
import { RefreshService } from './_abstract/refresh.service';
import { SnackBarService } from './snack-bar.service';
import { AcknowledgementObject } from '../_model/form-structure';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class FeedbackService extends RefreshService {

  if_valid_parent_ids: Observable<boolean> = new Observable((subscriber) => subscriber.next(this.tenantService.getCurrentId() > 0 && this.facilityService.getCurrentId() > 0))

  constructor(private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService,
    snackBarService: SnackBarService
  ) {
    super(snackBarService)
  }

  postPatientFeedback(patientId: number, feedback: Feedback): Observable<Feedback> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()

    return this.http.post<Feedback>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/feedbacks`,
      feedback,
      httpOptions);
  }

  postVaccinationFeedback(patientId: number, vaccinationId: number, feedback: Feedback): Observable<Feedback> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()

    return this.http.post<Feedback>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/${vaccinationId}/feedbacks`,
      feedback,
      httpOptions);
  }

  readFacilityFeedback(facilityId: number): Observable<Feedback[]> {
    const tenantId: number = this.tenantService.getCurrentId()
    if (facilityId < 0) {
      return of()
    }
    return this.http.get<Feedback[]>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/feedbacks`,
      httpOptions).pipe(share());
  }

  readCurrentFacilityFeedback(): Observable<Feedback[]> {
    return this.if_valid_parent_ids.pipe(switchMap((value) => {
      if (value === true) {
        return this.http.get<Feedback[]>(
          `${this.settings.getApiUrl()}/tenants/${this.tenantService.getCurrentId()}/facilities/${this.facilityService.getCurrentId()}/feedbacks`,
          httpOptions)
      } else {
        return of([])
      }
    }))
  }

  convertAck(ack: String, registryId?: number, patientId?: number, vaccinationId?: number): Observable<AcknowledgementObject<Feedback>> {
    return this.if_valid_parent_ids.pipe(switchMap((value) => {
      let baseUri = `${this.settings.getApiUrl()}`;
      if (value === true) {
        baseUri += `/tenants/${this.tenantService.getCurrentId()}/facilities/${this.facilityService.getCurrentId()}`
        if (patientId && patientId > 0) {
          baseUri += `/patients/${patientId}`
          if (vaccinationId && vaccinationId > 0) {
            baseUri += `/vaccinations/${vaccinationId}`
          }
        }
      } else {
        // return of({
        //   errors: [],
        //   warnings: [],
        //   notices: [],
        //   infos: []
        // })
      }
      return this.http.post<AcknowledgementObject<Feedback>>(
        `${baseUri}/feedbacks/$extract-ack`,
        ack,
        {
          ...httpOptions,
          params: (registryId && registryId > 0) ? { registryId: registryId } : {}
        })
    }))
  }

}
