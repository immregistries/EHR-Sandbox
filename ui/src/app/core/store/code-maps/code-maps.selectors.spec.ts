/**
 * Selector exemplar: project a full AppState through the real selectors.
 * Cross-slice behaviour (here: the tenant flavor driving the deprecated
 * filter) is exercised by composing appStateFactory overrides.
 */
import {aCodeBaseMap, appStateFactory, aTenant} from '../../../../testing';
import {adapter as tenantAdapter, initialState as tenantInitial} from '../tenant/tenant.reducer';
import {initialState as codeMapsInitial} from './code-maps.reducer';
import {
  selectCode,
  selectCodeSet,
  selectCodeSetOptions,
  selectIdentifierTypeOptions,
  selectIncludeDeprecatedCodes,
  selectQualificationOptions,
  selectVisibleCodeBaseMap,
} from './code-maps.selectors';

describe('code-maps selectors', () => {
  function stateWithTenant(tenantName: string) {
    const tenant = aTenant({id: 1, nameDisplay: tenantName});
    return appStateFactory({
      route: {tenantId: 1},
      codeMaps: {...codeMapsInitial, codeBaseMap: aCodeBaseMap()},
      tenant: tenantAdapter.setAll([tenant], tenantInitial),
    });
  }

  it('hides deprecated codes for an ordinary tenant', () => {
    const codeSet = selectCodeSet('VACCINATION_CVX_CODE')(stateWithTenant('General Hospital'));

    expect(Object.keys(codeSet ?? {})).toEqual(['08']);
  });

  it('keeps deprecated codes for the PAIN_PERDU flavor', () => {
    const state = stateWithTenant('PAIN_PERDU Hospital');

    expect(selectIncludeDeprecatedCodes(state)).toBeTrue();
    // Key order is not meaningful: JS lists integer-like keys first.
    expect(Object.keys(selectCodeSet('VACCINATION_CVX_CODE')(state) ?? {}).sort()).toEqual([
      '08',
      '999',
    ]);
  });

  it('filters every code set in the map, not just the one requested', () => {
    const visible = selectVisibleCodeBaseMap(stateWithTenant('General Hospital'));

    expect(Object.keys(visible['VACCINATION_CVX_CODE'])).toEqual(['08']);
  });

  it('resolves a single code by value', () => {
    const code = selectCode('VACCINATION_CVX_CODE', '08')(stateWithTenant('General Hospital'));

    expect(code?.label).toBe('Hep B, adolescent or pediatric');
  });

  it('returns an empty option list for an unknown code set', () => {
    expect(selectCodeSetOptions('NOT_A_SET')(stateWithTenant('General Hospital'))).toEqual([]);
  });

  it('returns options as an array for select bindings', () => {
    const options = selectCodeSetOptions('VACCINATION_CVX_CODE')(stateWithTenant('General'));

    expect(options.map((code) => code.value)).toEqual(['08']);
  });
});

describe('bundled code system selectors', () => {
  function stateWith(identifierConcepts: { code: string; display: string }[]) {
    return appStateFactory({
      codeMaps: {
        ...codeMapsInitial,
        identifierTypeCodeSystem: {
          resourceType: 'CodeSystem',
          status: 'active',
          content: 'complete',
          concept: identifierConcepts,
        } as never,
      },
    });
  }

  it('projects identifier types into select options', () => {
    const options = selectIdentifierTypeOptions(
      stateWith([
        {code: 'MR', display: 'Medical record number'},
        {code: 'SS', display: 'Social security number'},
      ]),
    );

    expect(options).toEqual([
      {value: 'MR', label: 'Medical record number', description: undefined},
      {value: 'SS', label: 'Social security number', description: undefined},
    ]);
  });

  it('returns nothing before the code system has loaded', () => {
    expect(selectIdentifierTypeOptions(appStateFactory({}))).toEqual([]);
    expect(selectQualificationOptions(appStateFactory({}))).toEqual([]);
  });

  it('falls back to the code when a concept has no display', () => {
    const options = selectIdentifierTypeOptions(stateWith([{code: 'XX'} as never]));

    expect(options[0].label).toBe('XX');
  });
});
