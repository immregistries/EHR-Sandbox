import { CodeSystemConcept } from "fhir/r5";
import { Facility, Vaccine } from "./rest";
import { FormArray, FormControl, FormGroup, ɵFormControlCtor } from "@angular/forms";

enum FormType {
  text = 'text',
  date = 'date',
  short = 'short',
  boolean = 'boolean',
  select = 'select',
  yesNo = 'yesNo',
  code = 'code',
  textarea = 'textarea',
  clinician = 'clinician',
  phoneNumbers = 'phoneNumbers',
  identifiers = 'identifiers',
  nextOfKin = 'nextOfKin',
  nextOfKinRelationships = 'nextOfKinRelationships',
  races = 'races',
  addresses = 'addresses',
}

export default FormType;

export class EhrFormGroupCard<X> extends FormGroup {
  formCard?: FormCard;
  // constructor(genericFormArray: GenericForm<X>[]) {
  //   let controls: { [key in Extract<keyof X, string>]?: EhrFormControl<X> } = {}
  //   genericFormArray.forEach(element => {
  //     controls[element.attributeName] = new EhrFormControl<X>(element)
  //   });
  //   super(controls)
  //   // this.formCard = formCard
  // }
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

export interface FormCardGeneric<X> extends FormCard {
  // export interface FormCardGeneric<X extends Record<(string | number),Record<(string | number), any> {
  forms?: GenericForm<X>[],  // form fields for each specific objects
}
export interface GenericForm<X> extends BaseForm {
  attributeName: Extract<keyof X, string>,
}

export interface GenericFormNoDisabled<X> extends BaseFormNoDisabled {
  attributeName: Extract<keyof X, string>,
}

export interface FormCard {
  title: string,
  cols?: number, // dimensions of the card not used anymore
  rows?: number,
  toolTips?: string,
  forms?: BaseForm[],
  vaccinationForms?: VaccinationForm[],
  vaccineForms?: VaccineForm[],
}



export interface BaseFormNoDisabled {
  type: FormType,
  title: string,
  attributeName: string,
  codeMapLabel?: string,
  options?: BaseFormOption[],
  required?: boolean,
  defaultListEmptyValue?: string, // Set default value when used in a list type form, '{}' string will use hard coded default, undefined will add no value
}


export interface BaseForm extends BaseFormNoDisabled {
  disabled?: boolean,
}
export interface BaseFormOption {
  code: string | boolean, display?: string, definition?: string,
}

export interface BaseFormOptionCodeSystemConcept extends BaseFormOption, CodeSystemConcept {
  code: string;
}

export interface VaccinationForm extends BaseForm {
  // attributeName: keyof VaccinationEvent,
  attributeName: "enteringClinician" | "orderingClinician" | "administeringClinician" | "primarySource"
}
export interface VaccineForm extends BaseForm {
  attributeName: keyof Vaccine,
}


export interface NotificationPrototype {
  facility: Facility | number,
  timestamp: string,
}


export interface ComparisonResult { [index: string]: ComparisonResult | any | null }
export interface BulkImportStatus {
  status?: string,
  lastAttemptCount?: number,
  lastAttemptTime?: number,
  result?: string,
}
