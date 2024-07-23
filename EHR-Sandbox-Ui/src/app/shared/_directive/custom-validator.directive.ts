import { Directive, Input } from '@angular/core';
import { NG_VALIDATORS, ValidatorFn, Validator, AbstractControl, ValidationErrors } from '@angular/forms';

@Directive({
  selector: '[customValidator]',
  providers: [{ provide: NG_VALIDATORS, useExisting: CustomValidatorDirective, multi: true }]
})
export class CustomValidatorDirective implements Validator {

  @Input()
  public customValidator?: ValidatorFn;

  public validate(control: AbstractControl): ValidationErrors | null {
    if (this.customValidator) {
      return this.customValidator(control);
    } else {
      return null;
    }
  }

}
