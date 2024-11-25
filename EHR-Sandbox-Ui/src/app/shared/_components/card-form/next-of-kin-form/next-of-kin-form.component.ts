import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NextOfKin } from 'src/app/core/_model/rest';
import { AbstractBaseFormComponent } from '../abstract-base-form/abstract-base-form.component';
import FormType, { BaseForm, GenericForm } from 'src/app/core/_model/form-structure';
import { EhrFormArray } from 'src/app/core/_model/form-test';

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
    { type: FormType.text, title: 'First name', attributeName: 'nameFirst', segmentRef: "NK1-2.2" },
    { type: FormType.text, title: 'Middle name', attributeName: 'nameMiddle', segmentRef: "NK1-2.3" },
    { type: FormType.text, title: 'Last name', attributeName: 'nameLast', segmentRef: "NK1-2.1" },
    { type: FormType.text, title: 'Suffix', attributeName: 'nameSuffix', segmentRef: "NK1-2.4" },
    { type: FormType.text, title: 'Maiden Name', attributeName: 'motherMaiden', segmentRef: "NK1-2.2" },
    // { type: FormType.date, title: 'Birth Date', attributeName: 'birthDate' },
    { type: FormType.text, title: 'Email', attributeName: 'email', segmentRef: "NK1-5[2]" },
    // { type: FormType.code, title: 'Sex', attributeName: 'sex', codeMapLabel: "PATIENT_SEX" },
    { type: FormType.phoneNumbers, title: 'Phone', attributeName: 'phones', segmentRef: "NK1-5" },
    { type: FormType.addresses, title: 'Address', attributeName: 'addresses', segmentRef: "NK1-4" },
    // Maiden name ---------- TODO
  ]


  // readonly FORMS_NEW: EhrFormArray<NextOfKin> = new EhrFormArray<NextOfKin>(this.FORMS)
  // readonly FORMS_NEW: EhrFormGroupCard<NextOfKin> = new EhrFormGroupCard<NextOfKin>(this.FORMS)

  @Input() overrideNoFieldsRequired: boolean = false
  @Input() overrideAllFieldsRequired: boolean = false

}
