import {EhrPatient} from '../../../core/models';
import {FormCard, FormField, FormType} from '../../../shared/forms/form-config';

const NAME_FIELDS: FormField<never>[] = [
  {
    type: FormType.text,
    title: 'Prefix',
    attributeName: 'namePrefix',
    hl7Location: {segmentId: 'PID', fieldPosition: 5, componentNumber: 5}
  },
  {
    type: FormType.text,
    title: 'First',
    attributeName: 'nameFirst',
    hl7Location: {segmentId: 'PID', fieldPosition: 5, componentNumber: 2}
  },
  {
    type: FormType.text,
    title: 'Middle',
    attributeName: 'nameMiddle',
    hl7Location: {segmentId: 'PID', fieldPosition: 5, componentNumber: 3}
  },
  {
    type: FormType.text,
    title: 'Last',
    attributeName: 'nameLast',
    hl7Location: {segmentId: 'PID', fieldPosition: 5, componentNumber: 1}
  },
  {
    type: FormType.text,
    title: 'Suffix',
    attributeName: 'nameSuffix',
    hl7Location: {segmentId: 'PID', fieldPosition: 5, componentNumber: 4}
  },
  {
    type: FormType.code,
    title: 'Type',
    attributeName: 'nameType',
    codeMapLabel: 'PERSON_NAME_TYPE',
    hl7Location: {segmentId: 'PID', fieldPosition: 5, componentNumber: 7}
  },
];

const IDENTIFIER_FIELDS: FormField<never>[] = [
  {
    type: FormType.text,
    title: 'Value',
    attributeName: 'value',
    hl7Location: {segmentId: 'PID', fieldPosition: 3, componentNumber: 1}
  },
  {
    type: FormType.text,
    title: 'System',
    attributeName: 'system',
    hl7Location: {segmentId: 'PID', fieldPosition: 3, componentNumber: 4}
  },
  {
    type: FormType.code,
    title: 'Type',
    attributeName: 'type',
    codeSystem: 'identifierType',
    hl7Location: {segmentId: 'PID', fieldPosition: 3, componentNumber: 5}
  },
  {type: FormType.text, title: 'Assigner', attributeName: 'assignerReference'},
];

const ADDRESS_FIELDS: FormField<never>[] = [
  {
    type: FormType.text,
    title: 'Line 1',
    attributeName: 'addressLine1',
    hl7Location: {segmentId: 'PID', fieldPosition: 11, componentNumber: 1}
  },
  {
    type: FormType.text,
    title: 'Line 2',
    attributeName: 'addressLine2',
    hl7Location: {segmentId: 'PID', fieldPosition: 11, componentNumber: 2}
  },
  {
    type: FormType.text,
    title: 'City',
    attributeName: 'addressCity',
    hl7Location: {segmentId: 'PID', fieldPosition: 11, componentNumber: 3}
  },
  {
    type: FormType.text,
    title: 'State',
    attributeName: 'addressState',
    hl7Location: {segmentId: 'PID', fieldPosition: 11, componentNumber: 4}
  },
  {
    type: FormType.text,
    title: 'ZIP',
    attributeName: 'addressZip',
    hl7Location: {segmentId: 'PID', fieldPosition: 11, componentNumber: 5}
  },
  {
    type: FormType.text,
    title: 'Country',
    attributeName: 'addressCountry',
    hl7Location: {segmentId: 'PID', fieldPosition: 11, componentNumber: 6}
  },
  {
    type: FormType.text,
    title: 'County / parish',
    attributeName: 'addressCountyParish',
    hl7Location: {segmentId: 'PID', fieldPosition: 11, componentNumber: 9}
  },
];

const PHONE_FIELDS: FormField<never>[] = [
  {type: FormType.text, title: 'Number', attributeName: 'number', hl7Location: {segmentId: 'PID', fieldPosition: 13}},
  {type: FormType.code, title: 'Type', attributeName: 'type', codeMapLabel: 'PHONE_TYPE'},
  {type: FormType.code, title: 'Use', attributeName: 'use', codeMapLabel: 'PHONE_USE'},
];

const RACE_FIELDS: FormField<never>[] = [
  {
    type: FormType.code,
    title: 'Race',
    attributeName: 'value',
    codeMapLabel: 'PATIENT_RACE',
    hl7Location: {segmentId: 'PID', fieldPosition: 10}
  },
];

