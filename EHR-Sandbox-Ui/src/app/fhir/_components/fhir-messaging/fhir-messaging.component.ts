import { AfterViewChecked, AfterViewInit, Component, Inject, Input, OnInit, Optional, ViewChild, ViewEncapsulation } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatTabGroup } from '@angular/material/tabs';
import { Feedback } from 'src/app/core/_model/rest';
import { FeedbackService } from 'src/app/core/_services/feedback.service';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';
import { FhirService } from 'src/app/fhir/_services/fhir.service';

@Component({
  selector: 'app-fhir-messaging',
  templateUrl: './fhir-messaging.component.html',
  styleUrls: ['./fhir-messaging.component.css'],
  encapsulation: ViewEncapsulation.None,
})
export class FhirMessagingComponent implements AfterViewInit, OnInit, AfterViewChecked {
  @ViewChild('tabs', {static: false}) tabGroup!: MatTabGroup;

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
    private snackBarService: SnackBarService,
    private feedbackService: FeedbackService,
    private immRegistriesService: ImmunizationRegistryService,
    @Optional() public _dialogRef: MatDialogRef<FhirMessagingComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: {patientId: number, vaccinationId?: number}) {
      if (data) {
        this.patientId = data.patientId
        if (data.vaccinationId) {
          this.vaccinationId = data.vaccinationId
        }
      }
     }

  public patientOperation:  "UpdateOrCreate" | "Create" | "Update" = "UpdateOrCreate";
  public immunizationOperation:  "UpdateOrCreate" | "Create" | "Update" = "UpdateOrCreate";

  ngOnInit(): void {
  }
  ngAfterViewChecked(): void {
    // this.tabGroup.focusTab(1)
    // this.tabGroup.selectedIndex = 1;
    // this.tabGroup.animationDone.next();
  }
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
    // this.tabGroup.focusTab(1)
    // this.tabGroup.selectedIndex = 1;
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
        // const feedback: Feedback = {iis: this.immRegistriesService.getImmRegistry().name, code: err.status, content: this.patientAnswer, severity: "Error", timestamp: Date.now()}
        // this.feedbackService.postPatientFeedback(this.patientId, feedback).subscribe((res) => {
        //   this.patientService.doRefresh()
        //   this.feedbackService.doRefresh()
        // })
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
      this.snackBarService.fatalFhirMessage("", this.patientId)
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
