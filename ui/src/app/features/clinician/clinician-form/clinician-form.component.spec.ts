import {FormArray} from '@angular/forms';
import {aClinician, ComponentHarness, renderComponent, resetFixtureIds} from '../../../../testing';
import {ClinicianFormComponent} from './clinician-form.component';
import {CLINICIAN_FORM_CONFIG} from './clinician-form.config';

describe('ClinicianFormComponent', () => {
  let h: ComponentHarness<ClinicianFormComponent>;

  beforeEach(async () => {
    resetFixtureIds();
    h = await renderComponent(ClinicianFormComponent, {state: {route: {tenantId: 1}}});
  });

  it('renders every configured section in order', () => {
    const titles = h.queryAll<HTMLElement>('app-form-card h3').map((el) => el.textContent?.trim());

    expect(titles).toEqual(CLINICIAN_FORM_CONFIG.map((card) => card.title));
    expect(titles).toEqual([
      'Clinician name',
      'Qualification',
      'Contact',
      'Addresses',
      'Identifiers',
    ]);
  });

  it('builds a repeating group for each list section', () => {
    expect(h.component.form().get('phones') instanceof FormArray).toBeTrue();
    expect(h.component.form().get('addresses') instanceof FormArray).toBeTrue();
    expect(h.component.form().get('identifiers') instanceof FormArray).toBeTrue();
  });

  it('reads the qualification from the bundled code system', () => {
    const field = CLINICIAN_FORM_CONFIG.find((card) => card.title === 'Qualification')?.fields[0];

    // HL7 table 0360 is a CodeSystem, not a codemap entry.
    expect(field?.codeSystem).toBe('qualification');
    expect(field?.codeMapLabel).toBeUndefined();
  });

  it('requires nothing, matching the old form', () => {
    expect(h.component.form().valid).toBeTrue();
  });
});

describe('ClinicianFormComponent when editing', () => {
  it('populates the form from the clinician being edited', async () => {
    resetFixtureIds();
    const clinician = aClinician({
      id: 9,
      nameFirst: 'Grace',
      nameLast: 'Hopper',
      qualification: 'MD',
      identifiers: [{value: 'NPI-1', type: 'NPI'}],
    });

    const h = await renderComponent(ClinicianFormComponent, {
      state: {route: {tenantId: 1}},
      inputs: {clinician},
    });

    expect(h.component.form().get('nameLast')?.value).toBe('Hopper');
    expect(h.component.form().get('qualification')?.value).toBe('MD');
    expect((h.component.form().get('identifiers') as FormArray).length).toBe(1);
    expect(h.component.isCreating()).toBeFalse();
  });
});
