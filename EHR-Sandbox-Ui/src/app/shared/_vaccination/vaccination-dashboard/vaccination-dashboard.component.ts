import { Component, Inject, Input, OnInit, Optional } from '@angular/core';
import { EhrPatient, Feedback, VaccinationEvent } from 'src/app/core/_model/rest';
import { CodeMapsService } from 'src/app/core/_services/code-maps.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FeedbackService } from 'src/app/core/_services/feedback.service';

@Component({
  selector: 'app-vaccination-dashboard',
  templateUrl: './vaccination-dashboard.component.html',
  styleUrls: ['./vaccination-dashboard.component.css']
})
export class VaccinationDashboardComponent implements OnInit {
  public feedbacksLoading!: boolean
  public feedbacks: Feedback[] = []

  @Input()
  private _vaccination!: VaccinationEvent;
  public get vaccination(): VaccinationEvent {
    return this._vaccination;
  }
  public set vaccination(value: VaccinationEvent) {
    this.feedbacksLoading = true
    this.feedbacks = []
    this._vaccination = value;
    this.feedbackService.readVaccinationFeedback(value).subscribe((res) => {
      this.feedbacksLoading = false
      this.feedbacks = res
    })
  }

  constructor(public codeMapsService: CodeMapsService,
    private vaccinationService: VaccinationService,
    private feedbackService: FeedbackService,
    private patientService: PatientService,
    @Optional() public _dialogRef: MatDialogRef<VaccinationDashboardComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { vaccination: number | VaccinationEvent }) {
    if (data?.vaccination) {
      this.patientService.getRefresh().subscribe((res) => {
        if ((typeof data.vaccination === "number" || typeof data.vaccination === "string")) {
          this.vaccinationService.quickReadVaccinationFromFacility(+data.vaccination).subscribe((res) => {
            this.vaccination = res
          })
        } else if (data.vaccination.id) {
          this.vaccinationService.quickReadVaccinationFromFacility(data.vaccination.id).subscribe((res) => {
            this.vaccination = res
          })
        }
      })
    }
    this._dialogRef?.afterClosed().subscribe(() => {
      this.patientService.doRefresh()
    })
  }

  ngOnInit(): void {
  }

}
