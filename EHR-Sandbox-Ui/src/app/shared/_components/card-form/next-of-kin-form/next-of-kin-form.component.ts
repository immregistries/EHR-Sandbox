import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NextOfKin } from 'src/app/core/_model/rest';
import FormType, { BaseForm, GenericForm } from 'src/app/core/_model/structure';
import { AbstractBaseFormComponent } from '../abstract-base-form/abstract-base-form.component';

@Component({
  selector: 'app-next-of-kin-form',
  templateUrl: './next-of-kin-form.component.html',
  styleUrls: ['./next-of-kin-form.component.css']
})
export class NextOfKinFormComponent extends AbstractBaseFormComponent {
  @Input()
  baseForm!: BaseForm;
  @Input()
  model!: NextOfKin;
  @Output()
  modelChange: EventEmitter<NextOfKin> = new EventEmitter<NextOfKin>()

  readonly FORMS: GenericForm<NextOfKin>[] = [
    { type: FormType.text, title: 'First name', attribute: 'nameFirst' },
    { type: FormType.text, title: 'Middle name', attribute: 'nameMiddle' },
    { type: FormType.text, title: 'Last name', attribute: 'nameLast' },
    { type: FormType.text, title: 'Suffix', attribute: 'nameSuffix' },
    { type: FormType.date, title: 'Birth Date', attribute: 'birthDate' },
    { type: FormType.text, title: 'Email', attribute: 'email' },
    { type: FormType.code, title: 'Sex', attribute: 'sex', codeMapLabel: "PATIENT_SEX" },
    { type: FormType.phoneNumbers, title: 'Phone', attribute: 'phones' },
    { type: FormType.addresses, title: 'Address', attribute: 'addresses' },
    // Maiden name ---------- TODO
  ]

  @Input() overrideNoFieldsRequired: boolean = false
  @Input() overrideAllFieldsRequired: boolean = false

}
