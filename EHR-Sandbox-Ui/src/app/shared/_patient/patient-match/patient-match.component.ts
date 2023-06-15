import { Component, Input } from '@angular/core';
import { FeedbackService } from 'src/app/core/_services/feedback.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { FhirClientService } from 'src/app/fhir/_services/fhir-client.service';

@Component({
  selector: 'app-patient-match',
  templateUrl: './patient-match.component.html',
  styleUrls: ['./patient-match.component.css']
})
export class PatientMatchComponent {

  constructor(private fhirClientService: FhirClientService,
    private patientService: PatientService,
    private snackBarService: SnackBarService,
    private feedbackService: FeedbackService) {

  }

  @Input()
  patientId!: number;

  answer: string = "";
  error: boolean = false;
  requestLoading: Boolean = false
  resource!: string;

  ngOnInit() {
    // this.fhirClientService.quickGetPatientResource
  }


  match() {
    this.answer = ""
    this.requestLoading = true
    this.fhirClientService.matchResource("Patient",this.resource, this.patientId, -1)
    .subscribe({
      next: (res) => {
        this.requestLoading = false
        this.feedbackService.doRefresh()
        this.error = false
        this.answer = JSON.stringify(res)
      },
      error: (err) => {
        this.requestLoading = false
        this.feedbackService.doRefresh()
        this.error = true
        this.answer = err.error
        console.error(err)
        this.answer = JSON.stringify(err.error)
      }
    })
  }
}
