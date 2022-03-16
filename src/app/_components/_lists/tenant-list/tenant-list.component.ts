import { Component, Input, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Tenant } from 'src/app/_model/rest';
import { FacilityService } from 'src/app/_services/facility.service';
import { TenantService } from 'src/app/_services/tenant.service';
import { TenantCreationComponent } from '../../_dialogs/tenant-creation/tenant-creation.component';

@Component({
  selector: 'app-tenant-list',
  templateUrl: './tenant-list.component.html',
  styleUrls: ['./tenant-list.component.css']
})
export class TenantListComponent implements OnInit {

  @Input() list?: Tenant[];

  constructor(public tenantService: TenantService,
    private facilityService: FacilityService,
    public dialog: MatDialog) { }

  ngOnInit(): void {
    this.tenantService.readTenants().subscribe((res) => {
      this.list = res
    })
  }

  openDialog() {
    const dialogRef = this.dialog.open(TenantCreationComponent);
    dialogRef.afterClosed().subscribe(result => {
      console.log(`Dialog result: ${result}`);
      this.ngOnInit();
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
