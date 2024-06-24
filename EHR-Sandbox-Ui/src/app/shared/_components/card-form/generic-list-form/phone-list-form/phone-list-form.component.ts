import { Component, EventEmitter, Input, Output } from '@angular/core';
import { EhrPhoneNumber } from 'src/app/core/_model/rest';
import FormType, { GenericForm } from 'src/app/core/_model/structure';
import { GenericListFormComponent } from '../generic-list-form.component';

@Component({
  selector: 'app-phone-list-form',
  templateUrl: '../generic-list-form.component.html',
  styleUrls: ['../generic-list-form.component.css']
})
export class PhoneListFormComponent extends GenericListFormComponent<EhrPhoneNumber> {

  override readonly EMPTY_VALUE: string = '{"type":""}';

  constructor() {
    super()
  }

  override readonly FORMS: GenericForm<EhrPhoneNumber>[] = [
    { type: FormType.text, title: 'Phone number', attribute: 'number' },
    {
      type: FormType.code, title: 'Phone type', attribute: 'type', options: [
        { code: 'HOME' },
        { code: 'MOBILE' },
        { code: 'WORK' },
        { code: 'TEMP' },
        { code: 'NULL' },
        { code: 'OLD' },
      ]
    },
    {
      type: FormType.code, title: 'Phone type', attribute: 'use', options: [
        { code: 'HOME' },
        { code: 'MOBILE' },
        { code: 'WORK' },
        { code: 'TEMP' },
        { code: 'NULL' },
        { code: 'OLD' },
      ]
    },
  ]


}
