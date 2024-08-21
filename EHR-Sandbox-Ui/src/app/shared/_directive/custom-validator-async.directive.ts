import { Directive, Input } from '@angular/core';
import { AbstractControl, ValidationErrors, AsyncValidatorFn, AsyncValidator, NG_ASYNC_VALIDATORS } from '@angular/forms';
import { firstValueFrom, Observable, of } from 'rxjs';

@Directive({
  selector: '[customValidatorAsync]',
  // standalone: true,
  providers: [{ provide: NG_ASYNC_VALIDATORS, useExisting: CustomValidatorAsyncDirective, multi: true }]
})
export class CustomValidatorAsyncDirective implements AsyncValidator {

  @Input()
  public customValidatorAsync?: AsyncValidatorFn;

  public validate(control: AbstractControl): Promise<ValidationErrors | null> | Observable<ValidationErrors | null> {
    if (this.customValidatorAsync) {
      return this.customValidatorAsync(control);
    } else {
      return firstValueFrom(of(null));
    }
  }
}
