import { Component } from '@angular/core';
import { Observable, map, merge, switchMap } from 'rxjs';
import { EhrGroup, EhrPatient } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { GroupService } from 'src/app/core/_services/group.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { TenantService } from 'src/app/core/_services/tenant.service';

@Component({
  selector: 'app-group-all-dashboard',
  templateUrl: './group-all-dashboard.component.html',
  styleUrls: ['./group-all-dashboard.component.css']
})
export class GroupAllDashboardComponent {

  constructor(public tenantService: TenantService,
    public facilityService: FacilityService,
    public patientService: PatientService,
    public groupService: GroupService) {
    this.groupService.getRefresh().subscribe((res) => {
      this.groupService.refreshGroup(this.groupService.getCurrent().id).subscribe((res) => {
        this.groupService.setCurrent(res)
      })
    })
    this.facilityService.getCurrentObservable().subscribe((res) => {
      this.groupService.setCurrent({})
    })
  }

  removePatient(patient: EhrPatient) {
    if (this.groupService.getCurrent().id && patient.id) {
      this.groupService.removeMember(this.groupService.getCurrent().id ?? -1, patient.id).subscribe((result) => {
        this.groupService.setCurrent(result)
        this.facilityService.doRefresh()
      })
    }
  }


  /**
   * Necessary to get updated after add patients
   * @returns
   */
  // currentGroupAlwaysUpToDate(): Observable<EhrPatient[]> {

  // return merge(
  //   this.groupService.getRefresh(),
  //   this.groupService.getCurrentObservable()
  // ).pipe(map((res) => {

  //   return this.groupService.getCurrent().patientList ?? []
  // }))
  // this.groupService.getCurrentObservable()

  // }

}
