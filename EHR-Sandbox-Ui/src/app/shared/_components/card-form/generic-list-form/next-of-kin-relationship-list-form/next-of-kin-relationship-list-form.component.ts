import { Component } from '@angular/core';
import { GenericListFormComponent } from '../generic-list-form.component';
import { NextOfKinRelationship } from 'src/app/core/_model/rest';
import FormType, { GenericForm } from 'src/app/core/_model/structure';

@Component({
  selector: 'app-next-of-kin-relationship-list-form',
  templateUrl: '../generic-list-form.component.html',
  styleUrls: ['../generic-list-form.component.css']
})
export class NextOfKinRelationshipListFormComponent extends GenericListFormComponent<NextOfKinRelationship> {

  override readonly FORMS: GenericForm<NextOfKinRelationship>[] = [
    { type: FormType.code, title: 'Relationship', attribute: 'relationshipKind', codeMapLabel: "PERSON_RELATIONSHIP" },
    { type: FormType.nextOfKin, title: 'Next of kin', attribute: 'nextOfKin' },
  ]

}
