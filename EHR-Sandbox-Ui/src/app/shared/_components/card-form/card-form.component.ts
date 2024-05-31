import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { BaseForm, CodeReference, ComparisonResult } from 'src/app/core/_model/structure';

@Component({
  selector: 'app-card-form',
  templateUrl: './card-form.component.html',
  styleUrls: ['./card-form.component.css']
})
export class CardFormComponent implements OnInit {

  /**
   * solely for select-codebase components
   */
  @Input() referenceFilter!: BehaviorSubject<{ [key: string]: { reference: CodeReference, value: string } }>;
  @Output() referenceEmitter = new EventEmitter<{ reference: CodeReference, value: string }>();
  @Input() toolTipDisabled: boolean = false;

  @Input() lotNumberValid: boolean = true;

  @Input() form!: BaseForm
  @Input() overrideNoFieldsRequired: boolean = false

  private _model!: any;
  @Input()
  set model(value: any) {
    this._model = value
    this.modelChange.emit(this._model)
  }
  get model(): any {
    return this._model
  }
  @Output() modelChange = new EventEmitter<any>();

  @Input() compareTo?: string;

  referencesChange(emitted: { reference: CodeReference, value: string }): void {
    this.referenceEmitter.emit(emitted)
  }

  /**
   * Allows String type casting in HTML template
   * @param val
   * @returns String type value
   */
  asString(val: any): string { return val; }

  constructor() { }

  ngOnInit(): void {
  }

  isRequired(): 'true' | 'false' {
    if (this.overrideNoFieldsRequired) {
      return 'false'
    } else if (this.form.required) {
      return 'true'
    } else {
      return 'false'
    }
  }

}
