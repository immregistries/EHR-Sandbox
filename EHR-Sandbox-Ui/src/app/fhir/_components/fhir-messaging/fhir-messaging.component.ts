import { AfterViewChecked, AfterViewInit, Component, Inject, Input, OnInit, Optional, ViewChild, ViewEncapsulation } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatTabGroup } from '@angular/material/tabs';
import { FhirResourceService } from '../../_services/fhir-resource.service';

@Component({
  selector: 'app-fhir-messaging',
  templateUrl: './fhir-messaging.component.html',
  styleUrls: ['./fhir-messaging.component.css'],
  encapsulation: ViewEncapsulation.None,
})
export class FhirMessagingComponent implements AfterViewInit {
  @ViewChild('tabs', { static: false }) tabGroup!: MatTabGroup;

  patientLoading: Boolean = false
  vaccinationLoading: Boolean = false

  @Input() vaccinationId!: number;
  @Input() patientId: number = -1;
  public patientResource: string = "";
  public vaccinationResource: string = "";

  @Input()
  public genericLocalId: number = -1;
  @Input()
  public genericResource: string = "";
  @Input()
  public genericResourceType: string = "Patient";
  public style: string = 'width: 50%'

  public patientFhirId = "";

  constructor(private fhirResourceService: FhirResourceService,
    @Optional() public _dialogRef: MatDialogRef<FhirMessagingComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { patientId: number, vaccinationId?: number, resource?: string, resourceType?: string, resourceLocalId?: number }) {
    if (data) {
      if (data.resource) {
        this.genericResource = data.resource
        this.genericResourceType = data.resourceType ?? "Patient"
        this.genericLocalId = data.resourceLocalId ?? -1
      } else {
        if (data.patientId) {
          this.patientId = data.patientId
          this.patientLoading = true
          this.fhirResourceService.quickGetPatientResource(this.patientId).subscribe((resource) => {
            this.genericResource = resource
            this.genericResourceType = "Patient"
            this.genericLocalId = data.patientId

            this.patientResource = resource
            this.patientLoading = false
          })
          if (data.vaccinationId) {
            this.vaccinationId = data.vaccinationId
            this.vaccinationLoading = true
            this.fhirResourceService.quickGetImmunizationResource(this.patientId, this.vaccinationId).subscribe((resource) => {
              this.vaccinationResource = resource
              this.vaccinationLoading = false
            })
          }
        }
      }
    }
  }

  ngAfterViewInit(): void {
    this.tabGroup.selectedIndex = 1;
    // if (this.patientId) {
    //   this.patientLoading = true
    //   this.fhirResourceService.quickGetPatientResource(this.patientId).subscribe((resource) => {
    //     this.patientResource = resource
    //     this.patientLoading = false
    //   })
    //   if (this.vaccinationId) {
    //     this.vaccinationLoading = true
    //     this.fhirResourceService.quickGetImmunizationResource(this.patientId, this.vaccinationId).subscribe((resource) => {
    //       this.vaccinationResource = resource
    //       this.vaccinationLoading = false
    //     })
    //   }
    // }
  }


}
