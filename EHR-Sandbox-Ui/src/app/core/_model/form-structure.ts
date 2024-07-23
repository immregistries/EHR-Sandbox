import { CodeSystemConcept } from "fhir/r5";
import { Facility, Vaccine } from "./rest";
import { ValidatorFn } from "@angular/forms";

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

export declare interface FormCardGeneric<X> extends FormCard {
  // export declare interface FormCardGeneric<X extends Record<(string | number),Record<(string | number), any> {
  forms?: GenericForm<X>[],  // form fields for each specific objects
}
export declare interface GenericForm<X> extends BaseForm {
  attributeName: Extract<keyof X, string>,
}

export declare interface GenericFormNoDisabled<X> extends BaseFormNoDisabled {
  attributeName: Extract<keyof X, string>,
}

export declare interface FormCard {
  title: string,
  cols?: number, // dimensions of the card not used anymore
  rows?: number,
  toolTips?: string,
  forms?: BaseForm[],
  vaccinationForms?: VaccinationForm[],
  vaccineForms?: VaccineForm[],
}



export declare interface BaseFormNoDisabled {
  type: FormType,
  title: string,
  attributeName: string,
  codeMapLabel?: string,
  options?: BaseFormOption[],
  required?: boolean,
  defaultListEmptyValue?: string, // Set default value when used in a list type form, '{}' string will use hard coded default, undefined will add no value
  customValidator?: ValidatorFn;
  hintProducer?: (value?: string) => string;
}


export declare interface BaseForm extends BaseFormNoDisabled {
  disabled?: boolean,
}
export declare interface BaseFormOption {
  code: string | boolean, display?: string, definition?: string,
}

export declare interface BaseFormOptionCodeSystemConcept extends BaseFormOption, CodeSystemConcept {
  code: string;
}

export declare interface VaccinationForm extends BaseForm {
  // attributeName: keyof VaccinationEvent,
  attributeName: "enteringClinician" | "orderingClinician" | "administeringClinician" | "primarySource"
}
export declare interface VaccineForm extends BaseForm {
  attributeName: keyof Vaccine,
}


export declare interface NotificationPrototype {
  facility: Facility | number,
  timestamp: string,
}


export declare interface ComparisonResult { [index: string]: ComparisonResult | any | null }
export declare interface BulkImportStatus {
  status?: string,
  lastAttemptCount?: number,
  lastAttemptTime?: number,
  result?: string,
}
