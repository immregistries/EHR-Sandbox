import { Clinician, Patient, VaccinationEvent, Vaccine } from "./rest";
export interface Code {
  "value": string,
  "label": string,
  "description"?: string,
  "codeStatus"?: string,
  "reference"?: string,
  "useDate"?: Date,
  "useAge"?: string,
  "conceptType"?: string,
  "testAge"?: string
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
