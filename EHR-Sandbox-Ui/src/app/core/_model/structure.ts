import { CodeSystemConcept } from "fhir/r5";
import { Code } from "./code-base-map";
import { Clinician, Facility, EhrPatient, VaccinationEvent, Vaccine, NextOfKin } from "./rest";

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
export interface FormCardGeneric<X> extends FormCard {
  // export interface FormCardGeneric<X extends Record<(string | number),Record<(string | number), any> {
  forms?: GenericForm<X>[],  // form fields for each specific objects
}
export interface GenericForm<X> extends BaseForm {
  attribute: Extract<keyof X, string>,
}

export interface FormCard {
  title: string,
  cols?: number, // dimensions of the card not used anymore
  rows?: number,
  toolTips?: string,
  vaccinationForms?: VaccinationForm[],
  vaccineForms?: VaccineForm[],
}


export interface BaseForm {
  type: FormType,
  title: string,
  attribute: string,
  codeMapLabel?: string,
  disabled?: boolean,
  options?: BaseFormOption[],
  required?: boolean,
  defaultListEmptyValue?: string, // Set default value when used in a list type form, '{}' string will use hard coded default, undefined will add no value
}
export interface BaseFormOption {
  code: string | boolean, display?: string, definition?: string,
}

export interface BaseFormOptionCodeSystemConcept extends BaseFormOption, CodeSystemConcept {
  code: string;
}
export interface PatientForm extends BaseForm {
  attribute: keyof EhrPatient,
}
export interface VaccinationForm extends BaseForm {
  // attribute: keyof VaccinationEvent,
  attribute: "enteringClinician" | "orderingClinician" | "administeringClinician" | "primarySource"
}
export interface VaccineForm extends BaseForm {
  attribute: keyof Vaccine,
}

export interface NextOfKinForms extends BaseForm {
  attribute: keyof NextOfKin,
}
export interface ClinicianForm extends BaseForm {
  attribute: keyof Clinician,
  role?: "enteringClinician" | "orderingClinician" | "administeringClinician"
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
