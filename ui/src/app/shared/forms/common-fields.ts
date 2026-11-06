import {FormField, FormType} from './form-config';

/** Sub-fields shared by any entity holding addresses, phones or identifiers. */

export const ADDRESS_ITEM_FIELDS: FormField<never>[] = [
  {type: FormType.text, title: 'Line 1', attributeName: 'addressLine1'},
  {type: FormType.text, title: 'Line 2', attributeName: 'addressLine2'},
  {type: FormType.text, title: 'City', attributeName: 'addressCity'},
  {type: FormType.text, title: 'State', attributeName: 'addressState'},
  {type: FormType.text, title: 'ZIP', attributeName: 'addressZip'},
  {type: FormType.text, title: 'Country', attributeName: 'addressCountry'},
  {type: FormType.text, title: 'County / parish', attributeName: 'addressCountyParish'},
] as FormField<never>[];

export const PHONE_ITEM_FIELDS: FormField<never>[] = [
  {type: FormType.text, title: 'Number', attributeName: 'number'},
  // The codebase has no PHONE_* codesets: these are the HL7 telecom tables.
  {
    type: FormType.code,
    title: 'Type / equipment',
    attributeName: 'type',
    codeMapLabel: 'TELECOMMUNICATION_EQUIPMENT',
  },
  {type: FormType.code, title: 'Use', attributeName: 'use', codeMapLabel: 'TELECOMMUNICATION_USE'},
] as FormField<never>[];

export const IDENTIFIER_ITEM_FIELDS: FormField<never>[] = [
  {type: FormType.text, title: 'Value', attributeName: 'value'},
  {type: FormType.text, title: 'System', attributeName: 'system'},
  // Identifier types come from a bundled FHIR CodeSystem, not the codemaps.
  {type: FormType.code, title: 'Type', attributeName: 'type', codeSystem: 'identifierType'},
  {type: FormType.text, title: 'Assigner', attributeName: 'assignerReference'},
] as FormField<never>[];
