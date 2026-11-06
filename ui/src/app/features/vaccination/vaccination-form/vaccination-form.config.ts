import {VaccinationEvent} from '../../../core/models';
import {FormConfig, FormType} from '../../../shared/forms/form-config';

/**
 * Vaccination form layout. Section order follows the previous UI.
 *
 * Most fields live on the nested `vaccine`, so those cards declare
 * `path: 'vaccine'`; the clinician cards read the VaccinationEvent itself.
 */
export const VACCINATION_FORM_CONFIG: FormConfig<VaccinationEvent> = [
  {
    title: 'Vaccine',
    path: 'vaccine',
    fields: [
      {
        type: FormType.date,
        title: 'Administered',
        attributeName: 'administeredDate',
        required: true,
        hl7Location: {segmentId: 'RXA', componentNumber: 3},
      },
      {
        type: FormType.text,
        title: 'Amount administered (mL)',
        attributeName: 'administeredAmount',
        hl7Location: {segmentId: 'RXA', componentNumber: 6},
      },
    ],
  },
  {
    title: 'Record nature',
    fields: [
      {
        type: FormType.select,
        title: 'Record nature',
        attributeName: 'primarySource',
        options: [
          {code: 'true', display: 'New administration'},
          {code: 'false', display: 'Historical'},
        ],
      },
    ],
  },
  {
    title: 'Codes',
    path: 'vaccine',
    fields: [
      {
        type: FormType.code,
        title: 'Vaccine type (CVX)',
        attributeName: 'vaccineCvxCode',
        codeMapLabel: 'VACCINATION_CVX_CODE',
        required: true,
        hl7Location: {segmentId: 'RXA', componentNumber: 5},
      },
      {
        type: FormType.code,
        title: 'Unit of use (NDC)',
        attributeName: 'vaccineNdcCode',
        codeMapLabel: 'VACCINATION_NDC_CODE_UNIT_OF_USE',
        hl7Location: {segmentId: 'RXA', componentNumber: 5},
      },
    ],
  },
  {
    title: 'Request',
    path: 'vaccine',
    fields: [
      {
        type: FormType.code,
        title: 'Information source',
        attributeName: 'informationSource',
        codeMapLabel: 'VACCINATION_INFORMATION_SOURCE',
        required: true,
        hl7Location: {segmentId: 'RXA', componentNumber: 9},
      },
      {
        type: FormType.code,
        title: 'Action code',
        attributeName: 'actionCode',
        codeMapLabel: 'VACCINATION_ACTION_CODE',
        hl7Location: {segmentId: 'RXA', componentNumber: 21},
      },
    ],
  },
  {
    title: 'Lot',
    path: 'vaccine',
    fields: [
      {
        type: FormType.code,
        title: 'Manufacturer (MVX)',
        attributeName: 'vaccineMvxCode',
        codeMapLabel: 'VACCINATION_MANUFACTURER_CODE',
        hl7Location: {segmentId: 'RXA', componentNumber: 17},
      },
      {
        type: FormType.text,
        title: 'Lot number',
        attributeName: 'lotNumber',
        hl7Location: {segmentId: 'RXA', componentNumber: 15},
      },
      {
        type: FormType.date,
        title: 'Expiration date',
        attributeName: 'expirationDate',
        hl7Location: {segmentId: 'RXA', componentNumber: 16},
      },
    ],
  },
  {
    title: 'Funding',
    path: 'vaccine',
    fields: [
      {
        type: FormType.code,
        title: 'Source',
        attributeName: 'fundingSource',
        codeMapLabel: 'VACCINATION_FUNDING_SOURCE',
        hl7Location: {segmentId: 'OBX', componentNumber: 5},
      },
      {
        type: FormType.code,
        title: 'Financial status (dose level accountability)',
        attributeName: 'financialStatus',
        codeMapLabel: 'FINANCIAL_STATUS_CODE',
        hl7Location: {segmentId: 'OBX', componentNumber: 5},
      },
    ],
  },
  {
    title: 'Information statement (VIS)',
    path: 'vaccine',
    fields: [
      {
        type: FormType.code,
        title: 'Document',
        attributeName: 'informationStatement',
        codeMapLabel: 'VACCINATION_VIS_DOC_TYPE',
        hl7Location: {segmentId: 'OBX', componentNumber: 5},
      },
      {
        type: FormType.date,
        title: 'Presented date',
        attributeName: 'informationStatementPresentedDate',
        hl7Location: {segmentId: 'OBX', componentNumber: 5},
      },
      {
        type: FormType.code,
        title: 'Statement CVX',
        attributeName: 'informationStatementCvx',
        codeMapLabel: 'VACCINATION_VIS_CVX_CODE',
        hl7Location: {segmentId: 'OBX', componentNumber: 5},
      },
      {
        type: FormType.date,
        title: 'Published date',
        attributeName: 'informationStatementPublishedDate',
        hl7Location: {segmentId: 'OBX', componentNumber: 5},
      },
    ],
  },
  {
    title: 'Injection route',
    path: 'vaccine',
    fields: [
      {
        type: FormType.code,
        title: 'Route',
        attributeName: 'bodyRoute',
        codeMapLabel: 'BODY_ROUTE',
        hl7Location: {segmentId: 'RXR', componentNumber: 1},
      },
      {
        type: FormType.code,
        title: 'Site',
        attributeName: 'bodySite',
        codeMapLabel: 'BODY_SITE',
        hl7Location: {segmentId: 'RXR', componentNumber: 2},
      },
    ],
  },
  {
    title: 'Injection status',
    path: 'vaccine',
    fields: [
      {
        type: FormType.code,
        title: 'Completion status',
        attributeName: 'completionStatus',
        codeMapLabel: 'VACCINATION_COMPLETION',
        hl7Location: {segmentId: 'RXA', componentNumber: 20},
      },
      {
        type: FormType.code,
        title: 'Refusal reason',
        attributeName: 'refusalReasonCode',
        codeMapLabel: 'VACCINATION_REFUSAL',
        hl7Location: {segmentId: 'RXA', componentNumber: 18},
      },
    ],
  },
  {
    title: 'Clinicians',
    fields: [
      {
        type: FormType.clinician,
        title: 'Entering',
        attributeName: 'enteringClinician',
        hl7Location: {segmentId: 'ORC', componentNumber: 10},
      },
      {
        type: FormType.clinician,
        title: 'Ordering',
        attributeName: 'orderingClinician',
        hl7Location: {segmentId: 'ORC', componentNumber: 12},
      },
      {
        type: FormType.clinician,
        title: 'Administering',
        attributeName: 'administeringClinician',
        hl7Location: {segmentId: 'RXA', componentNumber: 10},
      },
    ],
  },
  {
    title: 'Update dates',
    path: 'vaccine',
    fields: [
      {type: FormType.date, title: 'Creation date', attributeName: 'createdDate', disabled: true},
      {type: FormType.date, title: 'Updated date', attributeName: 'updatedDate', disabled: true},
    ],
  },
];
