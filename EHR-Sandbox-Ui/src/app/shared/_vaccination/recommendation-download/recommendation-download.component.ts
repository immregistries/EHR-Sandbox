import { Component, Inject, Input, Optional } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { EhrPatient } from 'src/app/core/_model/rest';
import { FhirClientService } from 'src/app/core/_services/_fhir/fhir-client.service';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { RecommendationService } from 'src/app/core/_services/recommendation.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';

@Component({
  selector: 'app-recommendation-download',
  templateUrl: './recommendation-download.component.html',
  styleUrls: ['./recommendation-download.component.css']
})
export class RecommendationDownloadComponent {
  @Input()
  patientId = -1

  constructor(private patientService: PatientService,
    private snackBarService: SnackBarService,
    private recommendationService: RecommendationService,
    private facilityService: FacilityService,
    private fhirClient: FhirClientService,
    @Optional() public _dialogRef: MatDialogRef<RecommendationDownloadComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { patientId: number }) {
    if (data && data.patientId) {
      this.patientId = data.patientId;
    }
  }

  fetch() {
    this.fhirClient.immdsForecast(this.facilityService.getCurrentId(), this.patientId).subscribe((res) => {
      this._dialogRef.close(res)
    })
  }
}
