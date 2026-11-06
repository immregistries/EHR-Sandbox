import {ComponentHarness, renderComponent} from '../../../../testing';
import {of} from 'rxjs';
import {VaccinationEvent} from '../../../core/models';
import {VaccinationService} from '../../../core/services';
import {VaccinationFormComponent} from './vaccination-form.component';
import {VACCINATION_FORM_CONFIG} from './vaccination-form.config';

const ROUTE = {tenantId: 1, facilityId: 2, patientId: 3};

describe('VaccinationFormComponent', () => {
  let h: ComponentHarness<VaccinationFormComponent>;

  beforeEach(async () => {
    h = await renderComponent(VaccinationFormComponent, {state: {route: ROUTE}});
  });

  it('renders every configured section in order', () => {
    const titles = h.queryAll<HTMLElement>('app-form-card h3').map((el) => el.textContent?.trim());

    expect(titles).toEqual(VACCINATION_FORM_CONFIG.map((card) => card.title));
    expect(titles.slice(0, 4)).toEqual(['Vaccine', 'Record nature', 'Codes', 'Request']);
  });

  it('requires the fields the old form required', () => {
    expect(h.component.form().get('administeredDate')?.hasError('required')).toBeTrue();
    expect(h.component.form().get('vaccineCvxCode')?.hasError('required')).toBeTrue();
    expect(h.component.form().get('informationSource')?.hasError('required')).toBeTrue();
  });

  it('blocks submission while required fields are empty', () => {
    let saved = 0;
    h.component.saved.subscribe(() => (saved += 1));

    h.component.submit();

    expect(saved).toBe(0);
  });

  it('disables the generated update dates', () => {
    expect(h.component.form().get('createdDate')?.disabled).toBeTrue();
    expect(h.component.form().get('updatedDate')?.disabled).toBeTrue();
  });
});

describe('VaccinationFormComponent record nature', () => {
  /**
   * The old form required every field for a new administration and none for
   * a historical record. That rule is reproduced by lifting automatically.
   */
  it('lifts required rules for a historical record', async () => {
    const h = await renderComponent(VaccinationFormComponent, {state: {route: ROUTE}});

    h.component.form().get('primarySource')?.setValue('false');
    h.detectChanges();

    expect(h.component.isHistorical()).toBeTrue();
    expect(h.component.liftRequired()).toBeTrue();
    expect(h.component.form().get('administeredDate')?.hasError('required')).toBeFalse();
  });

  it('restores them for a new administration', async () => {
    const h = await renderComponent(VaccinationFormComponent, {state: {route: ROUTE}});

    h.component.form().get('primarySource')?.setValue('false');
    h.detectChanges();
    h.component.form().get('primarySource')?.setValue('true');
    h.detectChanges();

    expect(h.component.isHistorical()).toBeFalse();
    expect(h.component.form().get('administeredDate')?.hasError('required')).toBeTrue();
  });
});

describe('VaccinationFormComponent nested vaccine shape', () => {
  it('populates the form from the nested vaccine of the record being edited', async () => {
    const vaccination: VaccinationEvent = {
      id: 4,
      primarySource: true,
      vaccine: {
        administeredDate: '2024-06-01' as unknown as Date,
        vaccineCvxCode: '08',
        lotNumber: 'LOT-123',
      },
    };

    const h = await renderComponent(VaccinationFormComponent, {
      state: {route: ROUTE},
      inputs: {vaccination},
    });

    // Fields of a `path: 'vaccine'` card read through that path.
    expect(h.component.form().get('vaccineCvxCode')?.value).toBe('08');
    expect(h.component.form().get('lotNumber')?.value).toBe('LOT-123');
    // While clinician fields live on the event itself.
    expect(h.component.form().get('primarySource')?.value).toBeTrue();
  });

  it('writes vaccine fields back under `vaccine` on save', async () => {
    const h = await renderComponent(VaccinationFormComponent, {state: {route: ROUTE}});
    h.component.form().patchValue({
      vaccineCvxCode: '207',
      lotNumber: 'LOT-9',
      primarySource: 'true',
    });

    const value = h.component['engine'].toEntity<VaccinationEvent>(
      h.component.form(),
      VACCINATION_FORM_CONFIG,
    ) as Record<string, unknown>;
    const vaccine = value['vaccine'] as Record<string, unknown>;

    expect(vaccine['vaccineCvxCode']).toBe('207');
    expect(vaccine['lotNumber']).toBe('LOT-9');
    // Not flattened onto the event.
    expect(value['vaccineCvxCode']).toBeUndefined();
    expect(value['primarySource']).toBe('true');
  });
});

