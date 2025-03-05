import { AfterViewInit, Component, Inject, Input, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { Hl7Service } from 'src/app/core/_services/_fhir/hl7.service';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatTabGroup } from '@angular/material/tabs';

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
    private hl7Service: Hl7Service,
    @Inject(MAT_DIALOG_DATA) public data: { patientId: number, vaccinationId: number }) {
    if (data) {
      this.patientId = data.patientId ?? -1
      this.vaccinationId = data.vaccinationId ?? -1
    }
  }

  ngOnInit(): void {
    if (this.vaccinationId > 0) {
      this.vxuLoading = true
      this.hl7Service.getVXU(this.patientId, this.vaccinationId).subscribe((res) => {
        this.vxu = res
        this.vxuLoading = false
      })
    } else if (this.patientId > 0) {
      this.qbpLoading = true
      this.hl7Service.getQBP(this.patientId).subscribe((res) => {
        this.qbp = res
        this.qbpLoading = false
      })

      this.vxuLoading = true
      this.hl7Service.getVXUAll(this.patientId).subscribe((res) => {
        this.vxu = res
        this.vxuLoading = false
      })
    }
  }

  ngAfterViewInit(): void {
    this.tabGroup.selectedIndex = 1;
  }

}
