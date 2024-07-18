import { EventEmitter, Injectable } from '@angular/core';
import { BaseForm } from 'src/app/core/_model/structure';

@Injectable()
export abstract class AbstractBaseFormComponent {

  abstract baseForm: BaseForm
  abstract set model(value: any);
  abstract get model();
  abstract modelChange: EventEmitter<any>;

  // referencesChange(emitted: CodeReferenceTableMember): void {
  //   this.referenceEmitter.emit(emitted)
  // }

  /**
   * Allows String type casting in HTML template
   * @param val
   * @returns String type value
   */
  asString(val: any): string { return val; }

  abstract overrideNoFieldsRequired: boolean;
  abstract overrideAllFieldsRequired: boolean;
  isRequired(): 'true' | 'false' {
    if (this.overrideNoFieldsRequired) {
      return 'false'
    } else if (this.overrideAllFieldsRequired) {
      return 'true'
    } else if (this.baseForm.required) {
      return 'true'
    } else {
      return 'false'
    }
  }


}