describe('VaccinationFormComponent fill random', () => {
  /**
   * "Fill random" replaces the whole record. Most vaccination fields live
   * under the nested `vaccine`, so the controls must repopulate from that
   * path — and the new values must actually reach the DOM, not just the
   * FormGroup.
   */
  const RANDOM: VaccinationEvent = {
    primarySource: true,
    vaccine: {
      administeredDate: '2024-06-01' as unknown as Date,
      administeredAmount: '0.5',
      vaccineCvxCode: '207',
      vaccineMvxCode: 'MSD',
      lotNumber: 'RND-LOT-9',
      expirationDate: '2026-01-01' as unknown as Date,
      completionStatus: 'CP',
      bodyRoute: 'IM',
      bodySite: 'LA',
    },
  };

  async function renderWithRandom(random: VaccinationEvent, initial?: VaccinationEvent) {
    const h = await renderComponent(VaccinationFormComponent, {
      state: {route: ROUTE},
      ...(initial ? {inputs: {vaccination: initial}} : {}),
    });
    const service = h.fixture.debugElement.injector.get(VaccinationService);
    spyOn(service, 'readRandom').and.returnValue(of(random));
    return h;
  }

  function inputValues(h: ComponentHarness<VaccinationFormComponent>): string[] {
    return h.queryAll<HTMLInputElement>('input').map((input) => input.value);
  }

  it('replaces the nested vaccine fields', async () => {
    const h = await renderWithRandom(RANDOM);

    h.component.fillRandom();
    h.detectChanges();

    expect(h.component.form().get('vaccineCvxCode')?.value).toBe('207');
    expect(h.component.form().get('lotNumber')?.value).toBe('RND-LOT-9');
    expect(h.component.form().get('administeredAmount')?.value).toBe('0.5');
  });

  it('shows the generated values in the rendered inputs', async () => {
    const h = await renderWithRandom(RANDOM);

    h.component.fillRandom();
    h.detectChanges();

    // The DOM, not just the FormGroup: a control can update while the view
    // still shows the previous value.
    const values = inputValues(h);
    expect(values).toContain('RND-LOT-9');
    expect(values).toContain('0.5');
  });

  it('replaces values already present when editing', async () => {
    const existing: VaccinationEvent = {
      id: 4,
      primarySource: true,
      vaccine: {lotNumber: 'OLD-LOT', administeredAmount: '1.0', vaccineCvxCode: '08'},
    };
    const h = await renderWithRandom(RANDOM, existing);
    expect(inputValues(h)).toContain('OLD-LOT');

    h.component.fillRandom();
    h.detectChanges();

    const values = inputValues(h);
    expect(values).toContain('RND-LOT-9');
    expect(values).not.toContain('OLD-LOT');
  });

  it('clears fields the generated record does not have', async () => {
    const existing: VaccinationEvent = {
      id: 4,
      primarySource: true,
      vaccine: {lotNumber: 'OLD-LOT', administeredAmount: '1.0'},
    };
    const sparse: VaccinationEvent = {primarySource: true, vaccine: {lotNumber: 'ONLY-LOT'}};
    const h = await renderWithRandom(sparse, existing);

    h.component.fillRandom();
    h.detectChanges();

    expect(h.component.form().get('lotNumber')?.value).toBe('ONLY-LOT');
    expect(h.component.form().get('administeredAmount')?.value).toBeNull();
    expect(inputValues(h)).not.toContain('1.0');
  });

  it('updates code-backed selects too', async () => {
    const h = await renderWithRandom(RANDOM);

    h.component.fillRandom();
    h.detectChanges();

    expect(h.component.form().get('bodyRoute')?.value).toBe('IM');
    expect(h.component.form().get('completionStatus')?.value).toBe('CP');
  });

  it('requests a random record for the patient in the url', async () => {
    const h = await renderWithRandom(RANDOM);
    const service = h.fixture.debugElement.injector.get(VaccinationService);

    h.component.fillRandom();

    expect(service.readRandom).toHaveBeenCalledWith(1, 2, 3);
  });

  it('leaves the saved payload nested under vaccine', async () => {
    const h = await renderWithRandom(RANDOM);

    h.component.fillRandom();
    h.detectChanges();

    const value = h.component['engine'].toEntity<VaccinationEvent>(
      h.component.form(),
      VACCINATION_FORM_CONFIG,
    ) as Record<string, unknown>;

    expect((value['vaccine'] as Record<string, unknown>)['lotNumber']).toBe('RND-LOT-9');
    expect(value['lotNumber']).toBeUndefined();
  });
});

