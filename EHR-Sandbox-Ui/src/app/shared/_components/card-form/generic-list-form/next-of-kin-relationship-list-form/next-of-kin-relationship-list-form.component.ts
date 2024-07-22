import { Component, Input } from '@angular/core';
import { GenericListFormComponent } from '../generic-list-form.component';
import { NextOfKinRelationship } from 'src/app/core/_model/rest';
import FormType, { GenericForm } from 'src/app/core/_model/structure';

@Component({
  selector: 'app-next-of-kin-relationship-list-form',
  templateUrl: '../generic-list-form.component.html',
  styleUrls: ['../generic-list-form.component.css']
})
export class NextOfKinRelationshipListFormComponent extends GenericListFormComponent<NextOfKinRelationship> {


  /**
   * Override to not add one by default
   */
  // @Input()
  // override set itemList(value: (NextOfKinRelationship)[] | undefined) {
  //   this._itemList = value;
  // }

  override ngOnInit(): void {

  }

  override readonly FORMS: GenericForm<NextOfKinRelationship>[] = [
    { type: FormType.code, title: 'Relationship', attributeName: 'relationshipKind', codeMapLabel: "PERSON_RELATIONSHIP" },
    { type: FormType.nextOfKin, title: 'Next of kin', attributeName: 'nextOfKin' },
  ]

}
