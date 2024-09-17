import { AfterViewInit, Component, ViewChild, ViewEncapsulation } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { EhrPatient } from '../../_model/rest';
import { MatDialog } from '@angular/material/dialog';
import { FacilityService } from '../../_services/facility.service';
import { PatientService } from '../../_services/patient.service';
import { TenantService } from '../../_services/tenant.service';
import { RemoteGroupService } from '../../_services/remote-group.service';
import { MatTabGroup } from '@angular/material/tabs';
import { Group } from 'fhir/r5';
import { filter } from 'rxjs';


/**
 * Deprecated
 */
@Component({
  selector: 'app-remote-group-dashboard',
  templateUrl: './remote-group-dashboard.component.html',
  styleUrls: ['./remote-group-dashboard.component.css'],
  // encapsulation: ViewEncapsulation.None,
})
export class RemoteGroupDashboardComponent implements AfterViewInit {
  public patientDatasource = new MatTableDataSource<EhrPatient>([]);

  private _group?: Group | undefined;
  public get group(): Group | undefined {

    return this._group;
  }
  public set group(value: Group | undefined) {
    this.patientService.quickReadPatients()
      .pipe(filter((patient) => {
        return true;
      })).subscribe((res) => this.patientDatasource.data = res)
    this._group = value;
    this.remoteGroupService.setCurrent(this.group ?? { resourceType: "Group", type: "person", membership: "definitional" })
  }

  constructor(private tenantService: TenantService,
    private facilityService: FacilityService,
    public patientService: PatientService,
    public remoteGroupService: RemoteGroupService,
    private dialog: MatDialog) { }

  @ViewChild('tabs', { static: false }) tabGroup!: MatTabGroup;


  sample!: string

  ngAfterViewInit(): void {
    this.tabGroup.selectedIndex = 1
    this.remoteGroupService.triggerFetch()
    this.remoteGroupService.readSample().subscribe(res => {
      this.sample = JSON.stringify(res, undefined, 4)
    })
  }

  rowHeight(): string {
    return (window.innerHeight / 2 - 60) + 'px'
  }

  triggerRefresh() {
    this.remoteGroupService.triggerFetch().subscribe(() => {
      this.facilityService.doRefresh()
    })
  }



}
