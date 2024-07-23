import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { BaseForm } from 'src/app/core/_model/form-structure';
import { CodeReferenceTable, CodeReferenceTableMember } from "src/app/core/_model/code-base-map";
import { AbstractBaseFormComponent } from './abstract-base-form/abstract-base-form.component';
import { EhrFormControl } from 'src/app/core/_model/form-test';
import { ValidatorFn, AbstractControl, ValidationErrors } from '@angular/forms';

@Component({
  selector: 'app-card-form',
  templateUrl: './card-form.component.html',
  styleUrls: ['./card-form.component.css']
})
export class CardFormComponent extends AbstractBaseFormComponent {

  /**
   * solely for select-codebase components
   */
  @Input() referenceFilterTableObservable?: BehaviorSubject<CodeReferenceTable>;
  @Output() referenceTableMemberEmitter = new EventEmitter<CodeReferenceTableMember>();
  @Input() baseForm!: BaseForm
  private _ehrFormControl?: EhrFormControl<any>;
  public get ehrFormControl(): EhrFormControl<any> | undefined {
    return this._ehrFormControl;
  }
  @Input()
  public set ehrFormControl(value: EhrFormControl<any> | undefined) {
    this._ehrFormControl = value;
    if (value) {
      this.baseForm = value.genericForm
    }
  }

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

  referencesChange(emitted: CodeReferenceTableMember): void {
    this.referenceTableMemberEmitter.emit(emitted)
  }

  @Input() overrideNoFieldsRequired: boolean = false
  @Input() overrideAllFieldsRequired: boolean = false

}
