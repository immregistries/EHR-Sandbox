import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { VaccinationEvent } from 'src/app/core/_model/rest';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';

@Component({
  selector: 'app-vaccination-dashboard-dialog',
  templateUrl: './vaccination-dashboard-dialog.component.html',
  styleUrls: ['./vaccination-dashboard-dialog.component.css']
})
export class VaccinationDashboardDialogComponent implements OnInit {
  vaccination!: VaccinationEvent

  constructor(private vaccinationService: VaccinationService,
    public _dialogRef: MatDialogRef<VaccinationDashboardDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {vaccination: number}) {
      if(data.vaccination) {
        this.vaccinationService.quickReadVaccinationFromFacility(data.vaccination).subscribe((res) => {
          this.vaccination = res
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
