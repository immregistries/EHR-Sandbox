import { Component, Inject, Input, Optional } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Facility, Feedback } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { FeedbackService } from 'src/app/core/_services/feedback.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { FacilityCachePipe } from '../../_pipes/facility-cache.pipe';

@Component({
  selector: 'app-facility-dashboard',
  templateUrl: './facility-dashboard.component.html',
  styleUrls: ['./facility-dashboard.component.css']
})
export class FacilityDashboardComponent {
  feedbacks?: Feedback[];

  _facility!: Facility;
  @Input()
  set facility(value: Facility) {
    this._facility = value
    this.feedbackService.readFacilityFeedback(value.id ?? -1).subscribe(res => this.feedbacks = res)
  }
  get facility(): Facility {
    return this._facility
  }

  constructor(public tenantService: TenantService,
    public facilityService: FacilityService,
    public facilityCachePipe: FacilityCachePipe,
    public feedbackService: FeedbackService,
    public dialog: MatDialog,
    @Optional() public _dialogRef: MatDialogRef<FacilityDashboardComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { facility?: Facility | number }) {
    if (data?.facility) {
      this.facility = (this.facilityCachePipe.transform([data.facility]) ?? [])[0]
    } else {
      this.facilityService.getCurrentObservable().subscribe((res) => {
        this.facility = res
      })
    }
  }

  openFacility(element?: Facility | number) {
    if (this.facility) {
      this.dialog.open(FacilityDashboardComponent, {
        maxWidth: '95vw',
        maxHeight: '95vh',
        height: 'fit-content',
        width: '100%',
        panelClass: 'dialog-with-bar',
        data: { facility: element }
      })
    }
  }


}
