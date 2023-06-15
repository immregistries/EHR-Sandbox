import { Component, Inject, Input, OnInit, Optional } from '@angular/core';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';
import { FhirMessagingComponent } from 'src/app/fhir/_components/fhir-messaging/fhir-messaging.component';
import { PatientService } from 'src/app/core/_services/patient.service';
import { VaccinationEvent } from 'src/app/core/_model/rest';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { FhirClientService } from 'src/app/fhir/_services/fhir-client.service';

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

  vaccinationEvents: VaccinationEvent[] = [];

  constructor(
    private dialog: MatDialog,
    private fhirClient: FhirClientService,
    public vaccinationService: VaccinationService,
    public patientService: PatientService,
    @Optional() public _dialogRef: MatDialogRef<FhirMessagingComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: {patientId: number}) {
      this.patientId = data?.patientId
     }

  ngOnInit(): void {
  }

  loadEverythingFromPatient() {
    this.loading = true
    this.fhirClient.loadEverythingFromPatient(this.patientId ?? -1).subscribe((res) => {
      this.loading = false
      this.vaccinationEvents = res
    })
  }

}
