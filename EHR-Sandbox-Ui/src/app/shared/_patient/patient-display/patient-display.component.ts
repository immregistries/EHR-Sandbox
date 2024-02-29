import { Component, Inject, Input, Optional } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { EhrPatient } from 'src/app/core/_model/rest';
import { FormCard } from 'src/app/core/_model/structure';
import { PatientService } from 'src/app/core/_services/patient.service';

@Component({
  selector: 'app-patient-display',
  templateUrl: './patient-display.component.html',
  styleUrls: ['./patient-display.component.css']
})
export class PatientDisplayComponent {

  @Input()
  patient!: any;

  @Input()
  formCards!: FormCard[];

  isEditionMode: boolean = false;

  constructor(@Optional() public _dialogRef: MatDialogRef<PatientDisplayComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { patient: EhrPatient }) {
    if (data && data.patient) {
      this.patient = data.patient;
    }
  }

}
