import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { BaseForm, ComparisonResult } from 'src/app/core/_model/structure';
import { Code, CodeReference, CodeReferenceTable, CodeReferenceTableMember } from "src/app/core/_model/code-base-map";
import { ValidatorFn, AbstractControl, ValidationErrors } from '@angular/forms';
import { AbstractBaseFormComponent } from './abstract-base-form/abstract-base-form.component';

@Component({
  selector: 'app-card-form',
  templateUrl: './card-form.component.html',
  styleUrls: ['./card-form.component.css']
})
export class CardFormComponent extends AbstractBaseFormComponent {

  /**
   * solely for select-codebase components
   */
  @Input() referenceFilter!: BehaviorSubject<CodeReferenceTable>;
  @Output() referenceEmitter = new EventEmitter<CodeReferenceTableMember>();
  @Input() lotNumberValid: boolean = true;
  @Input() baseForm!: BaseForm

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
    this.referenceEmitter.emit(emitted)
  }

  @Input() overrideNoFieldsRequired: boolean = false
  @Input() overrideAllFieldsRequired: boolean = false


  // lotNumberValidator(): ValidatorFn {
  //   return (control: AbstractControl): ValidationErrors | null => {
  //     if (!(this.form.attribute === 'lotNumber')) {
  //       return null;
  //     }
  //     let scanned = false
  //     let regexFit = false
  //     Object.keys
  //     this.referenceFilter.getValue()
  //     for (const ref of this.referenceFilter.) {
  //       if (ref.codeset == "VACCINATION_LOT_NUMBER_PATTERN") {
  //         scanned = true
  //         if (!this.vaccination.vaccine.lotNumber || this.vaccination.vaccine.lotNumber.length == 0) {
  //           this.vaccination.vaccine.lotNumber = randexp(ref.value)
  //           regexFit = true
  //           break;
  //         } else if (new RegExp(ref.value).test(this.vaccination.vaccine.lotNumber)) {
  //           regexFit = true
  //           break;
  //         }
  //       }
  //     }
  //     if (scanned && !regexFit) {
  //       return
  //     }
  //     return null;
  //   };
  // }

}