/**
 * A next-of-kin entry nests the person under `nextOfKin`, so these fields
 * declare the path they read and write.
 */
const NEXT_OF_KIN_FIELDS: FormField<never>[] = [
  {
    type: FormType.text,
    title: 'First',
    attributeName: 'nameFirst',
    path: 'nextOfKin.nameFirst',
    hl7Location: {segmentId: 'NK1', fieldPosition: 2, componentNumber: 2}
  },
  {
    type: FormType.text,
    title: 'Last',
    attributeName: 'nameLast',
    path: 'nextOfKin.nameLast',
    hl7Location: {segmentId: 'NK1', fieldPosition: 2, componentNumber: 1}
  },
  {
    type: FormType.text,
    title: 'Middle',
    attributeName: 'nameMiddle',
    path: 'nextOfKin.nameMiddle',
    hl7Location: {segmentId: 'NK1', fieldPosition: 2, componentNumber: 3}
  },
  {
    type: FormType.code,
    title: 'Relationship',
    attributeName: 'relationshipKind',
    codeMapLabel: 'PERSON_RELATIONSHIP',
    hl7Location: {segmentId: 'NK1', fieldPosition: 3}
  },
  {type: FormType.date, title: 'Birth date', attributeName: 'birthDate', path: 'nextOfKin.birthDate'},
  {type: FormType.code, title: 'Sex', attributeName: 'sex', path: 'nextOfKin.sex', codeMapLabel: 'PATIENT_SEX'},
  {type: FormType.text, title: 'Email', attributeName: 'email', path: 'nextOfKin.email'},
];

/**
 * Patient form layout. Section order matches the previous UI; repeating
 * groups (names, identifiers, addresses, phones, races, next of kin) render
 * as a nested card per entry.
 */
