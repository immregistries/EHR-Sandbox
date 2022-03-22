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
  cols?: number,
  rows?: number,
  patientForms?: PatientForm[],
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
  // options?: string[],
  attribute: string,
  codeMapLabel?: string,
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
