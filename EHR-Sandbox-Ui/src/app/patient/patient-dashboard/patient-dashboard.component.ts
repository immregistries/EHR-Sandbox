import { Component, Inject, Input, Optional } from '@angular/core';
import { map } from 'rxjs/operators';
import { Breakpoints, BreakpointObserver } from '@angular/cdk/layout';
import { EhrPatient } from 'src/app/core/_model/rest';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { PatientService } from 'src/app/core/_services/patient.service';

@Component({
  selector: 'app-patient-dashboard',
  templateUrl: './patient-dashboard.component.html',
  styleUrls: ['./patient-dashboard.component.css']
})
export class PatientDashboardComponent {
  @Input() patient!: EhrPatient

  constructor(private breakpointObserver: BreakpointObserver,
    private patientService: PatientService,
    public _dialogRef: MatDialogRef<PatientDashboardComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: {patient?: EhrPatient | number}) {
      if(data?.patient) {
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
}
