import { Component, Inject, Input, Optional } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Facility } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { TenantService } from 'src/app/core/_services/tenant.service';

@Component({
  selector: 'app-facility-dashboard',
  templateUrl: './facility-dashboard.component.html',
  styleUrls: ['./facility-dashboard.component.css']
})
export class FacilityDashboardComponent {
  _facility!: Facility;
  @Input()
  set facility(value: Facility) {
    this._facility = value
    if (!this.facility.facilities) {
      this.children = []
      this.facilityService.readFacilityChildren(this.tenantService.getCurrentId(), this.facility.id).subscribe((res) => {
        this._facility.facilities = res
        this.children = res
      });
    }
  }
  get facility(): Facility {
    return this._facility
  }

  children: Facility[] = [];

  constructor(public tenantService: TenantService,
    public facilityService: FacilityService,
    public dialog: MatDialog,
    @Optional() public _dialogRef: MatDialogRef<FacilityDashboardComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { facility?: Facility | number}) {
    if (data?.facility) {
      if (typeof data.facility === "number" || typeof data.facility === "string") {
        this.facilityService.readFacility(tenantService.getCurrentId(), +data.facility).subscribe((res) => {
          this.facility = res
        });
      } else {
        this.facility = data.facility
      }
    } else {
      this.facilityService.getCurrentObservable().subscribe((res) => {
        this.facility = res
      })
    }
  }

  openFacility(element?: Facility) {
    if (this.facility) {
      this.dialog.open(FacilityDashboardComponent, {
        maxWidth: '95vw',
        maxHeight: '95vh',
        height: 'fit-content',
        width: '100%',
        panelClass: 'dialog-with-bar',
        data: {facility: element}
      })
    }
  }


}
