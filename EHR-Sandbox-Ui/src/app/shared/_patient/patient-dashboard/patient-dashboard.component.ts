import { Component, Inject, Input, Optional } from '@angular/core';
import { EhrPatient, Feedback, VaccinationEvent } from 'src/app/core/_model/rest';
import { PatientService } from 'src/app/core/_services/patient.service';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Observable, merge, of } from 'rxjs';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';
import { FeedbackService } from 'src/app/core/_services/feedback.service';

@Component({
  selector: 'app-patient-dashboard',
  templateUrl: './patient-dashboard.component.html',
  styleUrls: ['./patient-dashboard.component.css']
})
export class PatientDashboardComponent {
  public feedbacksLoading!: boolean
  public feedbacks: Feedback[] = []

  @Input()
  private _patient!: EhrPatient;
  public get patient(): EhrPatient {
    return this._patient;
  }
  public set patient(value: EhrPatient) {
    this.feedbacksLoading = true
    this.feedbacks = []
    this._patient = value;
    this.feedbackService.readPatientFeedback(value).subscribe((res) => {
      this.feedbacksLoading = false
      this.feedbacks = res
    })
  }

  constructor(private patientService: PatientService,
    public vaccinationService: VaccinationService,
    private feedbackService: FeedbackService,
    @Optional() public _dialogRef: MatDialogRef<PatientDashboardComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { patient?: EhrPatient | number }) {
    if (data?.patient) {
      if (typeof data.patient === "number" || typeof data.patient === "string") {
        this.patientService.quickReadPatient(+data.patient).subscribe((res) => {
          this.patient = res
        });
      } else if (data.patient.id) {
        this.patientService.quickReadPatient(data.patient.id).subscribe((res) => {
          this.patient = res
        });
      }
    }
    this._dialogRef?.afterClosed().subscribe(() => {
      this.patientService.doRefresh()
    })
  }

}
