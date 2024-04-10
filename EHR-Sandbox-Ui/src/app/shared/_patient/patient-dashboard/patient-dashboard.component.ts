import { Component, Inject, Input, Optional } from '@angular/core';
import { EhrPatient, VaccinationEvent } from 'src/app/core/_model/rest';
import { PatientService } from 'src/app/core/_services/patient.service';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Observable, merge } from 'rxjs';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';

@Component({
  selector: 'app-patient-dashboard',
  templateUrl: './patient-dashboard.component.html',
  styleUrls: ['./patient-dashboard.component.css']
})
export class PatientDashboardComponent {
  @Input() patient!: EhrPatient

  constructor(private patientService: PatientService,
    private vaccinationService: VaccinationService,
    @Optional() public _dialogRef: MatDialogRef<PatientDashboardComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { patient?: EhrPatient | number }) {
    if (data?.patient) {
      if (typeof data.patient === "number" || typeof data.patient === "string") {
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

  vaccinationListRefreshObservable(): Observable<any> {
    return merge(this.patientService.getRefresh(), this.patientService.getCurrentObservable())
  }

  vaccinationListObservable(): Observable<VaccinationEvent[]> {
    return this.vaccinationService.quickReadVaccinations()
  }
}
