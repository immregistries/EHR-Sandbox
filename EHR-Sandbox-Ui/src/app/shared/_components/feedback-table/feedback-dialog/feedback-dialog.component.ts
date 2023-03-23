import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { EhrPatient, VaccinationEvent } from 'src/app/core/_model/rest';
import { PatientService } from 'src/app/core/_services/patient.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';

@Component({
  selector: 'app-feedback-dialog',
  templateUrl: './feedback-dialog.component.html',
  styleUrls: ['./feedback-dialog.component.css']
})
export class FeedbackDialogComponent implements OnInit {
  patient?: EhrPatient;
  vaccination?: VaccinationEvent;

  constructor( public _dialogRef: MatDialogRef<FeedbackDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {patient: EhrPatient, vaccination: VaccinationEvent}) {
      if (data.patient){
        this.patient = data.patient;
      }
      if (data.vaccination){
        this.vaccination = data.vaccination;
      }
    }

  ngOnInit(): void {
  }

}
