import { AfterViewInit, Component, Inject, Input, OnInit, Optional, ViewChild, ViewEncapsulation } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatTabGroup } from '@angular/material/tabs';
import { FhirResourceService } from '../../../core/_services/_fhir/fhir-resource.service';
import { Observable } from 'rxjs';
import { Hl7Service } from '../../../core/_services/_fhir/hl7.service';
import { FhirV2Service } from 'src/app/core/_services/_fhir/fhir-v2.service';

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

  /** no longer used, used to be in an extra form for overriding id in resource */
  public patientFhirId = "";

  public show_hl7_tab: boolean = false

  constructor(private fhirResourceService: FhirResourceService,
    @Optional() public _dialogRef: MatDialogRef<FhirMessagingComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: {
      patientId: number,
      vaccinationId?: number,
      resource?: string,
      resourceObservable: Observable<string>,
      resourceType?: string,
      resourceLocalId?: number,
      operation?: "UpdateOrCreate" | "Create" | "Update" | "$match" | "$transaction" | "",
      show_hl7_tab?: boolean
    }) {
    if (data) {
      if (data.show_hl7_tab) {
        this.show_hl7_tab = data.show_hl7_tab
      }
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
