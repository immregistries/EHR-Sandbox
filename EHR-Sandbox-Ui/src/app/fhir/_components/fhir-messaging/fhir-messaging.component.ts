import { AfterViewInit, Component, Inject, Input, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Feedback } from 'src/app/core/_model/rest';
import { FeedbackService } from 'src/app/core/_services/feedback.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';
import { FhirService } from 'src/app/fhir/_services/fhir.service';

@Component({
  selector: 'app-fhir-messaging',
  templateUrl: './fhir-messaging.component.html',
  styleUrls: ['./fhir-messaging.component.css']
})
export class FhirMessagingComponent implements AfterViewInit {
  patientLoading: Boolean = false
  vaccinationLoading: Boolean = false

  @Input() vaccinationId!: number;
  @Input() patientId: number = -1;

  public patientResource: string = "";
  public patientAnswer: string = "";
  public patientError: string = "";

  public vaccinationResource: string = "";
  public vaccinationAnswer: string = "";
  public vaccinationError: string = "";

  public style: string = 'width: 50%'

  constructor(private fhirService: FhirService,
    private patientService: PatientService,
    private vaccinationService: VaccinationService,
    private _snackBar: MatSnackBar,
    private feedbackService: FeedbackService) {

     }

  public patientOperation:  "UpdateOrCreate" | "Create" | "Update" = "UpdateOrCreate";
  public immunizationOperation:  "UpdateOrCreate" | "Create" | "Update" = "UpdateOrCreate";

  ngAfterViewInit(): void {
    this.patientLoading = true
    this.fhirService.quickGetPatientResource(this.patientId).subscribe((resource) => {
      this.patientResource = resource
      this.patientLoading = false
    })
    if (this.vaccinationId){
      this.vaccinationLoading = true
      this.fhirService.quickGetImmunizationResource(this.patientId,this.vaccinationId).subscribe((resource) => {
        this.vaccinationResource = resource
        this.vaccinationLoading = false
      })
    }
  }

  sendPatient() {
    this.fhirService.quickPostPatient(this.patientId, this.patientResource, this.patientOperation).subscribe({
      next: (res) => {
        this.patientAnswer = res
        this.patientError = ""
        this.patientService.doRefresh()
        // const feedback: Feedback = {iis: "fhirTest", content: res, severity: "info"}
        // this.feedbackService.postPatientFeedback(this.patientId, feedback).subscribe((res) => {
        //   console.log(res)
        // })
      },
      error: (err) => {
        this.patientAnswer = ""
        if (err.error.text) {
          this.patientError = err.error.text
        } else {
          this.patientError = err.error.error
        }
        const feedback: Feedback = {iis: "fhirTest", content: this.patientError, severity: "error"}
        this.feedbackService.postPatientFeedback(this.patientId, feedback).subscribe((res) => {
          console.log(res)
          this.patientService.doRefresh()
          this.feedbackService.doRefresh()
        })
        console.error(err)
      }
    })
  }

sendVaccination() {
    this.fhirService.quickPostImmunization(this.patientId,this.vaccinationId, this.vaccinationResource, this.immunizationOperation).subscribe(
      (res) => {
        this.vaccinationAnswer = res
        this.vaccinationError = ""
        this.feedbackService.doRefresh()
        // const feedback: Feedback = {iis: "fhirTest", content: res, severity: "info"}
        // this.feedbackService.postVaccinationFeedback(this.patientId, this.vaccinationId, feedback).subscribe((res) => {
        //   console.log(res)
        //   this.patientService.doRefresh()
        //   this.vaccinationService.doRefresh()
        // })
      },
      (err) => {
        this.vaccinationAnswer = ""
        if (err.error.text) {
          this.vaccinationError = err.error.text
        } else {
          this.vaccinationError = err.error.error
        }
        this.feedbackService.doRefresh()
        const feedback: Feedback = {iis: "fhirTest", content: this.vaccinationError, severity: "error", date: new Date()}
        this.feedbackService.postVaccinationFeedback(this.patientId, this.vaccinationId, feedback).subscribe((res) => {
          console.log(res)
          this.patientService.doRefresh()
          this.vaccinationService.doRefresh()
          this.feedbackService.doRefresh()
        })
        console.error(err)
      }
    )
  }


}
