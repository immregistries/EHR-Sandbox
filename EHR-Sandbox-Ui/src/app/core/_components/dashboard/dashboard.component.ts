import { Component } from '@angular/core';
import { PatientService } from 'src/app/core/_services/patient.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { Observable, merge, of } from 'rxjs';
import { EhrPatient, Feedback, VaccinationEvent } from '../../_model/rest';
import { VaccinationService } from '../../_services/vaccination.service';
import { FeedbackService } from '../../_services/feedback.service';
import { RecommendationService } from '../../_services/recommendation.service';

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
    public recommendationService: RecommendationService,
  ) { }

  rowHeight(): string {
    return (window.innerHeight / 2 - 35) + 'px'
  }

}
