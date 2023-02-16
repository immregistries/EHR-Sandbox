import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-fhir-dialog',
  templateUrl: './fhir-dialog.component.html',
  styleUrls: ['./fhir-dialog.component.css']
})
export class FhirDialogComponent {
  vaccinationId!: number;
  patientId!: number;

  constructor(
    public _dialogRef: MatDialogRef<FhirDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {patientId: number, vaccinationId: number}) {
      this.patientId = data.patientId
      if (data.vaccinationId) {
        this.vaccinationId = data.vaccinationId
      }
     }

}
