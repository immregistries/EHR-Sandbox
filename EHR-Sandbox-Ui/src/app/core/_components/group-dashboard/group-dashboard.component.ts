import { AfterViewInit, Component, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { EhrPatient } from '../../_model/rest';
import { MatDialog } from '@angular/material/dialog';
import { FacilityService } from '../../_services/facility.service';
import { PatientService } from '../../_services/patient.service';
import { TenantService } from '../../_services/tenant.service';
import { GroupService } from '../../_services/group.service';
import { MatTabGroup } from '@angular/material/tabs';
import { Group } from 'fhir/r5';

@Component({
  selector: 'app-group-dashboard',
  templateUrl: './group-dashboard.component.html',
  styleUrls: ['./group-dashboard.component.css']
})
export class GroupDashboardComponent implements AfterViewInit {
  public patientDatasource = new MatTableDataSource<EhrPatient>([]);

  public group?: Group | null;

  constructor(private tenantService: TenantService,
    private facilityService: FacilityService,
    public patientService: PatientService,
    public groupService: GroupService,
    private dialog: MatDialog) { }

    @ViewChild('tabs', {static: false}) tabGroup!: MatTabGroup;

    ngAfterViewInit(): void {
      this.tabGroup.selectedIndex = 1;
      this.groupService.triggerFetch()
    }

    rowHeight(): string {
      return (window.innerHeight/2 - 60) + 'px'
    }


}
