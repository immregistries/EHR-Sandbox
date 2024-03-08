import { Component, Input } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { EhrPatient } from 'src/app/core/_model/rest';
import { GroupService } from 'src/app/core/_services/group.service';
import { GroupDashboardComponent } from '../../_group/group-dashboard/group-dashboard.component';

@Component({
  selector: 'app-patient-group-list',
  templateUrl: './patient-group-list.component.html',
  styleUrls: ['./patient-group-list.component.css']
})
export class PatientGroupListComponent {
  @Input()
  patient!: EhrPatient;

  constructor(groupService: GroupService, private dialog: MatDialog,) {

  }

  open(groupName: String) {
    this.dialog.open(GroupDashboardComponent,{
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: {groupName: groupName}
    })

  }
}
