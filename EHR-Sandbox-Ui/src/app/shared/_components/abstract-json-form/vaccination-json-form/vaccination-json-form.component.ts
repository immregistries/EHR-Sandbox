import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { JsonFormComponent } from '../abstract-json-form.component';
import { VaccinationEvent } from 'src/app/core/_model/rest';
import { MatInput } from '@angular/material/input';

@Component({
  selector: 'app-vaccination-json-form',
  templateUrl: '../abstract-json-form.component.html',
  styleUrls: ['../abstract-json-form.component.css']
})
export class VaccinationJsonFormComponent extends JsonFormComponent<VaccinationEvent> {

  @ViewChild('txtarea', { static: false })
  public txtArea!: HTMLTextAreaElement;
  @ViewChild('txtarea', { static: false })
  public matInput!: MatInput;

  @Input()
  override set model(value: string) {
    super.model = value

  }

  @Output()
  modelChange: EventEmitter<VaccinationEvent> = new EventEmitter()

  constructor() {
    super()
  }

  checkType(value: VaccinationEvent): void {

  }

}
