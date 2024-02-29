import { Component, Input } from '@angular/core';
import { BaseForm } from 'src/app/core/_model/structure';

@Component({
  selector: 'app-card-display',
  templateUrl: './card-display.component.html',
  styleUrls: ['./card-display.component.css']
})
export class CardDisplayComponent {
  @Input() form!: BaseForm
  @Input() model!: any

  isEmpty() {
    return JSON.stringify(this.model).length == 2
  }
}
