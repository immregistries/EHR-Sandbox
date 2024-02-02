import { Dialog } from "@angular/cdk/dialog";
import { Injectable } from "@angular/core";
import { JsonDialogComponent } from "src/app/shared/_components/json-dialog/json-dialog.component";

@Injectable({
  providedIn: 'root'
})
export class JsonDialogService {

  constructor(private dialog: Dialog) { }

  open(content: any) {
    console.log("service", content)
    this.dialog.open(JsonDialogComponent, {
      maxWidth: '95vw',
      maxHeight: '98vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-without-bar',
      data: content,
    })
  }


}
