import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

import { Feedback } from '../_model/rest';
import { BehaviorSubject, Observable, of, share } from 'rxjs';
import { SettingsService } from './settings.service';
import { FacilityService } from './facility.service';
import { TenantService } from './tenant.service';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class FeedbackService {
  private refresh: BehaviorSubject<boolean>;

  public getRefresh(): Observable<boolean> {
    return this.refresh.asObservable();
  }

  public doRefresh(): void{
    this.refresh.next(!this.refresh.value)
  }


  constructor(private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService ) {
      this.refresh = new BehaviorSubject<boolean>(false)
    }

  postPatientFeedback(patientId: number, feedback: Feedback): Observable<Feedback> {
    const tenantId: number = this.tenantService.getTenantId()
    const facilityId: number = this.facilityService.getFacilityId()

    return this.http.post<Feedback>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/feedbacks`,
      feedback,
      httpOptions);
  }

  postVaccinationFeedback(patientId: number, vaccinationId: number, feedback: Feedback): Observable<Feedback> {
    const tenantId: number = this.tenantService.getTenantId()
    const facilityId: number = this.facilityService.getFacilityId()

    return this.http.post<Feedback>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/${vaccinationId}/feedbacks`,
      feedback,
      httpOptions);
  }

  readFacilityFeedback(facilityId: number): Observable<Feedback[]> {
    const tenantId: number = this.tenantService.getTenantId()
    if (facilityId < 0) {
      return of()
    }
    return this.http.get<Feedback[]>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/feedbacks`,
      httpOptions).pipe(share());
  }
}
