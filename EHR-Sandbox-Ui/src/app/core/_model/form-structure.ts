import { CodeSystemConcept } from "fhir/r5";
import { EhrPatient, Facility, VaccinationEvent, Vaccine } from "./rest";
import { AsyncValidatorFn, ValidatorFn } from "@angular/forms";

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
  names = 'names',
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
  toolTips?: string,
  forms?: BaseForm[],
  vaccinationForms?: VaccinationForm[],
  vaccineForms?: VaccineForm[],
  hl7Location?: Hl7Location,
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
  customValidatorAsync?: AsyncValidatorFn;
  hintProducer?: (value?: string) => string;
  hl7Location?: Hl7Location,
}

/**
 * Structure will overflow from parent to child, consider segment id to be "default" when on atomic form definition
 */
export declare interface Hl7Location {
  segmentId: "PID" | "PD1" | "NK1" | "RXR" | "OBX" | "RXA" | "ORC",
  fieldPosition?: number,
  componentNumber?: number,
  subComponentNumber?: number,
  segmentSequence?: number,
  fieldRepetition?: number,
  abbreviated?: string
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

export declare interface AcknowledgementObject<T> {
  id?: number,
  messageId?: string,
  rawResult?: string,
  rawSource?: string,
  sender?: string,
  destination?: string,
  senderSoftware?: string,
  destinationSoftware?: string,
  msa_2?: string,
  timestamp?: Date,
  iis?: number | string,
  sortedResult: SortedResult<T>,
  patient?: EhrPatient | number
  vaccinations?: (VaccinationEvent | number)[]
}

export declare interface SortedResult<T> {
  errors: T[],
  warnings: T[],
  notices: T[],
  infos: T[]
}
