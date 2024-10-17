import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { EhrPatient } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { PatientFormComponent } from '../patient-form/patient-form.component';

@Component({
  selector: 'app-patient-menu',
  templateUrl: './patient-menu.component.html',
  styleUrls: ['./patient-menu.component.css']
})
export class PatientMenuComponent implements OnInit {

  constructor(public patientService: PatientService, public facilityService: FacilityService, public dialog: MatDialog) { }


  list?: EhrPatient[];

  ngOnInit(): void {
    this.facilityService.getCurrentObservable().subscribe(facility => {
      this.patientService.quickReadPatients().subscribe((res) => {
        this.list = res
      })
    })
  }

  onSelection(event: EhrPatient) {
    if (this.patientService.getCurrentId() == event.id) { // unselect
      this.patientService.setCurrent({ names: [] })
    } else {
      this.patientService.setCurrent(event)
    }
  }

  openDialog() {
    const dialogRef = this.dialog.open(PatientFormComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '90%',
      panelClass: 'dialog-with-bar',
      data: {},
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.patientService.doRefresh()
      }
    });
  }

}
