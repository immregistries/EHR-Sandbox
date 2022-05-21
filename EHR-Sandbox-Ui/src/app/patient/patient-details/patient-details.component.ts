import { Component, Input, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { FhirDialogComponent } from 'src/app/fhir/_components/fhir-dialog/fhir-dialog.component';
import { Patient } from '../../core/_model/rest';
import { PatientService } from '../../core/_services/patient.service';
import { PatientCreationComponent } from '../patient-form/patient-creation/patient-creation.component';

@Component({
  selector: 'app-patient-details',
  templateUrl: './patient-details.component.html',
  styleUrls: ['./patient-details.component.css']
})
export class PatientDetailsComponent implements OnInit {
  @Input() patient!: Patient

  constructor(private dialog: MatDialog,
    private patientService: PatientService,) { }

  ngOnInit(): void {
  }

  openEdition(element: Patient) {
    const dialogRef = this.dialog.open(PatientCreationComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: {patient: element},
    });
    dialogRef.afterClosed().subscribe(result => {
      this.patientService.doRefresh()
    });
  }

  openFhir(element: Patient) {
    const dialogRef = this.dialog.open(FhirDialogComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '50%',
      panelClass: 'dialog-without-bar',
      data: {patientId: element.id},
    });
    dialogRef.afterClosed().subscribe(result => {
      this.patientService.doRefresh()
    });
  }

}
