import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Facility, Tenant } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { FacilityCreationComponent } from '../../_dialogs/facility-creation/facility-creation.component';

@Component({
  selector: 'app-facility-menu',
  templateUrl: './facility-menu.component.html',
  styleUrls: ['./facility-menu.component.css']
})
export class FacilityMenuComponent implements OnInit {

  list?: Facility[];

  constructor(public facilityService: FacilityService,
    public tenantService: TenantService,
    public dialog: MatDialog) { }

  ngOnInit(): void {
    this.tenantService.getObservableTenant().subscribe(tenant => {
      this.facilityService.getRefresh().subscribe((bool) => {
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

}
