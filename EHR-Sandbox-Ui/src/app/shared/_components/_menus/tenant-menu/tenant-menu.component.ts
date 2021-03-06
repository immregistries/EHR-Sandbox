import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Tenant } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { TenantCreationComponent } from '../../_dialogs/tenant-creation/tenant-creation.component';

@Component({
  selector: 'app-tenant-menu',
  templateUrl: './tenant-menu.component.html',
  styleUrls: ['./tenant-menu.component.css']
})
export class TenantMenuComponent implements OnInit {
  list!: Tenant[];

  constructor(
    public tenantService: TenantService,
    private facilityService: FacilityService,
    public dialog: MatDialog) { }

  ngOnInit(): void {
    this.tenantService.getRefresh().subscribe((bool) => {
      this.tenantService.readTenants().subscribe((res) => {
        this.list = res
      })
    })
  }

  openDialog() {
    const dialogRef = this.dialog.open(TenantCreationComponent);
    dialogRef.afterClosed().subscribe(result => {
      // this.ngOnInit();
    });
  }

  onSelection(event: Tenant) {
    if (this.tenantService.getTenantId() == event.id) { // unselect
      this.tenantService.setTenant({id: -1})
    } else {
      this.tenantService.setTenant(event)
    }
    this.facilityService.setFacility({id: -1})
  }

}
