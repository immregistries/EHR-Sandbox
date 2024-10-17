import { Component } from '@angular/core';
import { PatientService } from 'src/app/core/_services/patient.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { Observable, merge } from 'rxjs';
import { EhrPatient, Feedback, VaccinationEvent } from '../../_model/rest';
import { VaccinationService } from '../../_services/vaccination.service';
import { FeedbackService } from '../../_services/feedback.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent {

  constructor(public tenantService: TenantService,
    public facilityService: FacilityService,
    public patientService: PatientService,
    public vaccinationService: VaccinationService,
    public feedbackService: FeedbackService,
  ) { }

  rowHeight(): string {
    return (window.innerHeight / 2 - 35) + 'px'
  }

  patientLoading: boolean = false

  patientListRefreshObservable(): Observable<any> {
    return merge(
      this.facilityService.getCurrentObservable(),
      this.patientService.getRefresh(),
      this.facilityService.getRefresh(),
    )
  }

  patientListObservable(): Observable<EhrPatient[]> {
    return this.patientService.quickReadPatients()
  }

  feedbackListObservable(): Observable<Feedback[]> {
    return this.feedbackService.readCurrentFacilityFeedback()
  }


  vaccinationListRefreshObservable(): Observable<any> {
    return merge(this.patientService.getRefresh(), this.patientService.getCurrentObservable(), this.vaccinationService.getRefresh())
  }

  vaccinationListObservable(): Observable<VaccinationEvent[]> {
    return this.vaccinationService.quickReadVaccinations()
  }
}
