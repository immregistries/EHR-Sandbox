import { Component } from '@angular/core';
import { EhrAddress, EhrRace } from 'src/app/core/_model/rest';
import FormType, { GenericForm } from 'src/app/core/_model/form-structure';
import { GenericListFormComponent } from '../generic-list-form.component';

@Component({
  selector: 'app-address-list-form',
  templateUrl: '../generic-list-form.component.html',
  styleUrls: ['../generic-list-form.component.css']
})
export class AddressListFormComponent extends GenericListFormComponent<EhrAddress> {
  override readonly EMPTY_VALUE: string = '{"value":""}';
  override readonly FORMS: GenericForm<EhrAddress>[] = [
    { type: FormType.text, title: 'Line 1', attributeName: 'addressLine1' },
    { type: FormType.text, title: 'Line 2', attributeName: 'addressLine2' },
    { type: FormType.text, title: 'City', attributeName: 'addressCity' },
    { type: FormType.text, title: 'State', attributeName: 'addressState' },

    { type: FormType.text, title: 'Zip code', attributeName: 'addressZip' },
    { type: FormType.text, title: 'County/Parish', attributeName: 'addressCountyParish' },
    { type: FormType.text, title: 'Country', attributeName: 'addressCountry' },
  ]
}
