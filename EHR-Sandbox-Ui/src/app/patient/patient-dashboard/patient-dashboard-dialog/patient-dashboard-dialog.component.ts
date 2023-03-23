import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { EhrPatient } from 'src/app/core/_model/rest';
import { PatientService } from 'src/app/core/_services/patient.service';

@Component({
  selector: 'app-patient-dashboard-dialog',
  templateUrl: './patient-dashboard-dialog.component.html',
  styleUrls: ['./patient-dashboard-dialog.component.css']
})
export class PatientDashboardDialogComponent implements OnInit {
  patient!: EhrPatient

  constructor( private patientService: PatientService,
    public _dialogRef: MatDialogRef< PatientDashboardDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {patient?: EhrPatient | number}) {
      if(data.patient) {
        if (typeof data.patient === "number" ||  "string") {
          this.patientService.quickReadPatient(+data.patient).subscribe((res) => {
            this.patient = res
          });
        } else if (data.patient.id) {
          this.patientService.quickReadPatient(data.patient.id).subscribe((res) => {
            this.patient = res
          });
        }
      }
     }
  ngOnInit(): void {
  }

}
