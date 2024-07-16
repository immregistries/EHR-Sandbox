import { AfterViewInit, Component, EventEmitter, OnInit, Output } from '@angular/core';
import { Tenant } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { TenantFormComponent } from '../tenant-form/tenant-form.component';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-tenant-menu',
  templateUrl: './tenant-menu.component.html',
  styleUrls: ['./tenant-menu.component.css']
})
export class TenantMenuComponent implements AfterViewInit {
  list!: Tenant[];

  @Output()
  selectEmitter: EventEmitter<Tenant> = new EventEmitter<Tenant>()

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
    const dialogRef = this.dialog.open(TenantFormComponent, {
      maxWidth: '48vw',
      maxHeight: '98vh',
      minWidth: '33vw',
      height: 'fit-content',
      width: 'fit-content',
      panelClass: 'dialog-without-bar'
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.tenantService.setCurrent(result)
        this.facilityService.setCurrent({ id: -1 })
        this.tenantService.doRefresh()
        this.selectEmitter.emit(this.tenantService.getCurrent())
      }
    });
  }

  onSelection(event: Tenant) {
    if (this.tenantService.getCurrentId() == event.id) { // unselect
      this.tenantService.setCurrent({ id: -1 })
    } else {
      this.tenantService.setCurrent(event)
      this.selectEmitter.emit(this.tenantService.getCurrent())
    }
    this.facilityService.setCurrent({ id: -1 })
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