export const PATIENT_FORM_CONFIG: FormCard<EhrPatient>[] = [
  {
    title: 'Name',
    hl7Location: {segmentId: 'PID', fieldPosition: 5},
    fields: [
      {
        type: FormType.list,
        title: 'Name',
        attributeName: 'names',
        itemFields: NAME_FIELDS,
        itemDefault: {namePrefix: '', nameFirst: '', nameLast: '', nameMiddle: '', nameSuffix: '', nameType: 'L'},
        itemLabel: (item) => (item['nameType'] as string) ?? '',
        hl7Location: {segmentId: 'PID', fieldPosition: 5},
        // A SINGLENAME tenant takes exactly one name and no name type.
        flavors: {
          SINGLENAME: {singleEntry: true, omitItemFields: ['nameType']},
        },
      },
      {
        type: FormType.text,
        title: 'Mother maiden name',
        attributeName: 'motherMaiden',
        hl7Location: {segmentId: 'PID', componentNumber: 6}
      },
      {
        type: FormType.clinician,
        title: 'General practitioner',
        attributeName: 'generalPractitioner',
        hl7Location: {segmentId: 'PD1', componentNumber: 4}
      },
    ],
  },
  {
    title: 'Identifiers / Medical record number',
    hl7Location: {segmentId: 'PID', fieldPosition: 3},
    fields: [
      {
        type: FormType.list,
        title: 'Identifier',
        attributeName: 'identifiers',
        itemFields: IDENTIFIER_FIELDS,
        itemDefault: {value: '', system: '', type: 'MR'},
        itemLabel: (item) => (item['type'] as string) ?? '',
        hl7Location: {segmentId: 'PID', fieldPosition: 3},
      },
    ],
  },
  {
    title: 'Birth',
    fields: [
      {
        type: FormType.date,
        title: 'Birth date',
        attributeName: 'birthDate',
        required: true,
        hl7Location: {segmentId: 'PID', componentNumber: 7}
      },
      {
        type: FormType.yesNo,
        title: 'Multiple birth',
        attributeName: 'birthFlag',
        hl7Location: {segmentId: 'PID', componentNumber: 24}
      },
      {
        type: FormType.short,
        title: 'Order',
        attributeName: 'birthOrder',
        hl7Location: {segmentId: 'PID', componentNumber: 25}
      },
    ],
  },
  {
    title: 'Identity',
    fields: [
      {
        type: FormType.code,
        title: 'Sex',
        attributeName: 'sex',
        codeMapLabel: 'PATIENT_SEX',
        hl7Location: {segmentId: 'PID', componentNumber: 8}
      },
      {
        type: FormType.code,
        title: 'Ethnicity',
        attributeName: 'ethnicity',
        codeMapLabel: 'PATIENT_ETHNICITY',
        hl7Location: {segmentId: 'PID', componentNumber: 22}
      },
      {
        type: FormType.list,
        title: 'Race',
        attributeName: 'races',
        itemFields: RACE_FIELDS,
        itemDefault: {value: ''},
        hl7Location: {segmentId: 'PID', fieldPosition: 10},
      },
    ],
  },
  {
    title: 'Address',
    hl7Location: {segmentId: 'PID', fieldPosition: 11},
    fields: [
      {
        type: FormType.list,
        title: 'Address',
        attributeName: 'addresses',
        itemFields: ADDRESS_FIELDS,
        itemDefault: {},
        itemLabel: (item) => (item['addressCity'] as string) ?? '',
        hl7Location: {segmentId: 'PID', fieldPosition: 11},
      },
    ],
  },
  {
    title: 'Contact',
    fields: [
      {
        type: FormType.text,
        title: 'Email',
        attributeName: 'email',
        hl7Location: {segmentId: 'PID', componentNumber: 13}
      },
      {
        type: FormType.list,
        title: 'Phone',
        attributeName: 'phones',
        itemFields: PHONE_FIELDS,
        itemDefault: {},
        itemLabel: (item) => (item['use'] as string) ?? '',
        hl7Location: {segmentId: 'PID', fieldPosition: 13},
      },
    ],
  },
  {
    title: 'Death',
    fields: [
      {
        type: FormType.yesNo,
        title: 'Death flag',
        attributeName: 'deathFlag',
        hl7Location: {segmentId: 'PID', componentNumber: 30}
      },
      {
        type: FormType.date,
        title: 'Death date',
        attributeName: 'deathDate',
        hl7Location: {segmentId: 'PID', componentNumber: 29}
      },
    ],
  },
  {
    title: 'Publicity',
    fields: [
      {
        type: FormType.code,
        title: 'Indicator',
        attributeName: 'publicityIndicator',
        codeMapLabel: 'PATIENT_PUBLICITY',
        hl7Location: {segmentId: 'PD1', componentNumber: 11}
      },
      {
        type: FormType.date,
        title: 'Date',
        attributeName: 'publicityIndicatorDate',
        hl7Location: {segmentId: 'PD1', componentNumber: 12}
      },
    ],
  },
  {
    title: 'Protection',
    fields: [
      {
        type: FormType.yesNo,
        title: 'Indicator',
        attributeName: 'protectionIndicator',
        hl7Location: {segmentId: 'PD1', componentNumber: 12}
      },
      {
        type: FormType.date,
        title: 'Date',
        attributeName: 'protectionIndicatorDate',
        hl7Location: {segmentId: 'PD1', componentNumber: 13}
      },
    ],
  },
  {
    title: 'Registry',
    fields: [
      {
        type: FormType.code,
        title: 'Indicator',
        attributeName: 'registryStatusIndicator',
        codeMapLabel: 'REGISTRY_STATUS',
        hl7Location: {segmentId: 'PD1', componentNumber: 16}
      },
      {
        type: FormType.date,
        title: 'Date',
        attributeName: 'registryStatusIndicatorDate',
        hl7Location: {segmentId: 'PD1', componentNumber: 17}
      },
    ],
  },
  {
    title: 'Immunization financial status',
    fields: [
      {
        type: FormType.code,
        title: 'Financial status',
        attributeName: 'financialStatus',
        codeMapLabel: 'FINANCIAL_STATUS_CODE'
      },
    ],
  },
  {
    title: 'Next of kin',
    hl7Location: {segmentId: 'NK1'},
    fields: [
      {
        type: FormType.list,
        title: 'Next of kin',
        attributeName: 'nextOfKinRelationships',
        itemFields: NEXT_OF_KIN_FIELDS,
        itemDefault: {},
        itemLabel: (item) => (item['relationshipKind'] as string) ?? '',
        hl7Location: {segmentId: 'NK1'},
      },
    ],
  },
];
