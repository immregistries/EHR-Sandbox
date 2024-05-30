import { Component, EventEmitter, Input, Output } from '@angular/core';
import { title } from 'process';
import { EhrGroupCharacteristic, EhrIdentifier } from 'src/app/core/_model/rest';
import FormType, { GenericForm } from 'src/app/core/_model/structure';

@Component({
  selector: 'app-identifier-list-form',
  templateUrl: './identifier-list-form.component.html',
  styleUrls: ['./identifier-list-form.component.css']
})
export class IdentifierListFormComponent {
  @Input()
  identifierList?: EhrIdentifier[]
  @Output()
  identifierListChange: EventEmitter<EhrIdentifier[]> = new EventEmitter<EhrIdentifier[]>()

  addIdentifier() {
    if (!this.identifierList) {
      this.identifierList = []
    }
    this.identifierList.push({})
    this.identifierListChange.emit(this.identifierList)
  }

  readonly IDENTIFIER_FORMS: GenericForm<EhrIdentifier>[] = [
    { type: FormType.text, title: 'System', attribute: 'system' },
    { type: FormType.text, title: 'Value', attribute: 'value' },
    // { type: FormType.text, title: 'Type', attribute: 'type' },
  ]


}
