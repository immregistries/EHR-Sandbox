import { Component, Inject, Input, OnInit, Optional } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialog } from '@angular/material/dialog';
import { EhrPatient, VaccinationEvent } from 'src/app/core/_model/rest';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';
import { FhirMessagingComponent } from 'src/app/fhir/_components/fhir-messaging/fhir-messaging.component';
import { FhirService } from 'src/app/fhir/_services/fhir.service';
import { VaccinationFormComponent } from '../vaccination-form/vaccination-form.component';
import { PatientService } from 'src/app/core/_services/patient.service';

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
    private fhirService: FhirService,
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
    this.fhirService.loadEverythingFromPatient(this.patientId ?? -1).subscribe((res) => {
      this.loading = false
      this.vaccinationEvents = res
    })
  }

}
