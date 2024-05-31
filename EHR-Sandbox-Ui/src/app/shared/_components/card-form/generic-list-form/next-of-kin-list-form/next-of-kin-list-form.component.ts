import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NextOfKin } from 'src/app/core/_model/rest';
import FormType, { GenericForm } from 'src/app/core/_model/structure';
import { GenericListFormComponent } from '../generic-list-form.component';

@Component({
  selector: 'app-next-of-kin-list-form',
  templateUrl: '../generic-list-form.component.html',
  styleUrls: ['../generic-list-form.component.css']
})
export class NextOfKinListFormComponent extends GenericListFormComponent<NextOfKin> {

  override readonly FORMS: GenericForm<NextOfKin>[] = [
    { type: FormType.text, title: 'First name', attribute: 'nameFirst' },
    { type: FormType.text, title: 'Middle name', attribute: 'nameMiddle' },
    { type: FormType.text, title: 'Last name', attribute: 'nameLast' },
    { type: FormType.text, title: 'Suffix', attribute: 'nameSuffix' },
    { type: FormType.code, title: 'Relationship', attribute: 'relationship', codeMapLabel: "PERSON_RELATIONSHIP" },
    { type: FormType.phoneNumbers, title: 'Phone Number', attribute: 'phones' },
  ]

}
