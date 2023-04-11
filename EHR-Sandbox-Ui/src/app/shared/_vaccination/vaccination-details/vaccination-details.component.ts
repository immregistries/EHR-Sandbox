import { Component, Input, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { EhrPatient, VaccinationEvent } from 'src/app/core/_model/rest';
import { PatientService } from 'src/app/core/_services/patient.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';
import { FhirMessagingComponent } from 'src/app/fhir/_components/fhir-messaging/fhir-messaging.component';
import { Hl7MessagingComponent } from 'src/app/fhir/_components/hl7-messaging/hl7-messaging.component';
import { LocalCopyDialogComponent } from 'src/app/shared/_components/_dialogs/local-copy-dialog/local-copy-dialog.component';
import { VaccinationFormComponent } from '../vaccination-form/vaccination-form.component';

@Component({
  selector: 'app-vaccination-details',
  templateUrl: './vaccination-details.component.html',
  styleUrls: ['./vaccination-details.component.css']
})
export class VaccinationDetailsComponent implements OnInit {
  @Input() patientId!: number;
  @Input() vaccination!: VaccinationEvent;

  constructor(private dialog: MatDialog,
    private vaccinationService: VaccinationService,
    private patientService: PatientService,
    ) { }

  ngOnInit(): void {
  }

  openEdition() {
    const dialogRef = this.dialog.open(VaccinationFormComponent, {
      maxWidth: '98vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: {patientId: this.patientId, vaccination: this.vaccination},
    });
    dialogRef.afterClosed().subscribe(result => {
      this.vaccinationService.doRefresh()
    });
  }

  openHl7() {
    const dialogRef = this.dialog.open(Hl7MessagingComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: {patientId: this.patientId, vaccinationId: this.vaccination.id},
    });
    dialogRef.afterClosed().subscribe(result => {
      this.patientService.doRefresh()
    });
  }

  openFhir() {
    const dialogRef = this.dialog.open(FhirMessagingComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-without-bar',
      data: {patientId: this.patientId, vaccinationId: this.vaccination.id},
    });
    dialogRef.afterClosed().subscribe(result => {
      /**
       * patient refresh is called because of feedback update
       */
      this.patientService.doRefresh()
    });
  }

  openCopy() {
    const dialogRef = this.dialog.open(LocalCopyDialogComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-without-bar',
      data: {patient: this.patientId, vaccination: this.vaccination},
    });
  }


}
