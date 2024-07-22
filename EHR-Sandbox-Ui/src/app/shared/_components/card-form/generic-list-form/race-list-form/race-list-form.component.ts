import { Component } from '@angular/core';
import { GenericListFormComponent } from '../generic-list-form.component';
import { EhrRace } from 'src/app/core/_model/rest';
import FormType, { GenericForm } from 'src/app/core/_model/structure';

@Component({
  selector: 'app-race-list-form',
  templateUrl: '../generic-list-form.component.html',
  styleUrls: ['../generic-list-form.component.css']
})
export class RaceListFormComponent extends GenericListFormComponent<EhrRace> {
  override readonly EMPTY_VALUE: string = '{"value":""}';
  override readonly FORMS: GenericForm<EhrRace>[] = [
    { type: FormType.code, title: 'Race', attributeName: 'value', codeMapLabel: "PATIENT_RACE" },
  ]
}
