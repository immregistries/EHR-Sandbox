import { Component, Input, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { PatientService } from 'src/app/core/_services/patient.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';
import { FhirMessagingComponent } from 'src/app/shared/_fhir/fhir-messaging/fhir-messaging.component';
import { Hl7MessagingComponent } from 'src/app/shared/_fhir/hl7-messaging/hl7-messaging.component';
import { LocalCopyDialogComponent } from 'src/app/shared/_components/local-copy-dialog/local-copy-dialog.component';
import { VaccinationFormComponent } from '../vaccination-form/vaccination-form.component';
import { VaccinationEvent } from 'src/app/core/_model/rest';

@Component({
  selector: 'app-vaccination-tools',
  templateUrl: './vaccination-tools.component.html',
  styleUrls: ['./vaccination-tools.component.css']
})
export class VaccinationToolsComponent implements OnInit {
  @Input() patientId!: number;
  @Input() vaccination!: VaccinationEvent;

  constructor(private dialog: MatDialog,
    private patientService: PatientService,
    private vaccinationService: VaccinationService,
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
      data: { patientId: this.patientId, vaccination: this.vaccination },
    });
    dialogRef.afterClosed().subscribe(result => {
      this.vaccinationService.doRefresh()
      if (result) {
        this.vaccination = result
      }
    });
  }

  openHl7() {
    const dialogRef = this.dialog.open(Hl7MessagingComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: { patientId: this.patientId, vaccinationId: this.vaccination.id },
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
      data: { patientId: this.patientId, vaccinationId: this.vaccination.id, show_hl7_tab: true },
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
      data: { patient: this.patientId, vaccination: this.vaccination },
    });
  }


}
