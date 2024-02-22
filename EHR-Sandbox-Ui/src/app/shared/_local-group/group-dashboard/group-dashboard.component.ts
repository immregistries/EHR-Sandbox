import { Component, Inject, Input, Optional } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialog } from '@angular/material/dialog';
import { EhrGroup, EhrPatient } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { GroupService } from 'src/app/core/_services/group.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { GroupFormComponent } from '../group-form/group-form.component';
import { FhirMessagingComponent } from 'src/app/fhir/_components/fhir-messaging/fhir-messaging.component';
import { merge } from 'rxjs';
import { PatientListComponent } from '../../_patient/patient-list/patient-list.component';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';

@Component({
  selector: 'app-group-dashboard',
  templateUrl: './group-dashboard.component.html',
  styleUrls: ['./group-dashboard.component.css']
})
export class GroupDashboardComponent {
  @Input()
  public ehrGroup!: EhrGroup;

  constructor(public facilityService: FacilityService,
    public patientService: PatientService,
    public groupService: GroupService,
    private dialog: MatDialog,
    private snackBarService: SnackBarService,
    @Optional() public _dialogRef: MatDialogRef<GroupDashboardComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { patient?: EhrPatient | number, groupName?: string, groupId?: string }) {
    if (data?.groupName) {
      this.groupService.getGroupFromName(data.groupName).subscribe((result) => {
        this.ehrGroup = result
      })
    }
  }


  removePatient(patient: EhrPatient) {
    if (this.ehrGroup.id && patient.id) {
      this.groupService.removeMember(this.ehrGroup.id, patient.id).subscribe((result) => {
        this.ehrGroup = result
        this.facilityService.doRefresh()
      })
    }
  }



}
