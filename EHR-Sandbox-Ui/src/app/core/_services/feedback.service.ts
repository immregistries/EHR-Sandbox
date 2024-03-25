import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

import { Feedback } from '../_model/rest';
import { BehaviorSubject, Observable, of, share, switchMap } from 'rxjs';
import { SettingsService } from './settings.service';
import { FacilityService } from './facility.service';
import { TenantService } from './tenant.service';
import { RefreshService } from './refresh.service';

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
    private tenantService: TenantService ) {
      super()
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
      if (value) {
        return this.http.get<Feedback[]>(
          `${this.settings.getApiUrl()}/tenants/${this.tenantService.getCurrentId()}/facilities/${this.facilityService.getCurrentId()}/feedbacks`,
          httpOptions)
      } else {
        return of([])
      }
    }))
  }

}
