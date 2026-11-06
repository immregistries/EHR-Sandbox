import {aClinician, ComponentHarness, renderComponent, resetFixtureIds} from '../../../../testing';
import {ClinicianActions} from '../../../core/store/clinician/clinician.actions';
import {adapter, initialState} from '../../../core/store/clinician/clinician.reducer';
import {ClinicianListComponent} from './clinician-list.component';

describe('ClinicianListComponent', () => {
  const clinicians = [
    aClinician({
      id: 9,
      namePrefix: 'Dr',
      nameFirst: 'Grace',
      nameLast: 'Hopper',
      qualification: 'MD',
      identifiers: [{value: 'NPI-111', type: 'NPI'}],
      phones: [{number: '555-0100', use: 'WP'}],
    }),
    aClinician({id: 10, nameFirst: 'Alan', nameLast: 'Turing', identifiers: [], phones: []}),
  ];

  async function render(route: Record<string, number> = {tenantId: 3}) {
    return renderComponent(ClinicianListComponent, {
      state: {route, clinician: adapter.setAll(clinicians, initialState)},
    });
  }

  function rows(h: ComponentHarness<ClinicianListComponent>): HTMLElement[] {
    return h.queryAll<HTMLElement>('tr.clinician-row');
  }

  beforeEach(() => resetFixtureIds());

  it('lists the clinicians of the tenant in the url', async () => {
    const h = await render();

    expect(rows(h).length).toBe(2);
    expect(rows(h)[0].textContent).toContain('Dr Grace Hopper');
  });

  it('summarises qualification, identifier and contact', async () => {
    const h = await render();

    const [first] = h.component.rows();
    expect(first.qualification).toBe('MD');
    expect(first.identifier).toBe('NPI-111');
    expect(first.contact).toBe('555-0100');
  });

  it('leaves missing details blank rather than showing undefined', async () => {
    const h = await render();

    const [, second] = h.component.rows();
    expect(second.identifier).toBe('');
    expect(second.contact).toBe('');
  });

  it('declares a sortable field for every data column', async () => {
    const h = await render();

    const sortable = h.queryAll<HTMLElement>('thead th.p-datatable-sortable-column');

    expect(sortable.length).toBe(h.component.columns.length);
  });

  it('opens an empty form for a new clinician', async () => {
    const h = await render();

    h.component.openCreate();
    h.detectChanges();

    expect(h.component.formOpen()).toBeTrue();
    expect(h.component.editing()).toBeUndefined();
  });

  it('opens the form on the clinician whose row is clicked', async () => {
    const h = await render();

    rows(h)[0]?.click();
    h.detectChanges();

    expect(h.component.formOpen()).toBeTrue();
    expect(h.component.editing()?.id).toBe(9);
  });

  it('closes the form and refreshes after a save', async () => {
    const h = await render();
    h.component.openCreate();
    const dispatched: { type: string }[] = [];
    spyOn(h.store, 'dispatch').and.callFake(((action: { type: string }) => {
      dispatched.push(action);
    }) as never);

    h.component.onSaved();

    expect(h.component.formOpen()).toBeFalse();
    expect(dispatched.map((a) => a.type)).toContain(ClinicianActions.refresh.type);
  });
});
