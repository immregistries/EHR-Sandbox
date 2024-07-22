import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NextOfKin } from 'src/app/core/_model/rest';
import FormType, { BaseForm, EhrFormGroupCard, GenericForm, EhrFormControl, EhrFormArray } from 'src/app/core/_model/structure';
import { AbstractBaseFormComponent } from '../abstract-base-form/abstract-base-form.component';
import { FormControl, FormGroup } from '@angular/forms';

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
    { type: FormType.text, title: 'First name', attributeName: 'nameFirst' },
    { type: FormType.text, title: 'Middle name', attributeName: 'nameMiddle' },
    { type: FormType.text, title: 'Last name', attributeName: 'nameLast' },
    { type: FormType.text, title: 'Suffix', attributeName: 'nameSuffix' },
    { type: FormType.text, title: 'Maiden Name', attributeName: 'motherMaiden' },
    { type: FormType.date, title: 'Birth Date', attributeName: 'birthDate' },
    { type: FormType.text, title: 'Email', attributeName: 'email' },
    { type: FormType.code, title: 'Sex', attributeName: 'sex', codeMapLabel: "PATIENT_SEX" },
    { type: FormType.phoneNumbers, title: 'Phone', attributeName: 'phones' },
    { type: FormType.addresses, title: 'Address', attributeName: 'addresses' },
    // Maiden name ---------- TODO
  ]


  readonly FORMS_NEW: EhrFormArray<NextOfKin> = new EhrFormArray<NextOfKin>(this.FORMS)
  // readonly FORMS_NEW: EhrFormGroupCard<NextOfKin> = new EhrFormGroupCard<NextOfKin>(this.FORMS)
  //      ,
  // 'nameMiddle': { type: FormType.text, title: 'Middle name', attributeName: 'nameMiddle' },
  // { type: FormType.text, title: 'Last name', attributeName: 'nameLast' },
  // { type: FormType.text, title: 'Suffix', attributeName: 'nameSuffix' },
  // { type: FormType.text, title: 'Maiden Name', attributeName: 'motherMaiden' },
  // { type: FormType.date, title: 'Birth Date', attributeName: 'birthDate' },
  // { type: FormType.text, title: 'Email', attributeName: 'email' },
  // { type: FormType.code, title: 'Sex', attributeName: 'sex', codeMapLabel: "PATIENT_SEX" },
  // { type: FormType.phoneNumbers, title: 'Phone', attributeName: 'phones' },
  // { type: FormType.addresses, title: 'Address', attributeName: 'addresses' },
  // }
  // }

  @Input() overrideNoFieldsRequired: boolean = false
  @Input() overrideAllFieldsRequired: boolean = false

}
