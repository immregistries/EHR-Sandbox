import { Component, Inject, Input, OnInit } from '@angular/core';
import { Hl7Service } from 'src/app/core/_services/_fhir/hl7.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { tap } from 'rxjs';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-hl7-messaging',
  templateUrl: './hl7-messaging.component.html',
  styleUrls: ['./hl7-messaging.component.css']
})
export class Hl7MessagingComponent {

  @Input() vaccinationId: number = -1;
  @Input() patientId: number = -1;

  constructor(public _dialogRef: MatDialogRef<Hl7MessagingComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { patientId: number, vaccinationId: number }) {
    if (data) {
      this.patientId = data.patientId
      this.vaccinationId = data.vaccinationId
    }
  }

}
