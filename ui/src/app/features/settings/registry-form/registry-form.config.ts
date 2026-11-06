import {ImmunizationRegistry} from '../../../core/models';
import {FormConfig, FormType} from '../../../shared/forms/form-config';

/**
 * IIS connection settings. Section order follows the previous UI:
 * identity, the three API urls, then remote credentials.
 */
export const REGISTRY_FORM_CONFIG: FormConfig<ImmunizationRegistry> = [
  {
    title: 'IIS information',
    fields: [
      {type: FormType.text, title: 'Nickname', attributeName: 'name', required: true, span: 3},
      {type: FormType.textarea, title: 'Description', attributeName: 'description', span: 3},
      {
        type: FormType.text,
        title: 'Receiving facility',
        attributeName: 'receivingFacility',
        span: 3,
        hint: () => 'Sent as the MSH-6 segment of outgoing messages',
      },
    ],
  },
  {
    title: 'API URLs',
    fields: [
      {type: FormType.text, title: 'FHIR RESTful API URL', attributeName: 'iisFhirUrl', span: 3},
      {
        type: FormType.text,
        title: 'FHIR messaging URL',
        attributeName: 'iisFhirMessagingUrl',
        span: 3,
      },
      {type: FormType.text, title: 'HL7v2 SOAP URL', attributeName: 'iisHl7Url', span: 3},
    ],
  },
  {
    title: 'Remote login information',
    note:
      'Basic authentication is the default. Leaving the username empty switches to ' +
      'token bearer authentication using the password as the token.',
    fields: [
      {type: FormType.text, title: 'IIS facility id', attributeName: 'iisFacilityId'},
      {type: FormType.text, title: 'IIS username', attributeName: 'iisUsername'},
      {type: FormType.text, title: 'IIS password', attributeName: 'iisPassword'},
      {type: FormType.text, title: 'Extra headers', attributeName: 'headers', span: 3},
    ],
  },
];
