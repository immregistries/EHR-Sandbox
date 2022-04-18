import { Clinician, Patient, VaccinationEvent, Vaccine } from "./rest";
/**
 * Interface for codemaps lists
 */

export interface CodeBaseMap {
  [key:string]: CodeMap
}
export interface CodeMap {
  [key:string]: Code
}
export interface Code {
  "value": string,
  "label": string,
  "description"?: string,
  "codeStatus"?: string,
  "reference"?: Reference,
  "useDate"?: Date,
  "useAge"?: string,
  "conceptType"?: string,
  "testAge"?: string
}
export interface Reference {
  linkTo: ReferenceLink[]
}

export interface ReferenceLink {
  value: string,
  codeset: string
}
export interface FormCard {
  title?: string,
  cols?: number, // dimensions of the card
  rows?: number,
  patientForms?: PatientForm[],  // form fields for each specific objects
  vaccinationForms?: VaccinationForm[],
  vaccineForms?: VaccineForm[],
  clinicianForms?: ClinicianForm[],
}

export enum formType {
  text = 'text',
  date = 'date',
  short = 'short',
  boolean = 'boolean',
  select = 'select',
  textarea = 'textarea',
}
export interface Form {
  type: formType,
  title: string,
  attribute: string,
  codeMapLabel?: string,
  codeLabel?: string,
}
export interface PatientForm extends Form{
  attribute: keyof Patient,
}
export interface VaccinationForm extends Form{
  attribute: keyof VaccinationEvent,
}
export interface VaccineForm extends Form{
  attribute: keyof Vaccine,
}
export interface ClinicianForm extends Form{
  attribute: keyof Clinician,
  role: "enteringClinician" | "orderingClinician" | "administeringClinician"
}
