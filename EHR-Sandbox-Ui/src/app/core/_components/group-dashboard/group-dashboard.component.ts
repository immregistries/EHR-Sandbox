import { Component } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { EhrPatient } from '../../_model/rest';
import { MatDialog } from '@angular/material/dialog';
import { FacilityService } from '../../_services/facility.service';
import { PatientService } from '../../_services/patient.service';
import { TenantService } from '../../_services/tenant.service';
import { GroupService } from '../../_services/group.service';

@Component({
  selector: 'app-group-dashboard',
  templateUrl: './group-dashboard.component.html',
  styleUrls: ['./group-dashboard.component.css']
})
export class GroupDashboardComponent {
  public patientDatasource = new MatTableDataSource<EhrPatient>([]);


  constructor(private tenantService: TenantService,
    private facilityService: FacilityService,
    public patientService: PatientService,
    private groupService: GroupService,
    private dialog: MatDialog) { }


}
