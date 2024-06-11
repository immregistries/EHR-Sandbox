import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { BaseForm, ComparisonResult } from 'src/app/core/_model/structure';
import { Code, CodeReference, CodeReferenceTable, CodeReferenceTableMember } from "src/app/core/_model/code-base-map";
import { ValidatorFn, AbstractControl, ValidationErrors } from '@angular/forms';

@Component({
  selector: 'app-card-form',
  templateUrl: './card-form.component.html',
  styleUrls: ['./card-form.component.css']
})
export class CardFormComponent {

  /**
   * solely for select-codebase components
   */
  @Input() referenceFilter!: BehaviorSubject<CodeReferenceTable>;
  @Output() referenceEmitter = new EventEmitter<CodeReferenceTableMember>();
  @Input() toolTipDisabled: boolean = false;

  @Input() lotNumberValid: boolean = true;

  @Input() form!: BaseForm


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

  /**
   * Allows String type casting in HTML template
   * @param val
   * @returns String type value
   */
  asString(val: any): string { return val; }

  @Input() overrideNoFieldsRequired: boolean = false
  @Input() overrideAllFieldsRequired: boolean = false
  isRequired(): 'true' | 'false' {
    if (this.overrideNoFieldsRequired) {
      return 'false'
    } else if (this.overrideAllFieldsRequired) {
      return 'true'
    } else if (this.form.required) {
      return 'true'
    } else {
      return 'false'
    }
  }

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
