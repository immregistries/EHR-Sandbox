import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';


const DEFAULT_SETTINGS = {}
@Component({
  selector: 'app-json-dialog',
  templateUrl: './json-dialog.component.html',
  styleUrls: ['./json-dialog.component.css']
})
export class JsonDialogComponent {


  constructor(
    public _dialogRef: MatDialogRef<JsonDialogComponent>,
   @Inject(MAT_DIALOG_DATA) public data: any) {
    console.log("dialog",data)

   }

}
