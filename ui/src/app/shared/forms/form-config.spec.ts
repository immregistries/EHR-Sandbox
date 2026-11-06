import {KNOWN_CODESETS} from '../../../testing';
import {CLINICIAN_FORM_CONFIG} from '../../features/clinician/clinician-form/clinician-form.config';
import {PATIENT_FORM_CONFIG} from '../../features/patient/patient-form/patient-form.config';
import {REGISTRY_FORM_CONFIG} from '../../features/settings/registry-form/registry-form.config';
import {VACCINATION_FORM_CONFIG} from '../../features/vaccination/vaccination-form/vaccination-form.config';
import {FormConfig, FormField, FormType} from './form-config';

/**
 * A `codeMapLabel` the backend does not publish yields an empty dropdown at
 * runtime with no error — exactly how the phone type/use fields shipped
 * broken. These pin every key against the codebase's real codeset list.
 */
const CONFIGS: [string, FormConfig<never>][] = [
  ['patient', PATIENT_FORM_CONFIG as FormConfig<never>],
  ['vaccination', VACCINATION_FORM_CONFIG as FormConfig<never>],
  ['clinician', CLINICIAN_FORM_CONFIG as FormConfig<never>],
  ['registry', REGISTRY_FORM_CONFIG as FormConfig<never>],
];

function allFields(config: FormConfig<never>): FormField<never>[] {
  return config.flatMap((card) =>
    card.fields.flatMap((field) => [field, ...(field.itemFields ?? [])]),
  );
}

describe('form configs', () => {
  for (const [name, config] of CONFIGS) {
    it(`${name}: every codeMapLabel exists in the backend codebase`, () => {
      const unknown = allFields(config)
        .map((field) => field.codeMapLabel)
        .filter((label): label is string => Boolean(label))
        .filter((label) => !KNOWN_CODESETS.has(label));

      expect(unknown).toEqual([]);
    });

    it(`${name}: every code field has a source of options`, () => {
      // A code field with neither a codemap, a code system nor static
      // options can only ever render an empty dropdown.
      const sourceless = allFields(config)
        .filter((field) => field.type === FormType.code)
        .filter((field) => !field.codeMapLabel && !field.codeSystem && !field.options)
        .map((field) => field.attributeName);

      expect(sourceless).toEqual([]);
    });

    it(`${name}: every field has a title and an attribute`, () => {
      const broken = allFields(config)
        .filter((field) => !field.title || !field.attributeName)
        .map((field) => field.title || field.attributeName);

      expect(broken).toEqual([]);
    });

    it(`${name}: every list field declares its item fields`, () => {
      const incomplete = config
        .flatMap((card) => card.fields)
        .filter((field) => field.type === FormType.list)
        .filter((field) => !field.itemFields?.length)
        .map((field) => field.attributeName);

      expect(incomplete).toEqual([]);
    });
  }

  it('uses the telecom codesets for phone fields, not PHONE_*', () => {
    const phoneLabels = allFields(PATIENT_FORM_CONFIG as FormConfig<never>)
      .filter((field) => ['type', 'use'].includes(field.attributeName) && field.codeMapLabel)
      .map((field) => field.codeMapLabel);

    expect(phoneLabels).toContain('TELECOMMUNICATION_USE');
    expect(phoneLabels).toContain('TELECOMMUNICATION_EQUIPMENT');
  });
});
