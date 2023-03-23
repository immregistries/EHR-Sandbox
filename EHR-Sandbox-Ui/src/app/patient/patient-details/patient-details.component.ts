import { Component, Input, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { FhirMessagingComponent } from 'src/app/fhir/_components/fhir-messaging/fhir-messaging.component';
import { LocalCopyDialogComponent } from 'src/app/shared/_components/_dialogs/local-copy-dialog/local-copy-dialog.component';
import { EhrPatient } from '../../core/_model/rest';
import { PatientService } from '../../core/_services/patient.service';
import { PatientFormComponent } from '../patient-form/patient-form.component';

@Component({
  selector: 'app-patient-details',
  templateUrl: './patient-details.component.html',
  styleUrls: ['./patient-details.component.css']
})
export class PatientDetailsComponent implements OnInit {
  @Input() patient!: EhrPatient

  constructor(private dialog: MatDialog,
    private patientService: PatientService,) { }

  ngOnInit(): void {
  }

  openEdition(element: EhrPatient) {
    const dialogRef = this.dialog.open(PatientFormComponent, {
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

  openFhir(element: EhrPatient) {
    const dialogRef = this.dialog.open(FhirMessagingComponent, {
      maxWidth: '95vw',
      maxHeight: '98vh',
      height: 'fit-content',
      width: '50%',
      panelClass: 'dialog-without-bar',
      data: {patientId: element.id},
    });
    dialogRef.afterClosed().subscribe(result => {
      this.patientService.doRefresh()
    });
  }

  openCopy(element: EhrPatient) {
    const dialogRef = this.dialog.open(LocalCopyDialogComponent, {
      maxWidth: '95vw',
      maxHeight: '98vh',
      height: 'fit-content',
      width: '50%',
      panelClass: 'dialog-without-bar',
      data: {patient: element},
    });
    dialogRef.afterClosed().subscribe(result => {
      this.patientService.doRefresh()
    });
  }

}
