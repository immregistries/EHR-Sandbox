import { AfterViewInit, Component, Inject, Input, OnInit, ViewEncapsulation } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Feedback } from 'src/app/core/_model/rest';
import { FeedbackService } from 'src/app/core/_services/feedback.service';
import { ImmRegistriesService } from 'src/app/core/_services/imm-registries.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';
import { FhirService } from 'src/app/fhir/_services/fhir.service';

@Component({
  selector: 'app-fhir-messaging',
  templateUrl: './fhir-messaging.component.html',
  styleUrls: ['./fhir-messaging.component.css'],
  encapsulation: ViewEncapsulation.None,
})
export class FhirMessagingComponent implements AfterViewInit {
  patientLoading: Boolean = false
  vaccinationLoading: Boolean = false
  patientRequestLoading: Boolean = false
  vaccinationRequestLoading: Boolean = false

  @Input() vaccinationId!: number;
  @Input() patientId: number = -1;

  public patientResource: string = "";
  public patientAnswer: string = "";
  public patientError: boolean = false;

  public vaccinationResource: string = "";
  public vaccinationAnswer: string = "";
  public vaccinationError: boolean = false;

  public style: string = 'width: 50%'

  public autofillId: boolean = true;
  public patientFhirId = "";

  constructor(private fhirService: FhirService,
    private patientService: PatientService,
    private vaccinationService: VaccinationService,
    private _snackBar: MatSnackBar,
    private feedbackService: FeedbackService,
    private immRegistriesService: ImmRegistriesService) {
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
    this.patientAnswer = ""
    this.patientRequestLoading = true
    this.fhirService.quickPostPatient(this.patientId, this.patientResource, this.patientOperation).subscribe({
      next: (res) => {
        this.patientRequestLoading = false
        this.patientAnswer = res
        this.patientError = false
        this.patientService.doRefresh()
        if (this.autofillId) {
          this.patientFhirId = res
        }
      },
      error: (err) => {
        this.patientRequestLoading = false
        this.patientError = true
        if (err.error.error) {
          this.patientAnswer = err.error.error
        }else if (err.error.text) {
          this.patientAnswer = err.error.text
        } else {
          this.patientAnswer = "Error"
        }
        const feedback: Feedback = {iis: this.immRegistriesService.getImmRegistry().name, code: err.status, content: this.patientAnswer, severity: "Error", timestamp: Date.now()}
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
  this.vaccinationAnswer = ""
  this.vaccinationRequestLoading = true
  this.fhirService.quickPostImmunization(this.patientId,this.vaccinationId, this.vaccinationResource, this.immunizationOperation, this.patientFhirId).subscribe({
    next: (res) => {
      this.vaccinationRequestLoading = false
      this.vaccinationAnswer = res
      this.vaccinationError = false
      this.feedbackService.doRefresh()
    },
    error: (err) => {
      this.vaccinationRequestLoading = false
      this.vaccinationError = true
      if (err.error.error) {
        this.vaccinationAnswer = err.error.error
      } else if (err.error.text) {
        this.vaccinationAnswer = err.error.text
      } else {
        this.vaccinationAnswer = "Error"
      }
      this.feedbackService.doRefresh()
      // const feedback: Feedback = {iis: "fhirTest", content: this.vaccinationAnswer, severity: "error", date: new Date()}
      // this.feedbackService.postVaccinationFeedback(this.patientId, this.vaccinationId, feedback).subscribe((res) => {
      //   console.log(res)
      //   this.patientService.doRefresh()
      //   this.vaccinationService.doRefresh()
      //   this.feedbackService.doRefresh()
      // })
      console.error(err)
    }})
  }


}
