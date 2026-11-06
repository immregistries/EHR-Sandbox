import {FormArray} from '@angular/forms';
import {of} from 'rxjs';
import {EhrPatient} from '../../../core/models';
import {PatientService} from '../../../core/services';
import {aPatient, ComponentHarness, renderComponent, resetFixtureIds} from '../../../../testing';
import {adapter as tenantAdapter, initialState as tenantInitialState,} from '../../../core/store/tenant/tenant.reducer';
import {PatientFormComponent} from './patient-form.component';
import {PATIENT_FORM_CONFIG} from './patient-form.config';

describe('PatientFormComponent', () => {
  let h: ComponentHarness<PatientFormComponent>;

  beforeEach(async () => {
    resetFixtureIds();
    h = await renderComponent(PatientFormComponent, {
      state: {route: {tenantId: 1, facilityId: 2}},
    });
  });

  it('renders every configured section', () => {
    const titles = h.queryAll<HTMLElement>('app-form-card h3').map((el) => el.textContent?.trim());

    expect(titles).toEqual(PATIENT_FORM_CONFIG.map((card) => card.title));
  });

  it('keeps the section order of the previous UI', () => {
    const titles = h.queryAll<HTMLElement>('app-form-card h3').map((el) => el.textContent?.trim());

    expect(titles.slice(0, 6)).toEqual([
      'Name',
      'Identifiers / Medical record number',
      'Birth',
      'Identity',
      'Address',
      'Contact',
    ]);
  });

  it('marks birth date required, as the old form did', () => {
    expect(h.component.form().get('birthDate')?.hasError('required')).toBeTrue();
  });

  it('blocks submission while required fields are empty', () => {
    let saved = 0;
    h.component.saved.subscribe(() => (saved += 1));

    h.component.submit();

    expect(saved).toBe(0);
    expect(h.component.form().get('birthDate')?.touched).toBeTrue();
  });

  it('builds a repeating group per list section', () => {
    expect(h.component.form().get('names') instanceof FormArray).toBeTrue();
    expect(h.component.form().get('identifiers') instanceof FormArray).toBeTrue();
    expect(h.component.form().get('nextOfKinRelationships') instanceof FormArray).toBeTrue();
  });
});

describe('PatientFormComponent with registry feedback', () => {
  it('surfaces feedback against the field it refers to', async () => {
    const h = await renderComponent(PatientFormComponent, {
      state: {route: {tenantId: 1, facilityId: 2}},
      inputs: {
        issues: [
          {
            content: 'Birth date is in the future',
            severity: 'WARN',
            hl7Locations: [{segmentId: 'PID', componentNumber: 7}],
          },
        ],
      },
    });

    expect(h.component.warnings()['birthDate']).toEqual(['WARN: Birth date is in the future']);
  });
});

describe('PatientFormComponent when editing', () => {
  it('populates the form from the patient being edited', async () => {
    resetFixtureIds();
    const patient = aPatient({
      id: 4,
      names: [{nameFirst: 'Ada', nameLast: 'Lovelace', nameType: 'L'}],
      sex: 'F',
    });

    const h = await renderComponent(PatientFormComponent, {
      state: {route: {tenantId: 1, facilityId: 2}},
      inputs: {patient},
    });

    expect(h.component.form().get('sex')?.value).toBe('F');
    expect((h.component.form().get('names') as FormArray).length).toBe(1);
  });
});

describe('PatientFormComponent under the SINGLENAME flavor', () => {
  it('takes one name with no name type', async () => {
    const h = await renderComponent(PatientFormComponent, {
      state: {
        route: {tenantId: 1, facilityId: 2},
        tenant: {
          ...tenantAdapter.setAll(
            [{id: 1, nameDisplay: 'SINGLENAME Hospital'}],
            tenantInitialState,
          ),
          flavors: [{key: 'SINGLENAME', description: 'One name only', hidden: false}],
        },
      },
    });

    const names = h.component.config().find((card) => card.title === 'Name')?.fields[0];

    expect(names?.singleEntry).toBeTrue();
    expect(names?.itemFields?.map((field) => field.attributeName)).not.toContain('nameType');
    // The value stays an array, so the API contract is unchanged.
    expect((h.component.form().get('names') as FormArray).length).toBe(1);
  });
});

describe('PatientFormComponent under a plain tenant', () => {
  it('keeps the repeating name list and the name type', async () => {
    const h = await renderComponent(PatientFormComponent, {
      state: {
        route: {tenantId: 1, facilityId: 2},
        tenant: {
          ...tenantAdapter.setAll([{id: 1, nameDisplay: 'General Hospital'}], tenantInitialState),
          flavors: [{key: 'SINGLENAME', description: 'One name only', hidden: false}],
        },
      },
    });

    const names = h.component.config().find((card) => card.title === 'Name')?.fields[0];

    expect(names?.singleEntry).toBeFalsy();
    expect(names?.itemFields?.map((field) => field.attributeName)).toContain('nameType');
  });
});

