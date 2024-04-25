import { AfterViewChecked, AfterViewInit, Component, Inject, Input, OnInit, Optional, ViewChild, ViewEncapsulation } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatTabGroup } from '@angular/material/tabs';
import { FhirResourceService } from '../../_services/fhir-resource.service';
import { Observable, ObservableLike } from 'rxjs';

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
  @Input()
  public genericOperation: "UpdateOrCreate" | "Create" | "Update" | "$match" | "$transaction" | "" = "UpdateOrCreate";
  genericLoading: Boolean = false
  public style: string = 'width: 50%'

  public patientFhirId = "";

  constructor(private fhirResourceService: FhirResourceService,
    @Optional() public _dialogRef: MatDialogRef<FhirMessagingComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: {
      patientId: number,
      vaccinationId?: number,
      resource?: string,
      resourceObservable: Observable<string>,
      resourceType?: string,
      resourceLocalId?: number,
      operation?: "UpdateOrCreate" | "Create" | "Update" | "$match" | "$transaction" | "" }) {
    if (data) {
      if (data.resourceObservable) {
        this.genericResourceType = data.resourceType ?? "Patient"
        this.genericLocalId = data.resourceLocalId ?? -1
        this.genericOperation = data.operation ?? "UpdateOrCreate"
        this.genericLoading = true
        data.resourceObservable.subscribe((res) => {
          this.genericLoading = false
          this.genericResource = res
        })
      } else if (data.resource) {
        this.genericResource = data.resource
        this.genericResourceType = data.resourceType ?? "Patient"
        this.genericLocalId = data.resourceLocalId ?? -1
        this.genericOperation = data.operation ?? "UpdateOrCreate"
      } else {
        /**
         * Special cases with extra tab
         */
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
