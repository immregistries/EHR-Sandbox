import { Component, Input, OnInit } from '@angular/core';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { FacilityCreationComponent } from '../../../_dialogs/facility-creation/facility-creation.component';
import { Facility } from 'src/app/core/_model/rest';
import { MatDialog } from '@angular/material/dialog';

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
    this.tenantService.getCurrentObservable().subscribe(tenant => {
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
    if (this.facilityService.getCurrentId() == event.id) { // unselect
      this.facilityService.setCurrent({id: -1})
    } else {
      this.facilityService.setCurrent(event)
    }
  }

}