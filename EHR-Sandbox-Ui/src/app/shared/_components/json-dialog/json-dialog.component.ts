import { Component, Inject, Input, Optional } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';


const DEFAULT_SETTINGS = {}
@Component({
  selector: 'app-json-dialog',
  templateUrl: './json-dialog.component.html',
  styleUrls: ['./json-dialog.component.css']
})
export class JsonDialogComponent {

  @Input()
  value: any

  constructor(
    public _dialogRef: MatDialogRef<JsonDialogComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: any) {
    if (data) {
      this.value = data
    }
  }

}
