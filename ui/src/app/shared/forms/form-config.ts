import {AsyncValidatorFn, ValidatorFn} from '@angular/forms';

/**
 * Declarative form configuration, shared by every entity form (patient,
 * vaccination, facility, clinician...). A form is a list of cards; a card is
 * a list of fields; a field names an attribute of the entity.
 */
export enum FormType {
  text = 'text',
  textarea = 'textarea',
  short = 'short',
  date = 'date',
  boolean = 'boolean',
  yesNo = 'yesNo',
  select = 'select',
  code = 'code',
  clinician = 'clinician',
  /** A repeating group of sub-fields, rendered as one card per entry. */
  list = 'list',
}

/**
 * Where a field lives in an HL7v2 message. Registry feedback is reported
 * against these coordinates, which is how a warning finds its input.
 */
export interface Hl7Location {
  segmentId: 'PID' | 'PD1' | 'NK1' | 'RXR' | 'OBX' | 'RXA' | 'ORC';
  fieldPosition?: number;
  componentNumber?: number;
  subComponentNumber?: number;
  segmentSequence?: number;
  fieldRepetition?: number;
}

export interface FormOption {
  code: string | boolean;
  display?: string;
  definition?: string;
}

export interface FormField<T = Record<string, unknown>> {
  type: FormType;
  title: string;
  attributeName: Extract<keyof T, string> & string;
  required?: boolean;
  disabled?: boolean;
  /** Key into the code-maps store, for FormType.code. */
  codeMapLabel?: string;
  /**
   * For FormType.code: read the options from a bundled FHIR CodeSystem
   * instead of the codemaps. Identifier and qualification types are
   * published as code systems, not as codemap entries.
   */
  codeSystem?: 'identifierType' | 'qualification';
  /** Static options, for FormType.select. */
  options?: FormOption[];
  validator?: ValidatorFn;
  asyncValidator?: AsyncValidatorFn;
  /** Live hint shown under the control. */
  hint?: (value: unknown) => string | undefined;
  hl7Location?: Hl7Location;
  /** FormType.list: the shape of one entry. */
  itemFields?: FormField<never>[];
  /** FormType.list: a new entry's default value. */
  itemDefault?: Record<string, unknown>;
  /** FormType.list: builds the per-entry card title suffix. */
  itemLabel?: (item: Record<string, unknown>, index: number) => string;
  /** Width hint within the card grid. */
  span?: 1 | 2 | 3;
  /**
   * Dotted path to the value when it is not a direct property, e.g.
   * 'nextOfKin.nameFirst'. The control is still keyed by attributeName.
   */
  path?: string;
  /**
   * FormType.list: render exactly one entry, with no add or remove controls.
   * The value stays an array, so the API contract is unchanged.
   */
  singleEntry?: boolean;
  /**
   * Adjustments applied when a tenant flavor is active, keyed by flavor key.
   * See `applyFlavors`.
   */
  flavors?: Record<string, FieldFlavorOverride>;
}

/** How a tenant flavor changes a field. */
export interface FieldFlavorOverride {
  /** FormType.list: collapse to a single entry. */
  singleEntry?: boolean;
  /** Sub-fields to drop, by attributeName. */
  omitItemFields?: string[];
  /** Hide the field entirely. */
  omit?: boolean;
  required?: boolean;
}

export interface FormCard<T = Record<string, unknown>> {
  title: string;
  /** Explanatory note rendered under the card. */
  note?: string;
  fields: FormField<T>[];
  hl7Location?: Hl7Location;
}

export type FormConfig<T> = FormCard<T>[];

/** Renders an HL7 location as the `PID-5.1` style notation used in feedback. */
export function hl7LocationLabel(location: Hl7Location | undefined): string {
  if (!location) {
    return '';
  }
  let label: string = location.segmentId;
  if (location.fieldPosition !== undefined) {
    label += `-${location.fieldPosition}`;
  }
  if (location.componentNumber !== undefined) {
    label += location.fieldPosition !== undefined ? `.${location.componentNumber}` : `-${location.componentNumber}`;
  }
  if (location.subComponentNumber !== undefined) {
    label += `.${location.subComponentNumber}`;
  }
  return label;
}

/** True when two locations address the same place in a message. */
export function hl7LocationMatches(a: Hl7Location | undefined, b: Hl7Location | undefined): boolean {
  if (!a || !b || a.segmentId !== b.segmentId) {
    return false;
  }
  const samePart = (x?: number, y?: number) => x === undefined || y === undefined || x === y;
  return (
    samePart(a.fieldPosition, b.fieldPosition) &&
    samePart(a.componentNumber, b.componentNumber) &&
    samePart(a.subComponentNumber, b.subComponentNumber)
  );
}
