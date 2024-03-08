import { Component } from '@angular/core';
import { EhrPatient } from 'src/app/core/_model/rest';
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

    }

    removePatient(patient: EhrPatient) {
      if (this.groupService.getCurrent().id && patient.id) {
        this.groupService.removeMember(this.groupService.getCurrent().id ?? -1, patient.id).subscribe((result) => {
          this.groupService.setCurrent(result)
          this.facilityService.doRefresh()
        })
      }
    }
}
