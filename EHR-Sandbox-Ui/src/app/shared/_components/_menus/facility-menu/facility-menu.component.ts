import { Component, OnInit } from '@angular/core';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { FacilityCreationComponent } from '../../_dialogs/facility-creation/facility-creation.component';
import { Facility } from 'src/app/core/_model/rest';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-facility-menu',
  templateUrl: './facility-menu.component.html',
  styleUrls: ['./facility-menu.component.css']
})
export class FacilityMenuComponent implements OnInit {

  list?: Facility[];

  constructor(public facilityService: FacilityService,
    public tenantService: TenantService,
    public dialog: MatDialog) {}

  ngOnInit(): void {
    this.facilityService.getRefresh().subscribe((bool) => {
      this.tenantService.getObservableTenant().subscribe(tenant => {
        this.facilityService.readFacilities(tenant.id).subscribe((res) => {
          this.list = res
        })
      })
    });
  }

  openDialog() {
    const dialogRef = this.dialog.open(FacilityCreationComponent);
    dialogRef.afterClosed().subscribe(result => {
      // this.ngOnInit();
    });
  }

  onSelection(event: Facility) {
    if (this.facilityService.getFacilityId() == event.id) { // unselect
      this.facilityService.setFacility({id: -1})
    } else {
      this.facilityService.setFacility(event)
    }
  }

  selectFirstOrCreate() {
    if (this.facilityService.getFacilityId() < 0) {
      if (this.list && this.list[0] ) {
        this.onSelection(this.list[0])
      } else {
        this.openDialog()
      }
    }
    event?.stopPropagation()
  }

}
