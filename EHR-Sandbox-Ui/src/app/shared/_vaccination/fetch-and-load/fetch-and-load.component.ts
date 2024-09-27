import { Component, Inject, Input, OnInit, Optional } from '@angular/core';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';
import { FhirMessagingComponent } from 'src/app/shared/_fhir/fhir-messaging/fhir-messaging.component';
import { PatientService } from 'src/app/core/_services/patient.service';
import { VaccinationEvent } from 'src/app/core/_model/rest';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { FhirClientService } from 'src/app/core/_services/_fhir/fhir-client.service';
import { Observable, of } from 'rxjs';
import { SmartHealthLinkImportComponent } from '../../_fhir/smart-health-link-import/smart-health-link-import.component';
import { error } from 'console';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';

@Component({
  selector: 'app-fetch-and-load',
  templateUrl: './fetch-and-load.component.html',
  styleUrls: ['./fetch-and-load.component.css']
})
export class FetchAndLoadComponent implements OnInit {

  @Input()
  patientId?: number;

  loading: boolean = false;

  selectedVaccination: VaccinationEvent | null = null;

  remoteVaccinations: VaccinationEvent[] = [];

  constructor(
    private dialog: MatDialog,
    private fhirClient: FhirClientService,
    public vaccinationService: VaccinationService,
    public patientService: PatientService,
    public snackBarService: SnackBarService,
    @Optional() public _dialogRef: MatDialogRef<FhirMessagingComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { patientId: number }) {
    this.patientId = data?.patientId
  }

  ngOnInit(): void {
  }

  loadEverythingFromPatient() {
    this.loading = true
    this.fhirClient.loadEverythingFromPatient(this.patientId ?? -1).subscribe({
      next: (res) => {
        this.loading = false
        this.remoteVaccinations = res
      },
      error: (error) => {
        this.loading = false
        console.error(error)
        this.snackBarService.errorMessage(error.error)
      }
    })
  }

  shlink(url?: string) {
    this.dialog.open(SmartHealthLinkImportComponent, {
      maxWidth: '95vw',
      maxHeight: '98vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-without-bar',
      data: { patientId: this.patientId, url: url ?? "" }
    }).afterClosed().subscribe((res) => {
      if (res) {
        this.remoteVaccinations = res
      }
    })
  }

  selectVaccination(value: VaccinationEvent | null | undefined) {
    this.selectedVaccination = value ?? null
  }

  refreshLocalHistoryObservable(): Observable<boolean> {
    return of(true)
  }

}
