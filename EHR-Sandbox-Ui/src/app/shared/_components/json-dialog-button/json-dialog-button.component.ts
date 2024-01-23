import { Component, Input } from '@angular/core';
import { JsonDialogService } from 'src/app/core/_services/json-dialog.service';

@Component({
  selector: 'app-json-dialog-button',
  templateUrl: './json-dialog-button.component.html',
  styleUrls: ['./json-dialog-button.component.css']
})
export class JsonDialogButtonComponent {
  @Input()
  label: String = 'Json'
  @Input()
  disabled: boolean = false
  @Input()
  content: any = {}

  constructor ( public jsonDialog: JsonDialogService) {

  }

  action() {
    this.jsonDialog.open(this.content);
  }

}
