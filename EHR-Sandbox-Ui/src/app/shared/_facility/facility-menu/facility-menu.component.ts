import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { FacilityFormComponent } from '../facility-form/facility-form.component';
import { Facility } from 'src/app/core/_model/rest';
import { MatDialog } from '@angular/material/dialog';
import { FacilityDashboardComponent } from '../facility-dashboard/facility-dashboard.component';

@Component({
  selector: 'app-facility-menu',
  templateUrl: './facility-menu.component.html',
  styleUrls: ['./facility-menu.component.css']
})
export class FacilityMenuComponent implements OnInit {

  list?: Facility[];

  @Output()
  selectEmitter: EventEmitter<Facility> = new EventEmitter<Facility>()

  disabled() {
    return this.tenantService.getCurrentId() <= 0;
  }

  constructor(public facilityService: FacilityService,
    public tenantService: TenantService,
    public dialog: MatDialog) { }

  ngOnInit(): void {
    this.facilityService.getRefresh().subscribe((bool) => {
      this.tenantService.getCurrentObservable().subscribe(tenant => {
        this.facilityService.readFacilities(tenant.id).subscribe((res) => {
          this.list = res
        })
      })
    });
  }

  openDialog() {
    const dialogRef = this.dialog.open(FacilityFormComponent, {
      maxWidth: '48vw',
      maxHeight: '98vh',
      minWidth: '33vw',
      height: 'fit-content',
      width: 'fit-content',
      panelClass: 'dialog-without-bar',
    });
    dialogRef.afterClosed().subscribe(result => {
      this.facilityService.doRefresh()
      if (result) {
        this.facilityService.setCurrent(result)
        this.selectEmitter.emit(this.facilityService.getCurrent())
      }
    });
  }

  onSelection(event: Facility) {
    if (this.facilityService.getCurrentId() == event.id) { // unselect
      this.facilityService.setCurrent({ id: -1 })
    } else {
      this.facilityService.setCurrent(event)
      this.selectEmitter.emit(this.facilityService.getCurrent())
    }
  }

  selectFirstOrCreate() {
    if (!this.disabled() && this.facilityService.getCurrentId() < 0) {
      if (this.list && this.list[0]) {
        this.onSelection(this.list[0])
      } else {
        this.openDialog()
      }
    }
    event?.stopPropagation()
  }

  openFacility() {
    this.dialog.open(FacilityDashboardComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: { facility: this.facilityService.getCurrent() }
    })

  }

}
