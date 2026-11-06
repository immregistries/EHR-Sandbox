import {Clinician} from '../../../core/models';
import {FormCard, FormType} from '../../../shared/forms/form-config';
import {ADDRESS_ITEM_FIELDS, IDENTIFIER_ITEM_FIELDS, PHONE_ITEM_FIELDS,} from '../../../shared/forms/common-fields';

/** Clinician form layout. Section order follows the previous UI. */
export const CLINICIAN_FORM_CONFIG: FormCard<Clinician>[] = [
  {
    title: 'Clinician name',
    fields: [
      {type: FormType.text, title: 'Prefix', attributeName: 'namePrefix'},
      {type: FormType.text, title: 'First name', attributeName: 'nameFirst'},
      {type: FormType.text, title: 'Middle name', attributeName: 'nameMiddle'},
      {type: FormType.text, title: 'Last name', attributeName: 'nameLast'},
      {type: FormType.text, title: 'Suffix', attributeName: 'nameSuffix'},
    ],
  },
  {
    title: 'Qualification',
    fields: [
      {
        type: FormType.code,
        title: 'Qualification',
        attributeName: 'qualification',
        // HL7 table 0360, published as a bundled FHIR CodeSystem.
        codeSystem: 'qualification',
      },
    ],
  },
  {
    title: 'Contact',
    fields: [
      {
        type: FormType.list,
        title: 'Phone number',
        attributeName: 'phones',
        itemFields: PHONE_ITEM_FIELDS,
        itemDefault: {},
        itemLabel: (item) => (item['use'] as string) ?? '',
      },
    ],
  },
  {
    title: 'Addresses',
    fields: [
      {
        type: FormType.list,
        title: 'Address',
        attributeName: 'addresses',
        itemFields: ADDRESS_ITEM_FIELDS,
        itemDefault: {},
        itemLabel: (item) => (item['addressCity'] as string) ?? '',
      },
    ],
  },
  {
    title: 'Identifiers',
    fields: [
      {
        type: FormType.list,
        title: 'Identifier',
        attributeName: 'identifiers',
        itemFields: IDENTIFIER_ITEM_FIELDS,
        itemDefault: {value: '', system: '', type: 'NPI'},
        itemLabel: (item) => (item['type'] as string) ?? '',
      },
    ],
  },
];
