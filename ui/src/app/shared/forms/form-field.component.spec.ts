import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {runInInjectionContext, Injector} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {ComponentHarness, renderComponent} from '../../../testing';
import {FormField, FormType} from './form-config';
import {FormEngineService} from './form-engine.service';
import {FormFieldComponent} from './form-field.component';

/**
 * These assert on the rendered DOM, not on control state. A field can hold a
 * correct validator and still never show the error — which is exactly what
 * happened when the error computeds did not react to control events.
 */
/**
 * The engine is a plain service, so build it outside the TestBed —
 * `TestBed.inject` would instantiate the module before `renderComponent`
 * gets to configure it.
 */
function makeEngine(): FormEngineService {
  const injector = Injector.create({
    providers: [{provide: FormBuilder, useValue: new FormBuilder()}, FormEngineService],
  });
  return injector.get(FormEngineService);
}

describe('FormFieldComponent validation rendering', () => {
  const REQUIRED_FIELD: FormField<never> = {
    type: FormType.text,
    title: 'Last name',
    attributeName: 'nameLast',
    required: true,
  } as FormField<never>;

  async function render(
    field: FormField<never> = REQUIRED_FIELD,
  ): Promise<{h: ComponentHarness<FormFieldComponent>; group: FormGroup}> {
    const engine = makeEngine();
    const group = engine.build([{title: 'Card', fields: [field]}] as never, {});
    const h = await renderComponent(FormFieldComponent, {inputs: {field, group}});
    return {h, group};
  }

  it('marks a required field in the label', async () => {
    const {h} = await render();

    expect(h.query('.required')).not.toBeNull();
  });

  it('does not mark an optional field', async () => {
    const {h} = await render({
      type: FormType.text,
      title: 'Middle name',
      attributeName: 'nameMiddle',
    } as FormField<never>);

    expect(h.query('.required')).toBeNull();
  });

  it('shows no error before the user has touched the field', async () => {
    const {h} = await render();

    expect(h.query('.error')).toBeNull();
  });

  it('renders the error once the field is touched and still empty', async () => {
    const {h, group} = await render();

    group.get('nameLast')?.markAsTouched();
    group.get('nameLast')?.updateValueAndValidity();
    h.detectChanges();

    expect(h.text('.error')).toBe('Last name is required');
  });

  it('clears the error once a value is entered', async () => {
    const {h, group} = await render();
    const control = group.get('nameLast');
    control?.markAsTouched();
    control?.updateValueAndValidity();
    h.detectChanges();
    expect(h.query('.error')).not.toBeNull();

    control?.setValue('Lovelace');
    h.detectChanges();

    expect(h.query('.error')).toBeNull();
  });

  it('renders a custom validator message', async () => {
    const {h, group} = await render({
      type: FormType.text,
      title: 'Lot number',
      attributeName: 'lotNumber',
      validator: () => ({customIssue: 'Lot number is not recognised'}),
    } as FormField<never>);

    const control = group.get('lotNumber');
    control?.markAsTouched();
    control?.setValue('nope');
    h.detectChanges();

    expect(h.text('.error')).toBe('Lot number is not recognised');
  });

  it('stops marking the field required once rules are lifted', async () => {
    const field = REQUIRED_FIELD;
    const engine = makeEngine();
    const config = [{title: 'Card', fields: [field]}] as never;
    const group = engine.build(config, {});
    const h = await renderComponent(FormFieldComponent, {inputs: {field, group}});
    expect(h.query('.required')).not.toBeNull();

    engine.applyRequiredRules(group, config, true);
    h.detectChanges();

    expect(h.query('.required')).toBeNull();
  });

  it('stops rendering the error once rules are lifted', async () => {
    const field = REQUIRED_FIELD;
    const engine = makeEngine();
    const config = [{title: 'Card', fields: [field]}] as never;
    const group = engine.build(config, {});
    const h = await renderComponent(FormFieldComponent, {inputs: {field, group}});

    group.get('nameLast')?.markAsTouched();
    group.get('nameLast')?.updateValueAndValidity();
    h.detectChanges();
    expect(h.query('.error')).not.toBeNull();

    engine.applyRequiredRules(group, config, true);
    h.detectChanges();

    expect(h.query('.error')).toBeNull();
  });

  it('shows the configured hint when there is no error', async () => {
    const {h} = await render({
      type: FormType.text,
      title: 'Lot number',
      attributeName: 'lotNumber',
      hint: () => 'Check the vial label',
    } as FormField<never>);

    expect(h.text('.hint')).toContain('Check the vial label');
  });

  it('shows the comparison value when one is supplied', async () => {
    const engine = makeEngine();
    const field = REQUIRED_FIELD;
    const group = engine.build([{title: 'Card', fields: [field]}] as never, {});

    const h = await renderComponent(FormFieldComponent, {
      inputs: {field, group, comparison: 'Byron'},
    });

    expect(h.text('.hint--compare')).toContain('Previously: Byron');
  });

  it('renders registry feedback as a warning, separately from validation', async () => {
    const engine = makeEngine();
    const field = REQUIRED_FIELD;
    const group = engine.build([{title: 'Card', fields: [field]}] as never, {});

    const h = await renderComponent(FormFieldComponent, {
      inputs: {field, group, warnings: ['WARN: Last name missing from registry']},
    });

    expect(h.text('.warning')).toContain('Last name missing from registry');
  });

  it('shows the HL7 location so feedback can be traced to the field', async () => {
    const {h} = await render({
      type: FormType.text,
      title: 'Last name',
      attributeName: 'nameLast',
      hl7Location: {segmentId: 'PID', fieldPosition: 5, componentNumber: 1},
    } as FormField<never>);

    expect(h.text('.hl7')).toBe('PID-5.1');
  });
});

