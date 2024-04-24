import { Component, Inject, Input, Optional } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialog } from '@angular/material/dialog';
import { EhrGroup, EhrPatient } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { GroupService } from 'src/app/core/_services/group.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { ImmunizationRegistryFormComponent } from '../../_immunization-registry/immunization-registry-form/immunization-registry-form.component';

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

  openImmunizationRegistry() {
    // console.log(this.ehrGroup.immunizationRegistry)
    const dialogRef = this.dialog.open(ImmunizationRegistryFormComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: this.ehrGroup.immunizationRegistry,
    });
    dialogRef.afterClosed().subscribe(result => {
    });
  }



}
