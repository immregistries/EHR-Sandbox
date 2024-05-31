import { Clinician, Facility, EhrPatient, VaccinationEvent, Vaccine, NextOfKin } from "./rest";
/**
 * Interface for codemaps lists
 */

export interface CodeBaseMap {
  [key: string]: CodeMap
}
export interface CodeMap {
  [key: string]: Code
}
export interface Code {
  "value": string,
  "label": string,
  "description"?: string,
  "codeStatus"?: string,
  "reference"?: CodeReference,
  "useDate"?: Date,
  "useAge"?: string,
  "conceptType"?: string,
  "testAge"?: string
}
export interface CodeReference {
  linkTo: CodeReferenceLink[]
}

export interface CodeReferenceLink {
  value: string,
  codeset: string
}
export interface FormCardGeneric<X> {
  // export interface FormCardGeneric<X extends Record<(string | number),Record<(string | number), any> {
  title: string,
  cols?: number, // dimensions of the card
  rows?: number,
  forms?: GenericForm<X>[],  // form fields for each specific objects
}
export interface GenericForm<X> extends BaseForm {
  attribute: Extract<keyof X, string>,
}

export interface FormCard {
  title: string,
  cols?: number, // dimensions of the card
  rows?: number,
  patientForms?: PatientForm[],  // form fields for each specific objects
  vaccinationForms?: VaccinationForm[],
  vaccineForms?: VaccineForm[],
  clinicianForms?: ClinicianForm[],
}

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
  nextOfKins = 'nextOfKins',
  races = 'races',
  addresses = 'addresses',
}

export default FormType;
export interface BaseForm {
  type: FormType,
  title: string,
  attribute: string,
  codeMapLabel?: string,
  disabled?: boolean,
  options?: { value: string, label?: string }[],
  required?: boolean,
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
