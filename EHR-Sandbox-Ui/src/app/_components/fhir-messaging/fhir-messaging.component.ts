import { AfterViewInit, Component, Inject, Input, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FhirService } from 'src/app/_services/fhir.service';
import { VaccinationService } from 'src/app/_services/vaccination.service';

@Component({
  selector: 'app-fhir-messaging',
  templateUrl: './fhir-messaging.component.html',
  styleUrls: ['./fhir-messaging.component.css']
})
export class FhirMessagingComponent implements AfterViewInit {

  @Input() vaccinationId!: number;
  @Input() patientId!: number;

  public patientResource: string = "";
  public patientAnswer: string = "";
  public patientError: string = "";

  public vaccinationResource: string = "";
  public vaccinationAnswer: string = "";
  public vaccinationError: string = "";

  public style: string = 'width: 50%'

  constructor(private fhirService: FhirService,
    private _snackBar: MatSnackBar) {

     }

  ngAfterViewInit(): void {
    this.fhirService.quickGetPatientResource(this.patientId).subscribe((resource) => {
      this.patientResource = resource
    })
    if (this.vaccinationId){
      this.fhirService.quickGetImmunizationResource(this.patientId,this.vaccinationId).subscribe((resource) => {
        this.vaccinationResource = resource
      })
    }
  }

  sendPatient() {
    this.fhirService.quickPostPatient(this.patientId, this.patientResource).subscribe(
      (res) => {
        this.patientAnswer = res
        this.patientError = ""
      },
      (err) => {
        this.patientAnswer = ""
        if (err.error.text) {
          this.patientError = err.error.text
        } else {
          this.patientError = err.error
        }
        console.error(err)
      }
    )
  }

sendVaccination() {
    this.fhirService.quickPostImmunization(this.patientId,this.vaccinationId, this.vaccinationResource).subscribe(
      (res) => {
        this.vaccinationAnswer = res
        this.vaccinationError = ""
      },
      (err) => {
        this.vaccinationAnswer = ""
        if (err.error.text) {
          this.vaccinationError = err.error.text
        } else {
          this.vaccinationError = err.error
        }
        console.error(err)
      }
    )
  }


}
