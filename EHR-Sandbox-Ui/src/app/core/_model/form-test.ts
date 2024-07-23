import { FormGroup, FormControl, FormArray, ɵFormControlCtor } from "@angular/forms";
import { FormCard, FormCardGeneric, GenericForm } from "./form-structure";

export class EhrFormGroupCard<X> extends FormGroup {
  formCard?: FormCard;
  constructor(formCard?: FormCardGeneric<X>, array?: EhrFormControl<X>[]) {
    let controls: { [key in Extract<keyof X, string>]?: EhrFormControl<X> } = {}
    formCard?.forms?.forEach(element => {
      controls[element.attributeName] = new EhrFormControl<X>(element)
    });
    // array?.forEach(element => {
    //   controls[element.attributeName] = new EhrFormControl<X>(element)
    // });
    super(controls)
    this.formCard = formCard
  }
}

export class EhrFormControl<X> extends FormControl {
  genericForm: GenericForm<X>
  constructor(genericForm: GenericForm<X>) {
    super()
    this.genericForm = genericForm
  }
}

export class EhrFormArray<X> extends FormArray {
  // genericForm: GenericForm<X>
  constructor(genericForm: GenericForm<X>[]) {
    // let controlsArray: EhrFormControl<X>[] = []
    super(genericForm)
    // this.genericForm = genericForm
  }
}

// export declare const EhrFormControl<X>: ɵEhrFormControlCtor<X>;

export declare interface ɵEhrFormControlCtor<X> extends ɵFormControlCtor {

}
