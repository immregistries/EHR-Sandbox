import { Component } from '@angular/core';
import { GenericListFormComponent } from '../generic-list-form.component';
import { EhrHumanName } from 'src/app/core/_model/rest';
import FormType, { GenericForm } from 'src/app/core/_model/form-structure';
import { TenantService } from 'src/app/core/_services/tenant.service';

@Component({
  selector: 'app-name-list-form',
  templateUrl: '../generic-list-form.component.html',
  styleUrls: ['../generic-list-form.component.css']
})
export class NameListFormComponent extends GenericListFormComponent<EhrHumanName> {
  constructor(private tenantService: TenantService) {
    super()
    this.FORMS = [
      { type: FormType.text, title: 'First Name', attributeName: 'nameFirst' },
      { type: FormType.text, title: 'Middle Name', attributeName: 'nameMiddle' },
      { type: FormType.text, title: 'Last Name', attributeName: 'nameLast' },
      { type: FormType.text, title: 'Prefix', attributeName: 'namePrefix' },
      { type: FormType.text, title: 'Suffix', attributeName: 'nameSuffix' },
    ]
    tenantService.getCurrentObservable().subscribe((tenant) => {
      if (tenant.nameDisplay?.includes("SINGLENAME")) {
        this.single_mode = true
        let index = this.FORMS.indexOf(this.NAME_TYPE_FORM)
        if (index > 0) {
          this.FORMS.splice(index, 1)
        }

      } else {
        this.FORMS.push(this.NAME_TYPE_FORM)
      }
    })
  }

  override readonly EMPTY_VALUE: string = '{}';
  override FORMS: GenericForm<EhrHumanName>[];

  readonly NAME_TYPE_FORM: GenericForm<EhrHumanName> = {
    type: FormType.code, title: 'Type', attributeName: 'nameType', codeMapLabel: "PERSON_NAME_TYPE", options: [
      { code: "NB", display: 'Newborn Name' },
      { code: "TEST", display: 'Test' }
    ]
  }
}
