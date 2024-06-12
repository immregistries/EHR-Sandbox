import { Component, OnInit } from '@angular/core';
import { EhrIdentifier } from 'src/app/core/_model/rest';
import FormType, { BaseFormOptionCodeSystemConcept, GenericForm } from 'src/app/core/_model/structure';
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
      { type: FormType.text, title: 'Identifier Value', attribute: 'value' },
      { type: FormType.text, title: 'Identifier System', attribute: 'system' },
      {
        type: FormType.code, title: 'Identifier Type', attribute: 'type', options: this.identifierOptions
      },
    ]
  }

  override FORMS: GenericForm<EhrIdentifier>[] = []

}
