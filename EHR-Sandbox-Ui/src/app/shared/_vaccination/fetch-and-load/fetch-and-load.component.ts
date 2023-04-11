import { Component, Inject, Input, OnInit, Optional } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { EhrPatient, VaccinationEvent } from 'src/app/core/_model/rest';
import { FhirMessagingComponent } from 'src/app/fhir/_components/fhir-messaging/fhir-messaging.component';
import { FhirService } from 'src/app/fhir/_services/fhir.service';

@Component({
  selector: 'app-fetch-and-load',
  templateUrl: './fetch-and-load.component.html',
  styleUrls: ['./fetch-and-load.component.css']
})
export class FetchAndLoadComponent implements OnInit {

  @Input()
  patientId?: number;

  vaccinationEvents: VaccinationEvent[] = [];

  constructor(private fhirService: FhirService,
    @Optional() public _dialogRef: MatDialogRef<FhirMessagingComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: {patientId: number}) {
      if (data) {
        this.patientId = data.patientId
      }

     }

  ngOnInit(): void {
  }

  loadEverythingFromPatient() {
    this.fhirService.loadEverythingFromPatient(this.patientId ?? -1).subscribe((res) => {
      this.vaccinationEvents = res
    })
  }

}
