import { HttpResponse } from '@angular/common/http';
import { Component, EventEmitter, Inject, Input, Optional, Output } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { EhrGroup, EhrGroupCharacteristic } from 'src/app/core/_model/rest';
import FormType, { FormCard, FormCardGeneric, GenericForm } from 'src/app/core/_model/structure';
import { GroupService } from 'src/app/core/_services/group.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';

@Component({
  selector: 'app-group-form',
  templateUrl: './group-form.component.html',
  styleUrls: ['./group-form.component.css']
})
export class GroupFormComponent {

  @Input()
  ehrGroup: EhrGroup = { id: -1 };
  @Output()
  ehrGroupChange = new EventEmitter<EhrGroup>();

  formCards: FormCardGeneric<EhrGroup>[] = [
    {
      title: 'Name', cols: 1, rows: 1, forms: [
        { type: FormType.text, title: 'name', attribute: 'name' },
        { type: FormType.text, title: 'description', attribute: 'description' },
        { type: FormType.text, title: 'type', attribute: 'type' },
        { type: FormType.text, title: 'code', attribute: 'code' },
        // {type: FormType.text, title: 'authority', attribute: ''},
      ]
    }
  ]

  characteristicForms: GenericForm<EhrGroupCharacteristic>[] = [
    { type: FormType.text, title: 'Code System', attribute: 'codeSystem' },
    { type: FormType.text, title: 'Code Value', attribute: 'codeValue' },
    { type: FormType.boolean, title: 'exclude', attribute: 'exclude' },
    { type: FormType.text, title: 'Value', attribute: 'value' },
    // {type: FormType.text, title: 'authority', attribute: ''},
  ]

  isEditionMode: boolean = false;

  constructor(
    private groupService: GroupService,
    private snackBarService: SnackBarService,
    @Optional() public _dialogRef: MatDialogRef<GroupFormComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { ehrGroup: EhrGroup }) {
    if (data && data.ehrGroup) {
      this.ehrGroup = data.ehrGroup;
      this.isEditionMode = true
    }
  }

  fillRandom(): void {
    this.groupService.getRandom().subscribe(res => this.ehrGroup = res)
    // this.patientService.readRandom().subscribe((res) => this.patient = res)
  }

  addChar() {
    if (!this.ehrGroup.ehrGroupCharacteristics) {
      this.ehrGroup.ehrGroupCharacteristics = []
    }
    this.ehrGroup.ehrGroupCharacteristics.push({})
  }

  save(): void {
    if (this.isEditionMode) {
      this.groupService.putGroup(this.ehrGroup).subscribe({
        next: (res: EhrGroup) => {
          this.groupService.doRefresh()
          this._dialogRef?.close(true)
        },
        error: (err) => {
          console.log(err.error)
          this.snackBarService.errorMessage(err.error.error);
        }
      });
    } else {
      this.groupService.postGroup(this.ehrGroup).subscribe({
        next: (res: HttpResponse<string>) => {
          this.groupService.doRefresh()
          this._dialogRef?.close(true)
        },
        error: (err) => {
          console.log(err.error)
          this.snackBarService.errorMessage(err.error.error);
        }
      });
    }
  }

}
