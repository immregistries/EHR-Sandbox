import { Patient } from "./rest";

export interface FormCard {
  title?: string,
  cols?: number,
  rows?: number,
  forms: Form[],
}

export enum formType {
  text = 'text',
  date = 'date',
  short = 'short',
  boolean = 'boolean',
  textarea = 'textarea',
}

export interface Form {
  type: formType,
  title: string,
  attribute: keyof Patient,
  options?: string[]
}