describe('VaccinationFormComponent date fields', () => {
  const WITH_DATES: VaccinationEvent = {
    id: 11,
    primarySource: true,
    vaccine: {
      administeredDate: '2024-06-01' as unknown as Date,
      expirationDate: '2026-01-15' as unknown as Date,
    },
  };

  /**
   * p-datepicker renders nothing for a string, so the control must hold a
   * Date — the input looked blank even though the value was set.
   */
  it('shows the dates of the record being edited', async () => {
    const h = await renderComponent(VaccinationFormComponent, {
      state: {route: ROUTE},
      inputs: {vaccination: WITH_DATES},
    });

    const administered = h.component.form().get('administeredDate')?.value;
    expect(administered instanceof Date).toBeTrue();
    expect((administered as Date).getMonth()).toBe(5);
    expect(h.component.form().get('expirationDate')?.value instanceof Date).toBeTrue();
  });

  it('shows generated dates after fill random', async () => {
    const h = await renderComponent(VaccinationFormComponent, {state: {route: ROUTE}});
    const service = h.fixture.debugElement.injector.get(VaccinationService);
    spyOn(service, 'readRandom').and.returnValue(
      of({
        primarySource: true,
        vaccine: {administeredDate: '2023-03-09', expirationDate: '2025-11-20'},
      } as unknown as VaccinationEvent),
    );

    h.component.fillRandom();
    h.detectChanges();

    const administered = h.component.form().get('administeredDate')?.value;
    expect(administered instanceof Date).toBeTrue();
    expect((administered as Date).getDate()).toBe(9);
    expect(h.component.form().get('expirationDate')?.value instanceof Date).toBeTrue();
  });

  it('renders the date in the datepicker input', async () => {
    const h = await renderComponent(VaccinationFormComponent, {
      state: {route: ROUTE},
      inputs: {vaccination: WITH_DATES},
    });

    const shown = h
      .queryAll<HTMLInputElement>('p-datepicker input')
      .map((input) => input.value)
      .filter(Boolean);

    expect(shown).toContain('2024-06-01');
  });
});

describe('VaccinationFormComponent clinician references', () => {
  /**
   * The API returns clinicians expanded and expects an entity back: a bare
   * `{id}` stub is an unresolvable detached entity that fails to save, and
   * the backend distinguishes "existing" from "create me" by whether `id`
   * is set. So the form holds the whole clinician.
   */
  const WITH_CLINICIANS: VaccinationEvent = {
    id: 11,
    primarySource: true,
    enteringClinician: {id: 9, nameFirst: 'Grace', nameLast: 'Hopper'},
    administeringClinician: {id: 10, nameFirst: 'Alan', nameLast: 'Turing'},
    vaccine: {vaccineCvxCode: '08'},
  };

  it('keeps the whole clinician on the control, not just its id', async () => {
    const h = await renderComponent(VaccinationFormComponent, {
      state: {route: ROUTE},
      inputs: {vaccination: WITH_CLINICIANS},
    });

    const entering = h.component.form().get('enteringClinician')?.value;
    expect(entering.id).toBe(9);
    expect(entering.nameLast).toBe('Hopper');
  });

  it('normalises a reference sent as a bare id', async () => {
    const h = await renderComponent(VaccinationFormComponent, {
      state: {route: ROUTE},
      inputs: {
        vaccination: {
          id: 12,
          enteringClinician: 9 as unknown as VaccinationEvent['enteringClinician'],
          vaccine: {},
        },
      },
    });

    expect(h.component.form().get('enteringClinician')?.value).toEqual({id: 9});
  });

  it('leaves an absent clinician null', async () => {
    const h = await renderComponent(VaccinationFormComponent, {
      state: {route: ROUTE},
      inputs: {vaccination: WITH_CLINICIANS},
    });

    expect(h.component.form().get('orderingClinician')?.value).toBeNull();
  });

  it('sends the full clinician back, so the backend can resolve it', async () => {
    const h = await renderComponent(VaccinationFormComponent, {
      state: {route: ROUTE},
      inputs: {vaccination: WITH_CLINICIANS},
    });

    const value = h.component['engine'].toEntity<VaccinationEvent>(
      h.component.form(),
      VACCINATION_FORM_CONFIG,
    ) as Record<string, unknown>;
    const entering = value['enteringClinician'] as Record<string, unknown>;

    expect(entering['id']).toBe(9);
    // An `{id}`-only stub fails to save: the entity must carry its fields.
    expect(entering['nameLast']).toBe('Hopper');
    expect(value['orderingClinician']).toBeUndefined();
  });

  it('does not leak the display label into the saved payload', async () => {
    const h = await renderComponent(VaccinationFormComponent, {
      state: {route: ROUTE},
      inputs: {vaccination: WITH_CLINICIANS},
    });
    // An option carries a `label` for the select; it is not part of the API
    // entity and must not be sent.
    h.component.form().get('enteringClinician')?.setValue({
      id: 9,
      nameFirst: 'Grace',
      nameLast: 'Hopper',
      label: 'Grace Hopper',
    });

    const value = h.component['engine'].toEntity<VaccinationEvent>(
      h.component.form(),
      VACCINATION_FORM_CONFIG,
    ) as Record<string, unknown>;

    expect((value['enteringClinician'] as Record<string, unknown>)['label']).toBeUndefined();
  });
});