describe('FormFieldComponent required rendering uses the live validator', () => {
  it('reflects a validator added after the control was rendered', async () => {
    const engine = makeEngine();
    const field = {
      type: FormType.text,
      title: 'Email',
      attributeName: 'email',
    } as FormField<never>;
    const group = engine.build([{title: 'Card', fields: [field]}] as never, {});

    const h = await renderComponent(FormFieldComponent, {inputs: {field, group}});
    expect(h.query('.required')).toBeNull();

    const control = group.get('email');
    control?.addValidators(Validators.required);
    control?.updateValueAndValidity();
    h.detectChanges();

    expect(h.query('.required')).not.toBeNull();
  });
});

describe('FormFieldComponent code options', () => {
  const IDENTIFIER_TYPE: FormField<never> = {
    type: FormType.code,
    title: 'Type',
    attributeName: 'type',
    // Identifier types come from a bundled FHIR CodeSystem, not the codemaps.
    codeSystem: 'identifierType',
  } as FormField<never>;

  it('offers the identifier types from the bundled code system', async () => {
    const engine = makeEngine();
    const group = engine.build([{title: 'Card', fields: [IDENTIFIER_TYPE]}] as never, {});

    const h = await renderComponent(FormFieldComponent, {
      inputs: {field: IDENTIFIER_TYPE, group},
      state: {
        codeMaps: {
          codeBaseMap: {},
          qualificationCodeSystem: null,
          callState: 'loaded',
          identifierTypeCodeSystem: {
            resourceType: 'CodeSystem',
            status: 'active',
            content: 'complete',
            concept: [
              {code: 'MR', display: 'Medical record number'},
              {code: 'SS', display: 'Social security number'},
            ],
          },
        } as never,
      },
    });

    expect(h.component.codeOptions().map((option) => option.value)).toEqual(['MR', 'SS']);
    expect(h.component.codeOptions()[0].label).toBe('Medical record number');
  });

  it('does not fall back to the codemaps for a code-system field', async () => {
    const engine = makeEngine();
    const group = engine.build([{title: 'Card', fields: [IDENTIFIER_TYPE]}] as never, {});

    const h = await renderComponent(FormFieldComponent, {
      inputs: {field: IDENTIFIER_TYPE, group},
      state: {
        codeMaps: {
          // A codemap of the same name must not be used.
          codeBaseMap: {type: {WRONG: {value: 'WRONG', label: 'Wrong source'}}},
          identifierTypeCodeSystem: null,
          qualificationCodeSystem: null,
          callState: 'loaded',
        } as never,
      },
    });

    expect(h.component.codeOptions()).toEqual([]);
  });

  it('still reads codemap-backed fields from the codemaps', async () => {
    const field = {
      type: FormType.code,
      title: 'Sex',
      attributeName: 'sex',
      codeMapLabel: 'PATIENT_SEX',
    } as FormField<never>;
    const engine = makeEngine();
    const group = engine.build([{title: 'Card', fields: [field]}] as never, {});

    const h = await renderComponent(FormFieldComponent, {
      inputs: {field, group},
      state: {
        codeMaps: {
          codeBaseMap: {PATIENT_SEX: {F: {value: 'F', label: 'Female'}}},
          identifierTypeCodeSystem: null,
          qualificationCodeSystem: null,
          callState: 'loaded',
        } as never,
      },
    });

    expect(h.component.codeOptions().map((option) => option.value)).toEqual(['F']);
  });
});
