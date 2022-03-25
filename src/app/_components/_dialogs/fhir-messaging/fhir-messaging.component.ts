import { Component, Inject, Input, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FhirService } from 'src/app/_services/fhir.service';
import { VaccinationService } from 'src/app/_services/vaccination.service';

@Component({
  selector: 'app-fhir-messaging',
  templateUrl: './fhir-messaging.component.html',
  styleUrls: ['./fhir-messaging.component.css']
})
export class FhirMessagingComponent implements OnInit {

  @Input() vaccinationId: number = -1;
  @Input() patientId: number = -1;

  public patientResource: string = "";
  public patientAnswer: string = "";
  public patientError: string = "";

  public vaccinationResource: string = "";
  public vaccinationAnswer: string = "";
  public vaccinationError: string = "";

  constructor(private vaccinationService: VaccinationService,
    private fhirService: FhirService,
    private _snackBar: MatSnackBar,
    public _dialogRef: MatDialogRef<FhirMessagingComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {patientId: number, vaccinationId: number}) {
      this.patientId = data.patientId
      this.vaccinationId = data.vaccinationId
     }

  ngOnInit(): void {
    this.fhirService.quickGetPatient(this.patientId).subscribe((resource) => {
      this.patientResource = resource
    })
    this.fhirService.quickGetVaccination(this.patientId,this.vaccinationId).subscribe((resource) => {
      this.vaccinationResource = resource
    })
  }

  sendPatient() {
    this.fhirService.quickPostPatient(this.patientId,this.vaccinationId, this.patientResource).subscribe(
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
    this.fhirService.quickPostVaccination(this.patientId,this.vaccinationId, this.vaccinationResource).subscribe(
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
