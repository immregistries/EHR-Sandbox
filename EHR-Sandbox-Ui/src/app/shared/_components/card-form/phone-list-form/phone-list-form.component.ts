import { Component, EventEmitter, Input, Output } from '@angular/core';
import { EhrPhoneNumber } from 'src/app/core/_model/rest';
import FormType, { GenericForm } from 'src/app/core/_model/structure';

@Component({
  selector: 'app-phone-list-form',
  templateUrl: './phone-list-form.component.html',
  styleUrls: ['./phone-list-form.component.css']
})
export class PhoneListFormComponent {
  @Input()
  phoneNumberList?: EhrPhoneNumber[]
  @Output()
  phoneNumberListChange: EventEmitter<EhrPhoneNumber[]> = new EventEmitter<EhrPhoneNumber[]>()

  addPhoneNumber() {
    if (!this.phoneNumberList) {
      this.phoneNumberList = []
    }
    this.phoneNumberList.push({})
    this.phoneNumberListChange.emit(this.phoneNumberList)
  }

  readonly PHONE_NUMBER_FORMS: GenericForm<EhrPhoneNumber>[] = [
    { type: FormType.text, title: 'Phone number', attribute: 'number' },
    {
      type: FormType.code, title: 'Phone label', attribute: 'type', options: [
        { value: 'HOME' },
        { value: 'MOBILE' },
        { value: 'WORK' },
        { value: 'TEMP' },
        { value: 'NULL' },
        { value: 'OLD' },
      ]
    },
  ]


}
