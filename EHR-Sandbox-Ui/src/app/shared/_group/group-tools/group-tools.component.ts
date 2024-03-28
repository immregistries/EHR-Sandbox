import { Component, Input } from '@angular/core';
import { EhrGroup } from 'src/app/core/_model/rest';
import { FhirMessagingComponent } from 'src/app/fhir/_components/fhir-messaging/fhir-messaging.component';
import { GroupFormComponent } from '../group-form/group-form.component';
import { MatDialog } from '@angular/material/dialog';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { GroupService } from 'src/app/core/_services/group.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { PatientListComponent } from '../../_patient/patient-list/patient-list.component';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { FhirResourceService } from 'src/app/fhir/_services/fhir-resource.service';

@Component({
  selector: 'app-group-tools',
  templateUrl: './group-tools.component.html',
  styleUrls: ['./group-tools.component.css']
})
export class GroupToolsComponent {
  @Input() ehrGroup!: EhrGroup

  constructor(public dialog: MatDialog,
    public facilityService: FacilityService,
    public patientService: PatientService,
    public groupService: GroupService,
    private fhirResourceService: FhirResourceService,
    private snackBarService: SnackBarService,
    ) {}

  openEdition() {
    const dialogRef = this.dialog.open(GroupFormComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: { ehrGroup: this.ehrGroup },
    });
    dialogRef.afterClosed().subscribe(result => {
    });
  }

  openFhir() {
    this.fhirResourceService.quickGetGroupResource(this.ehrGroup.id ?? -1).subscribe((res) => {
      const dialogRef = this.dialog.open(FhirMessagingComponent, {
        maxWidth: '95vw',
        maxHeight: '98vh',
        height: 'fit-content',
        width: '100%',
        panelClass: 'dialog-without-bar',
        data: {resource: res, resourceType: 'Group', resourceLocalId: this.ehrGroup.id},
      });
      dialogRef.afterClosed().subscribe((result) => {
        this.groupService.refreshGroup(this.ehrGroup.id).subscribe((res) => {
          this.ehrGroup = res
        })
      })
    })
  }

  triggerRefresh() {
    this.groupService.refreshGroup(this.ehrGroup.id).subscribe((result) => {
      this.ehrGroup = result
      this.facilityService.doRefresh()
    })
  }

  addMember() {
    const dialogRef = this.dialog.open(PatientListComponent, {
      maxWidth: '95vw',
      maxHeight: '98vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-without-bar',
      data: {},
    });
    dialogRef.afterClosed().subscribe(
      selectedPatientId => {
        if (selectedPatientId) {
          if (this.ehrGroup.id) {
            this.groupService.addMember(this.ehrGroup.id, selectedPatientId).subscribe({
              next: (res) => {
                this.ehrGroup = res
                this.groupService.doRefresh()
              },
              error: error => {
                this.snackBarService.errorMessage(JSON.stringify(error.error))
              }
            });
          } else {
            this.snackBarService.errorMessage("Group.id undefined")
          }
        }
      });

  }

}