describe('PatientFormComponent fill random', () => {
  /**
   * "Fill random" replaces the whole model, including the repeating
   * sections. Those are FormArrays whose controls are rebuilt, so the list
   * views must re-render rather than keep the previous entries.
   */
  async function renderWithRandom(random: Partial<EhrPatient>) {
    const h = await renderComponent(PatientFormComponent, {
      state: {route: {tenantId: 1, facilityId: 2}},
    });
    const patientService = h.fixture.debugElement.injector.get(PatientService);
    spyOn(patientService, 'readRandom').and.returnValue(of(random as EhrPatient));
    return h;
  }

  it('replaces every section of the form, including repeating ones', async () => {
    const h = await renderWithRandom({
      names: [{nameFirst: 'Random', nameLast: 'Person', nameType: 'L'}],
      identifiers: [
        {value: 'RND-111', system: 'urn:oid:9', type: 'MR'},
        {value: 'RND-222', system: 'urn:oid:8', type: 'SS'},
      ],
      sex: 'M',
      birthDate: '2001-05-05' as unknown as Date,
    });

    h.component.fillRandom();
    h.detectChanges();

    const identifiers = h.component.form().get('identifiers') as FormArray;
    expect(identifiers.length).toBe(2);
    expect(identifiers.at(0).get('value')?.value).toBe('RND-111');
    expect(identifiers.at(1).get('value')?.value).toBe('RND-222');
    expect(h.component.form().get('sex')?.value).toBe('M');
  });

  it('renders the generated identifiers in the DOM', async () => {
    const h = await renderWithRandom({
      identifiers: [{value: 'RND-111', system: 'urn:oid:9', type: 'MR'}],
    });

    h.component.fillRandom();
    h.detectChanges();

    const values = h.queryAll<HTMLInputElement>('input').map((input) => input.value);
    expect(values).toContain('RND-111');
  });

  it('discards entries the generated patient does not have', async () => {
    const h = await renderComponent(PatientFormComponent, {
      state: {route: {tenantId: 1, facilityId: 2}},
      inputs: {
        patient: aPatient({
          id: 1,
          identifiers: [{value: 'OLD-1'}, {value: 'OLD-2'}, {value: 'OLD-3'}],
        }),
      },
    });
    const patientService = h.fixture.debugElement.injector.get(PatientService);
    spyOn(patientService, 'readRandom').and.returnValue(
      of({names: [], identifiers: [{value: 'ONLY-ONE'}]} as EhrPatient),
    );
    expect((h.component.form().get('identifiers') as FormArray).length).toBe(3);

    h.component.fillRandom();
    h.detectChanges();

    const identifiers = h.component.form().get('identifiers') as FormArray;
    expect(identifiers.length).toBe(1);
    expect(identifiers.at(0).get('value')?.value).toBe('ONLY-ONE');
    const stale = h.queryAll<HTMLInputElement>('input').map((input) => input.value);
    expect(stale).not.toContain('OLD-1');
  });
});

describe('PatientFormComponent fill random availability', () => {
  function randomButton(h: ComponentHarness<PatientFormComponent>): HTMLButtonElement | undefined {
    return h
      .queryAll<HTMLButtonElement>('button')
      .find((button) => (button.textContent ?? '').includes('Fill random'));
  }

  it('offers Fill random when creating a patient', async () => {
    const h = await renderComponent(PatientFormComponent, {
      state: {route: {tenantId: 1, facilityId: 2}},
    });

    expect(h.component.isCreating()).toBeTrue();
    expect(randomButton(h)).toBeDefined();
  });

  it('hides Fill random when editing an existing patient', async () => {
    resetFixtureIds();
    const h = await renderComponent(PatientFormComponent, {
      state: {route: {tenantId: 1, facilityId: 2}},
      inputs: {patient: aPatient({id: 7})},
    });

    expect(h.component.isCreating()).toBeFalse();
    expect(randomButton(h)).toBeUndefined();
  });
});

describe('PatientFormComponent layout', () => {
  it('gives each repeating field a row of its own', async () => {
    const h = await renderComponent(PatientFormComponent, {
      state: {route: {tenantId: 1, facilityId: 2}},
    });

    const nameCard = h
      .queryAll<HTMLElement>('app-form-card')
      .find((card) => card.querySelector('h3')?.textContent?.trim() === 'Name');

    // Names is a list, so it occupies a list-row; the scalar fields that
    // follow it share a separate field-row.
    expect(nameCard?.querySelectorAll('.list-row').length).toBe(1);
    expect(nameCard?.querySelectorAll('.field-row').length).toBe(1);
  });

  it('widens cards that contain a repeating field', async () => {
    const h = await renderComponent(PatientFormComponent, {
      state: {route: {tenantId: 1, facilityId: 2}},
    });

    const wide = h
      .queryAll<HTMLElement>('app-form-card.card--wide')
      .map((card) => card.querySelector('h3')?.textContent?.trim());

    expect(wide).toContain('Name');
    expect(wide).toContain('Identifiers / Medical record number');
    // Birth holds only scalar fields, so it stays in the responsive grid.
    expect(wide).not.toContain('Birth');
  });
});
