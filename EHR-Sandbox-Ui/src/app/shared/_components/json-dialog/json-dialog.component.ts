import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { JsonDialogService } from 'src/app/core/_services/json-dialog.service';

@Component({
  selector: 'app-json-dialog',
  templateUrl: './json-dialog.component.html',
  styleUrls: ['./json-dialog.component.css']
})
export class JsonDialogComponent {


  constructor(
    // public _dialogRef: MatDialogRef<JsonDialogComponent>,
   @Inject(MAT_DIALOG_DATA) public data: any,
   jsonDialogService: JsonDialogService) {
    console.log(data)

   }

}
