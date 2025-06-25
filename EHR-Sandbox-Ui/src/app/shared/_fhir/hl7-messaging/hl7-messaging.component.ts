import { AfterViewInit, Component, Inject, Input, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { Hl7Service } from 'src/app/core/_services/_fhir/hl7.service';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatTabGroup } from '@angular/material/tabs';
import { count } from 'console';
import { ImmunizationRegistry } from 'src/app/core/_model/rest';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';

@Component({
  selector: 'app-hl7-messaging',
  templateUrl: './hl7-messaging.component.html',
  styleUrls: ['./hl7-messaging.component.css'],
  encapsulation: ViewEncapsulation.None,
})
export class Hl7MessagingComponent implements AfterViewInit, OnInit {
  @ViewChild('tabs', { static: false }) tabGroup!: MatTabGroup;

  @Input() vaccinationId: number = -1;
  @Input() patientId: number = -1;

  @Input() vxuLoading: boolean = false
  @Input() qbpLoading: boolean = false

  vxu: string = "";
  qbp: string = "";

  constructor(public _dialogRef: MatDialogRef<Hl7MessagingComponent>,
    private hl7Service: Hl7Service, private immunizationRegistryService: ImmunizationRegistryService,
    @Inject(MAT_DIALOG_DATA) public data: { patientId: number, vaccinationId: number }) {
    if (data) {
      this.patientId = data.patientId ?? -1
      this.vaccinationId = data.vaccinationId ?? -1
    }
    this.immunizationRegistryService.getCurrentObservable().subscribe((registry) => {
      if (this.reloadOnChange) {
        this.vxu = this.updateMsh6(this.vxu, registry)
        this.qbp = this.updateMsh6(this.qbp, registry)
      }
    })
  }

  ngOnInit(): void {
    if (this.vaccinationId > 0) {
      this.vxuLoading = true
      this.hl7Service.getVXU(this.patientId, this.vaccinationId).subscribe((res) => {
        this.vxu = this.updateMsh6(res, this.immunizationRegistryService.getCurrent())
        this.vxuLoading = false
      })
    } else if (this.patientId > 0) {
      this.qbpLoading = true
      this.hl7Service.getQBP(this.patientId).subscribe((res) => {
        this.qbp = this.updateMsh6(res, this.immunizationRegistryService.getCurrent())
        this.qbpLoading = false
      })

      this.vxuLoading = true
      this.hl7Service.getVXUAll(this.patientId).subscribe((res) => {
        this.vxu = this.updateMsh6(res, this.immunizationRegistryService.getCurrent())
        this.vxuLoading = false
      })
    }
  }

  ngAfterViewInit(): void {
    this.tabGroup.selectedIndex = 1;
  }

  public reloadOnChange: boolean = false;
  updateMsh6(message: string, registry: ImmunizationRegistry): string {
    console.info(this.reloadOnChange, registry.receivingFacility)
    if (!this.reloadOnChange) {
      return message;
    }
    // if ((registry.receivingFacility?.length ?? 0) < 1) {
    //   return message;
    // }
    if (message.startsWith(`MSH|^~\\&|`)) {
      let count = 0
      let start = 0
      while (count++ < 5) {
        start = message.indexOf("|", start + 1)
      }
      let end = message.indexOf("|", start + 1)
      console.log(start, end)
      return message.substring(0, start + 1) + registry.receivingFacility + message.substring(end)
    }
    return message;
  }

}
