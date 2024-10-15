import { Component } from '@angular/core';
import { GenericListFormComponent } from '../generic-list-form.component';
import { EhrName } from 'src/app/core/_model/rest';
import FormType, { GenericForm } from 'src/app/core/_model/form-structure';

@Component({
  selector: 'app-race-list-form',
  templateUrl: '../generic-list-form.component.html',
  styleUrls: ['../generic-list-form.component.css']
})
export class NameListFormComponent extends GenericListFormComponent<EhrName> {
  override readonly EMPTY_VALUE: string = '{"value":""}';
  override readonly FORMS: GenericForm<EhrName>[] = [
    { type: FormType.code, title: 'Name', attributeName: 'name', codeMapLabel: "PATIENT_RACE" },
  ]
}
