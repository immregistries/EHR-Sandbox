import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Patient } from 'src/app/core/_model/rest';
import { PatientService } from 'src/app/core/_services/patient.service';

@Component({
  selector: 'app-patient-dashboard-dialog',
  templateUrl: './patient-dashboard-dialog.component.html',
  styleUrls: ['./patient-dashboard-dialog.component.css']
})
export class PatientDashboardDialogComponent implements OnInit {
  patient!: Patient

  constructor( private patientService: PatientService,
    public _dialogRef: MatDialogRef< PatientDashboardDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {patient: number}) {
      if(data.patient) {
        this.patientService.quickReadPatient(data.patient).subscribe((res) => {
          this.patient = res
        })
        // if (typeof(data.patient) === 'number') {
        //   this.patientService.quickReadPatient(data.patient).subscribe((res) => {
        //     this.patient = res
        //   })
        // } else {
        //   this.patient = data.patient
        // }
      }
     }

  ngOnInit(): void {
  }

}
