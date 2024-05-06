import { AfterViewChecked, AfterViewInit, Component, Inject, Input, OnInit, Optional, ViewChild, ViewEncapsulation } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatTabGroup } from '@angular/material/tabs';
import { FhirResourceService } from '../../_services/fhir-resource.service';
import { Observable, ObservableLike } from 'rxjs';
import { Hl7Service } from '../../_services/hl7.service';

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

  public show_hl7_tab: boolean = false
  public hl7Message: string = ""
  public hl7FhirTransaction: string = ""
  public loading: boolean = false

  constructor(private fhirResourceService: FhirResourceService,
    private hl7Service: Hl7Service,
    @Optional() public _dialogRef: MatDialogRef<FhirMessagingComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: {
      patientId: number,
      vaccinationId?: number,
      resource?: string,
      resourceObservable: Observable<string>,
      resourceType?: string,
      resourceLocalId?: number,
      operation?: "UpdateOrCreate" | "Create" | "Update" | "$match" | "$transaction" | "",
      show_hl7_tab? : boolean
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
    /**
         * Special cases with extra tab for hl7
         */
    this.getHl7Message()
  }

  getHl7Message() {
    if (this.show_hl7_tab) {
      this.loading = true
      if (this.vaccinationId > 0) {
        this.hl7Service.getVXU(this.patientId, this.vaccinationId).subscribe((res) => {
          this.hl7Message = res
          this.loading = false
        })
        this.fhirResourceService.getVaccinationExportBundle(this.patientId, this.vaccinationId).subscribe((resource) => {
          this.hl7FhirTransaction = resource
        })
      } else if (this.patientId > 0) {
        this.hl7Service.getQBP(this.patientId).subscribe((res) => {
          this.hl7Message = res
          this.loading = false
        })
        this.fhirResourceService.getPatientExportBundle(this.patientId).subscribe((resource) => {
          this.hl7FhirTransaction = resource
        })
      } else {
        this.loading = false
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
