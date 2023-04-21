import { AfterViewChecked, AfterViewInit, Component, Inject, Input, OnInit, Optional, ViewChild, ViewEncapsulation } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatTabGroup } from '@angular/material/tabs';
import { FeedbackService } from 'src/app/core/_services/feedback.service';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { FhirService } from 'src/app/fhir/_services/fhir.service';

@Component({
  selector: 'app-fhir-messaging',
  templateUrl: './fhir-messaging.component.html',
  styleUrls: ['./fhir-messaging.component.css'],
  encapsulation: ViewEncapsulation.None,
})
export class FhirMessagingComponent implements AfterViewInit {
  @ViewChild('tabs', {static: false}) tabGroup!: MatTabGroup;

  patientLoading: Boolean = false
  vaccinationLoading: Boolean = false
  patientRequestLoading: Boolean = false
  vaccinationRequestLoading: Boolean = false

  @Input() vaccinationId!: number;
  @Input() patientId: number = -1;

  public patientResource: string = "";
  public patientAnswer: string = "";
  public patientError: boolean = false;

  public vaccinationResource: string = "";
  public vaccinationAnswer: string = "";
  public vaccinationError: boolean = false;

  public style: string = 'width: 50%'

  public autofillId: boolean = false;
  public patientFhirId = "";

  constructor(private fhirService: FhirService,
    private snackBarService: SnackBarService,
    private feedbackService: FeedbackService,
    private immRegistriesService: ImmunizationRegistryService,
    @Optional() public _dialogRef: MatDialogRef<FhirMessagingComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: {patientId: number, vaccinationId?: number}) {
      if (data) {
        this.patientId = data.patientId
        if (data.vaccinationId) {
          this.vaccinationId = data.vaccinationId
        }
      }
     }

  public patientOperation:  "UpdateOrCreate" | "Create" | "Update" = "UpdateOrCreate";
  public immunizationOperation:  "UpdateOrCreate" | "Create" | "Update" = "UpdateOrCreate";

  ngAfterViewInit(): void {
    this.tabGroup.selectedIndex = 1;
    this.patientLoading = true
    this.fhirService.quickGetPatientResource(this.patientId).subscribe((resource) => {
      this.patientResource = resource
      this.patientLoading = false
    })
    if (this.vaccinationId){
      this.vaccinationLoading = true
      this.fhirService.quickGetImmunizationResource(this.patientId,this.vaccinationId).subscribe((resource) => {
        this.vaccinationResource = resource
        this.vaccinationLoading = false
      })
    }
  }


}
