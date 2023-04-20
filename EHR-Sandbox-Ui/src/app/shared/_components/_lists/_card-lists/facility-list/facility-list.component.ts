import { Component, Input, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { FacilityCreationComponent } from '../../../_dialogs/facility-creation/facility-creation.component';
import { Facility } from 'src/app/core/_model/rest';

@Component({
  selector: 'app-facility-list',
  templateUrl: './facility-list.component.html',
  styleUrls: ['./facility-list.component.css']
})
export class FacilityListComponent implements OnInit {

  @Input() list?: Facility[];

  constructor(public facilityService: FacilityService,
    public tenantService: TenantService,
    public dialog: MatDialog) { }



  ngOnInit(): void {
    this.tenantService.getObservableTenant().subscribe(tenant => {
      this.facilityService.getRefresh().subscribe((bool) => {
        this.facilityService.readFacilities(tenant.id ?? -1).subscribe((res) => {
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
