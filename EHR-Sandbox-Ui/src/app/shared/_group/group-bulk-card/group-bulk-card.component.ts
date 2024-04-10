import { Component, Input } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { EhrGroup } from 'src/app/core/_model/rest';
import { BulkImportStatus } from 'src/app/core/_model/structure';
import { GroupService } from 'src/app/core/_services/group.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { JsonDialogComponent } from '../../_components/json-dialog/json-dialog.component';

@Component({
  selector: 'app-group-bulk-card',
  templateUrl: './group-bulk-card.component.html',
  styleUrls: ['./group-bulk-card.component.css']
})
export class GroupBulkCardComponent {
  private _group?: EhrGroup | undefined | null;
  public get group(): EhrGroup | undefined | null {
    return this._group;
  }
  @Input()
  public set group(value: EhrGroup | undefined | null) {
    this._group = value;
    console.log("bulk",value)
    this.refreshStatus()
  }

  importStatus?: BulkImportStatus = {};

  constructor(private groupService: GroupService, private snackBarService: SnackBarService, private dialog: MatDialog) {

  }

  kickoff() {
    if (this.group?.id && this.group.immunizationRegistry) {
      this.groupService.groupBulkImportKickoff(this.group.id).subscribe((res) => {
        this.snackBarService.open("Bulk Import started")
        this.refreshStatus()
      })
    } else {
      this.snackBarService.errorMessage("Group not remotely registered")
      this.refreshStatus()
    }
  }

  refreshStatus() {
    if (this.group?.id && this.group.immunizationRegistry) {
      this.groupService.getGroupBulkImportStatus(this.group?.id ?? -1).subscribe((res) => {
        if (res) {
          this.importStatus = res
        } else {
          this.importStatus = {}
        }
      })
    } else {
      this.importStatus = {}
    }
  }

  openResult(){
    this.dialog.open(JsonDialogComponent, {maxWidth: '95vw',
    maxHeight: '98vh',
    height: 'fit-content',
    width: '100%',
    // panelClass: 'dialog-without-bar',
    data: this.importStatus?.result
  })
 }
}
