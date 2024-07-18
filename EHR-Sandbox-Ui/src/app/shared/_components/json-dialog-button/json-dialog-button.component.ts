import { Component, Input } from '@angular/core';
import { JsonDialogComponent } from '../json-dialog/json-dialog.component';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-json-dialog-button',
  templateUrl: './json-dialog-button.component.html',
  styleUrls: ['./json-dialog-button.component.css']
})
export class JsonDialogButtonComponent {
  @Input()
  label: String = 'JSON'
  @Input()
  disabled: boolean = false
  @Input()
  content: {} | null | undefined;

  @Input()
  tooltip = 'Visualize or export in JSON';

  @Input()
  showIcon: boolean = true;
  @Input()
  color: "primary" | "accent" | "warn" | "" = "primary"

  constructor(private dialog: MatDialog) { }

  open(content: any) {
    this.dialog.open(JsonDialogComponent, {
      maxWidth: '95vw',
      maxHeight: '98vh',
      height: 'fit-content',
      width: '100%',
      // panelClass: 'dialog-without-bar',
      data: content,
    })
  }


}
