import {aPatient, ComponentHarness, renderComponent, resetFixtureIds} from '../../../../testing';
import {PatientSummaryTableComponent} from './patient-summary-table.component';

describe('PatientSummaryTableComponent', () => {
  const patients = [aPatient({id: 1}), aPatient({id: 2})];

  function toolbarButton(
    h: ComponentHarness<PatientSummaryTableComponent>,
    label: string,
  ): HTMLButtonElement | undefined {
    return h
      .queryAll<HTMLButtonElement>('p-button button')
      .find((button) => (button.textContent ?? '').includes(label));
  }

  beforeEach(() => resetFixtureIds());

  it('disables create and populate until a facility is selected', async () => {
    const h = await renderComponent(PatientSummaryTableComponent, {
      inputs: {patients: [], canEdit: false},
    });

    expect(toolbarButton(h, 'New patient')?.disabled).toBeTrue();
    expect(toolbarButton(h, 'Populate')?.disabled).toBeTrue();
  });

  it('enables create once a facility is selected', async () => {
    const h = await renderComponent(PatientSummaryTableComponent, {
      inputs: {patients, canEdit: true},
    });

    expect(toolbarButton(h, 'New patient')?.disabled).toBeFalse();
  });

  it('offers populate while the facility is empty', async () => {
    const h = await renderComponent(PatientSummaryTableComponent, {
      inputs: {patients: [], canEdit: true},
    });

    expect(h.component.canPopulate()).toBeTrue();
  });

  it('withdraws populate once the facility has patients', async () => {
    const h = await renderComponent(PatientSummaryTableComponent, {
      inputs: {patients, canEdit: true},
    });

    expect(h.component.canPopulate()).toBeFalse();
    expect(h.component.populateTooltip()).toContain('empty');
  });

  it('emits create and populate requests', async () => {
    const h = await renderComponent(PatientSummaryTableComponent, {
      inputs: {patients: [], canEdit: true},
    });
    let created = 0;
    let populated = 0;
    h.component.create.subscribe(() => (created += 1));
    h.component.populate.subscribe(() => (populated += 1));

    toolbarButton(h, 'New patient')?.click();
    toolbarButton(h, 'Populate')?.click();

    expect(created).toBe(1);
    expect(populated).toBe(1);
  });

  it('emits the selected patient when a row is clicked', async () => {
    const h = await renderComponent(PatientSummaryTableComponent, {
      inputs: {patients, canEdit: true},
    });
    const selected: number[] = [];
    h.component.patientSelect.subscribe((patient) => selected.push(patient.id ?? -1));

    h.queryAll<HTMLElement>('tr.patient-row')[1]?.click();

    expect(selected).toEqual([2]);
  });

  it('exposes the full patient tool set in an expanded row', async () => {
    const h = await renderComponent(PatientSummaryTableComponent, {
      inputs: {patients, canEdit: true},
    });

    h.queryAll<HTMLButtonElement>('button[aria-label="Toggle actions"]')[0]?.click();
    h.detectChanges();

    const labels = h
      .queryAll<HTMLElement>('app-record-tools button')
      .map((button) => button.getAttribute('aria-label'));

    expect(labels).toEqual([
      'Open',
      'Edit',
      'HL7v2',
      'FHIR',
      'IPS',
      'Local facility',
      'Remote history',
      'JSON',
    ]);
  });

  it('emits a tool request carrying the row it came from', async () => {
    const h = await renderComponent(PatientSummaryTableComponent, {
      inputs: {patients, canEdit: true},
    });
    const requests: string[] = [];
    h.component.toolSelect.subscribe((request) =>
      requests.push(`${request.action}:${request.patient.id}`),
    );

    h.queryAll<HTMLButtonElement>('button[aria-label="Toggle actions"]')[0]?.click();
    h.detectChanges();
    h.queryAll<HTMLButtonElement>('app-record-tools button')[1]?.click();

    expect(requests).toEqual(['edit:1']);
  });
});
