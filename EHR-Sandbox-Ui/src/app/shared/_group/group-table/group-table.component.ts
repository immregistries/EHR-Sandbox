import { Component, Input } from '@angular/core';
import { AbstractDataTableComponent } from '../../_components/abstract-data-table/abstract-data-table.component';
import { EhrGroup, EhrPatient, Facility } from 'src/app/core/_model/rest';
import { GroupService } from 'src/app/core/_services/group.service';
import { MatDialog } from '@angular/material/dialog';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { GroupFormComponent } from '../group-form/group-form.component';
import { GroupDashboardComponent } from '../group-dashboard/group-dashboard.component';
import { merge } from 'rxjs';
import { trigger, state, style, transition, animate } from '@angular/animations';

@Component({
  selector: 'app-group-table',
  templateUrl: './group-table.component.html',
  styleUrls: ['./group-table.component.css']
})
export class GroupTableComponent extends AbstractDataTableComponent<EhrGroup> {
  @Input() title: string = 'Groups'

  @Input()
  facility!: Facility | null;
  @Input()
  columns: (keyof EhrGroup | "alerts" | "member-count")[] = [
    "name",
    "immunizationRegistry",
    "member-count",
    "facility",
  ]

  constructor(public tenantService: TenantService,
    public facilityService: FacilityService,
    public patientService: PatientService,
    public groupService: GroupService,
    private dialog: MatDialog) {
    super()
    this.observableRefresh = merge(facilityService.getRefresh(),
    this.facilityService.getCurrentObservable(),
     groupService.getRefresh())
    this.observableSource = this.groupService.quickReadGroups();
  }


  openCreation() {
    const dialogRef = this.dialog.open(GroupFormComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '90%',
      panelClass: 'dialog-with-bar',
      data: {},
    });
    dialogRef.afterClosed().subscribe(result => {
      this.ngAfterViewInit()
    });
  }

  openEhrGroupDashboard(ehrGroup: EhrGroup) {
    const dialogRef = this.dialog.open(GroupDashboardComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: { groupName: ehrGroup.name },
    });
    dialogRef.afterClosed().subscribe(result => {
      this.groupService.doRefresh()
    });
  }


}
