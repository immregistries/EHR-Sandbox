import { AfterViewInit, Component, OnInit } from '@angular/core';
import { Tenant } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { TenantCreationComponent } from '../../_dialogs/tenant-creation/tenant-creation.component';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-tenant-menu',
  templateUrl: './tenant-menu.component.html',
  styleUrls: ['./tenant-menu.component.css']
})
export class TenantMenuComponent implements AfterViewInit {
  list!: Tenant[];

  constructor(
    public tenantService: TenantService,
    private facilityService: FacilityService,
    public dialog: MatDialog) { }

  ngAfterViewInit(): void {
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
    if (this.tenantService.getCurrentId() == event.id) { // unselect
      this.tenantService.setCurrent({id: -1})
    } else {
      this.tenantService.setCurrent(event)
    }
    this.facilityService.setCurrent({id: -1})
  }

  selectFirstOrCreate() {
    if (this.tenantService.getCurrentId() < 0) {
      if (this.list && this.list[0]) {
        this.onSelection(this.list[0])
      } else {
        this.openDialog()
      }
    }
    event?.stopPropagation()
  }

}
