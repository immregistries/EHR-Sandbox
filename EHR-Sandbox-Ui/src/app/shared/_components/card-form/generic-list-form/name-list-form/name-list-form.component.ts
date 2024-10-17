import { Component } from '@angular/core';
import { GenericListFormComponent } from '../generic-list-form.component';
import { EhrHumanName } from 'src/app/core/_model/rest';
import FormType, { GenericForm } from 'src/app/core/_model/form-structure';

@Component({
  selector: 'app-name-list-form',
  templateUrl: '../generic-list-form.component.html',
  styleUrls: ['../generic-list-form.component.css']
})
export class NameListFormComponent extends GenericListFormComponent<EhrHumanName> {
  override readonly EMPTY_VALUE: string = '{}';
  override readonly FORMS: GenericForm<EhrHumanName>[] = [
    { type: FormType.text, title: 'First Name', attributeName: 'nameFirst' },
    { type: FormType.text, title: 'Middle Name', attributeName: 'nameMiddle' },
    { type: FormType.text, title: 'Last Name', attributeName: 'nameLast' },
    { type: FormType.text, title: 'Prefix', attributeName: 'namePrefix' },
    { type: FormType.text, title: 'Suffix', attributeName: 'nameSuffix' },
    { type: FormType.code, title: 'Type', attributeName: 'nameType', codeMapLabel: "PERSON_NAME_TYPE" },
  ]
}
