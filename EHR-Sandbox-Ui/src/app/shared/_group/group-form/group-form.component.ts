import { HttpResponse } from '@angular/common/http';
import { Component, EventEmitter, Inject, Input, Optional, Output } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { EhrGroup, EhrGroupCharacteristic, ImmunizationRegistry } from 'src/app/core/_model/rest';
import FormType, { FormCard, FormCardGeneric, GenericForm } from 'src/app/core/_model/form-structure';
import { GroupService } from 'src/app/core/_services/group.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';

@Component({
  selector: 'app-group-form',
  templateUrl: './group-form.component.html',
  styleUrls: ['./group-form.component.css']
})
export class GroupFormComponent {
  readonly formCards: FormCardGeneric<EhrGroup>[] = [
    {
      title: 'Name', cols: 1, rows: 1, forms: [
        { type: FormType.text, title: 'name', attributeName: 'name' },
        { type: FormType.text, title: 'description', attributeName: 'description' },
        { type: FormType.text, title: 'type', attributeName: 'type' },
        { type: FormType.text, title: 'code', attributeName: 'code' },
        // {type: FormType.text, title: 'authority', attributeName: ''},
      ]
    }
  ]

  readonly characteristicForms: GenericForm<EhrGroupCharacteristic>[] = [
    { type: FormType.text, title: 'Kind System', attributeName: 'codeSystem' },
    { type: FormType.text, title: 'Kind Code', attributeName: 'codeValue' },
    { type: FormType.boolean, title: 'exclude', attributeName: 'exclude' },
    { type: FormType.text, title: 'Value', attributeName: 'value' },
    // {type: FormType.text, title: 'authority', attributeName: ''},
  ]

  @Input()
  ehrGroup: EhrGroup = { id: -1 };
  @Output()
  ehrGroupChange = new EventEmitter<EhrGroup>();
  @Output()
  savedEmitter = new EventEmitter<EhrGroup | number | string>();

  isEditionMode: boolean = false;
  @Input()
  set defaultRegistry(value: ImmunizationRegistry) {
    this.ehrGroup.immunizationRegistry = value
  }

  constructor(
    private groupService: GroupService,
    private snackBarService: SnackBarService,
    @Optional() public _dialogRef?: MatDialogRef<GroupFormComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data?: { ehrGroup: EhrGroup }) {
    if (data && data.ehrGroup) {
      this.ehrGroup = data.ehrGroup;
      this.isEditionMode = true
    }
  }

  fillRandom(): void {
    let registry = this.ehrGroup.immunizationRegistry
    this.groupService.getRandom().subscribe(res => {
      this.ehrGroup = res
      this.ehrGroup.immunizationRegistry = registry
    })
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
          this.close(res)
        },
        error: (err) => {
          console.log(err.error)
          this.snackBarService.errorMessage(err.error.error ?? err.error);
        }
      });
    } else {
      this.groupService.postGroup(this.ehrGroup).subscribe({
        next: (res: HttpResponse<string>) => {
          this.close(res.body ?? -1)
        },
        error: (err) => {
          console.log(err.error)
          this.snackBarService.errorMessage(err.error.error ?? err.error);
        }
      });
    }
  }

  close(res: EhrGroup | string | number) {
    this.savedEmitter.emit(res)
    this.groupService.doRefresh()
    if (this._dialogRef?.close) {
      this._dialogRef.close(res)
    }
  }


}
