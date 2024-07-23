import { Component, OnInit } from '@angular/core';
import { EhrIdentifier } from 'src/app/core/_model/rest';
import FormType, { BaseFormOptionCodeSystemConcept, GenericForm } from 'src/app/core/_model/form-structure';
import { GenericListFormComponent } from '../generic-list-form.component';
import { CodeMapsService } from 'src/app/core/_services/code-maps.service';

@Component({
  selector: 'app-identifier-list-form',
  templateUrl: '../generic-list-form.component.html',
  styleUrls: ['../generic-list-form.component.css']
})
export class IdentifierListFormComponent extends GenericListFormComponent<EhrIdentifier> implements OnInit {
  constructor(private codeMapsService: CodeMapsService) {
    super()
  }
  identifierOptions?: BaseFormOptionCodeSystemConcept[]

  override ngOnInit(): void {
    super.ngOnInit()
    this.identifierOptions = this.codeMapsService.identifierTypeCodeSystem.concept
    this.FORMS = [
      { type: FormType.text, title: 'Identifier Value', attributeName: 'value' },
      { type: FormType.text, title: 'Identifier System', attributeName: 'system' },
      { type: FormType.code, title: 'Identifier Type', attributeName: 'type', options: this.identifierOptions },
      { type: FormType.text, title: 'Assigner (reference)', attributeName: 'assignerReference' },
    ]
  }

  override FORMS: GenericForm<EhrIdentifier>[] = []

  override EMPTY_VALUE: string = JSON.stringify({ value: "", system: "", type: "" });

}
