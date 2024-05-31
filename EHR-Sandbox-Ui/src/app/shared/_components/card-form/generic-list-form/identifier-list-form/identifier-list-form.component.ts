import { Component, EventEmitter, Input, Output } from '@angular/core';
import { title } from 'process';
import { EhrGroupCharacteristic, EhrIdentifier } from 'src/app/core/_model/rest';
import FormType, { GenericForm } from 'src/app/core/_model/structure';
import { GenericListFormComponent } from '../generic-list-form.component';

@Component({
  selector: 'app-identifier-list-form',
  templateUrl: '../generic-list-form.component.html',
  styleUrls: ['../generic-list-form.component.css']
})
export class IdentifierListFormComponent extends GenericListFormComponent<EhrIdentifier> {

  override readonly FORMS: GenericForm<EhrIdentifier>[] = [
    { type: FormType.text, title: 'Identifier Value', attribute: 'value' },
    { type: FormType.text, title: 'Identifier System', attribute: 'system' },
    { type: FormType.text, title: 'Identifier Type', attribute: 'type' },
  ]

}
