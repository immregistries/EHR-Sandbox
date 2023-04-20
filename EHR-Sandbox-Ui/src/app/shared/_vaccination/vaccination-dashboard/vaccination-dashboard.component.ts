import { Component, Inject, Input, OnInit, Optional } from '@angular/core';
import { VaccinationEvent } from 'src/app/core/_model/rest';
import { CodeMapsService } from 'src/app/core/_services/code-maps.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-vaccination-dashboard',
  templateUrl: './vaccination-dashboard.component.html',
  styleUrls: ['./vaccination-dashboard.component.css']
})
export class VaccinationDashboardComponent implements OnInit {
  @Input() vaccination!: VaccinationEvent

  constructor(public codeMapsService: CodeMapsService,
    private vaccinationService: VaccinationService,
    private patientService: PatientService,
    @Optional() public _dialogRef: MatDialogRef<VaccinationDashboardComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: {vaccination: number | VaccinationEvent}) {
      if(data?.vaccination) {
        this.patientService.getRefresh().subscribe((res) => {
          if (typeof data.vaccination === "number" ||  "string") {
            this.vaccinationService.quickReadVaccinationFromFacility(+data.vaccination).subscribe((res) => {
              this.vaccination = res
            })
          } else if (data.vaccination.id) {
            this.vaccinationService.quickReadVaccinationFromFacility(data.vaccination.id).subscribe((res) => {
              this.vaccination = res
            })
          }
        })
      }
    }

  ngOnInit(): void {
    this.codeMapsService.getCodeMap("cvx")
  }

}
