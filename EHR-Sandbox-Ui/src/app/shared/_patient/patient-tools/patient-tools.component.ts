import { Component, Input, OnInit } from '@angular/core';
import { FhirMessagingComponent } from 'src/app/fhir/_components/fhir-messaging/fhir-messaging.component';
import { Hl7MessagingComponent } from 'src/app/fhir/_components/hl7-messaging/hl7-messaging.component';
import { LocalCopyDialogComponent } from 'src/app/shared/_components/local-copy-dialog/local-copy-dialog.component';
import { EhrPatient } from '../../../core/_model/rest';
import { PatientService } from '../../../core/_services/patient.service';
import { PatientFormComponent } from '../patient-form/patient-form.component';
import { FetchAndLoadComponent } from '../../_vaccination/fetch-and-load/fetch-and-load.component';
import { MatDialog } from '@angular/material/dialog';
import { FhirResource } from 'fhir/r5';
import { FhirResourceService } from 'src/app/fhir/_services/fhir-resource.service';

@Component({
  selector: 'app-patient-tools',
  templateUrl: './patient-tools.component.html',
  styleUrls: ['./patient-tools.component.css']
})
export class PatientToolsComponent implements OnInit {
  @Input() patient!: EhrPatient

  constructor(private dialog: MatDialog,
    public patientService: PatientService,
    public fhirResourceService: FhirResourceService) { }

  ngOnInit(): void {
  }

  openEdition() {
    const dialogRef = this.dialog.open(PatientFormComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: {patient: this.patient},
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.patientService.setCurrent(result)
      }
    });
  }

  openFhir() {
    const dialogRef = this.dialog.open(FhirMessagingComponent, {
      maxWidth: '95vw',
      maxHeight: '98vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-without-bar',
      data: {patientId: this.patient.id},
    });
    dialogRef.afterClosed().subscribe(result => {
      this.patientService.doRefresh()
    });
  }

  openCopy() {
    const dialogRef = this.dialog.open(LocalCopyDialogComponent, {
      maxWidth: '95vw',
      maxHeight: '98vh',
      height: 'fit-content',
      width: '50%',
      panelClass: 'dialog-without-bar',
      data: {patient: this.patient},
    });
    dialogRef.afterClosed().subscribe(result => {
      this.patientService.doRefresh()
    });
  }

  openHl7() {
    const dialogRef = this.dialog.open(Hl7MessagingComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: {patientId: this.patient.id},
    });
    dialogRef.afterClosed().subscribe(result => {
      this.patientService.doRefresh()
    });
  }

  openFetch() {
    const dialogRef = this.dialog.open(FetchAndLoadComponent, {
      maxWidth: '98vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: {patientId: this.patient.id},
    });
    dialogRef.afterClosed().subscribe(result => {
      this.patientService.doRefresh()
    });
  }


}
