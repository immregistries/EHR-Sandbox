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

describe('PatientSummaryTableComponent rows', () => {
  it('flattens every column so each sorts on the value displayed', async () => {
    const h = await renderComponent(PatientSummaryTableComponent, {
      inputs: {
        patients: [
          aPatient({
            id: 7,
            // The preferred ('L') name is deliberately not the first entry.
            names: [
              {nameFirst: 'Nick', nameLast: 'Zulu', nameType: 'N'},
              {nameFirst: 'Ada', nameLast: 'Lovelace', nameType: 'L'},
            ],
            birthDate: '1985-12-09' as unknown as Date,
            sex: 'F',
            identifiers: [{value: 'MRN-042'}],
            feedbacksCount: 3,
          }),
        ],
        canEdit: true,
      },
    });

    const [row] = h.component.rows();

    expect(row.name).toBe('Ada Lovelace');
    expect(row.sex).toBe('F');
    expect(row.mrn).toBe('MRN-042');
    expect(row.issues).toBe(3);
    // Date-only values must not drift to the previous day.
    expect(row.birthDate?.getDate()).toBe(9);
  });

  it('declares a sortable field for every visible column', async () => {
    const h = await renderComponent(PatientSummaryTableComponent, {
      inputs: {patients: [aPatient({id: 1})], canEdit: true},
    });

    const headers = h.queryAll<HTMLElement>('thead th');
    // PrimeNG marks a pSortableColumn header with this class and aria-sort.
    const sortable = h.queryAll<HTMLElement>('thead th.p-datatable-sortable-column');

    // Every header except the row-toggle spacer is sortable.
    expect(sortable.length).toBe(headers.length - 1);
    expect(sortable.every((th) => th.hasAttribute('aria-sort'))).toBeTrue();
    expect(h.component.columns.map((column) => column.field)).toEqual([
      'name',
      'birthDate',
      'sex',
      'mrn',
      'issues',
    ]);
  });
});

describe('PatientSummaryTableComponent sorting', () => {
  /** Unsorted on every column, so each sort is observable. */
  const unsorted = [
    aPatient({
      id: 1,
      names: [{nameFirst: 'Zoe', nameLast: 'Zulu', nameType: 'L'}],
      birthDate: '1990-03-12' as unknown as Date,
      sex: 'F',
      identifiers: [{value: 'MRN-003'}],
      feedbacksCount: 2,
    }),
    aPatient({
      id: 2,
      names: [{nameFirst: 'Ada', nameLast: 'Lovelace', nameType: 'L'}],
      birthDate: '1985-12-09' as unknown as Date,
      sex: 'M',
      identifiers: [{value: 'MRN-001'}],
      feedbacksCount: 5,
    }),
    aPatient({
      id: 3,
      names: [{nameFirst: 'Mia', nameLast: 'Mendez', nameType: 'L'}],
      birthDate: '2001-06-23' as unknown as Date,
      sex: 'F',
      identifiers: [{value: 'MRN-002'}],
      feedbacksCount: 0,
    }),
  ];

  function columnText(
    h: ComponentHarness<PatientSummaryTableComponent>,
    index: number,
  ): string[] {
    return h
      .queryAll<HTMLElement>('tr.patient-row')
      .map((tr) => (tr.children[index]?.textContent ?? '').trim());
  }

  function clickHeader(h: ComponentHarness<PatientSummaryTableComponent>, label: string): void {
    const header = h
      .queryAll<HTMLElement>('thead th')
      .find((th) => (th.textContent ?? '').trim().startsWith(label));
    header?.click();
    h.detectChanges();
  }

  it('reorders rows ascending then descending on every column', async () => {
    const h = await renderComponent(PatientSummaryTableComponent, {
      inputs: {patients: unsorted, canEdit: true},
    });

    // [header label, column index in the row]
    const columns: [string, number][] = [
      ['Name', 1],
      ['Birth date', 2],
      ['Sex', 3],
      ['MRN', 4],
      ['Issues', 5],
    ];

    for (const [label, index] of columns) {
      clickHeader(h, label);
      const ascending = columnText(h, index);

      clickHeader(h, label);
      const descending = columnText(h, index);

      expect(ascending.length)
        .withContext(`${label}: rows rendered`)
        .toBe(unsorted.length);
      expect(descending)
        .withContext(`${label}: second click reverses the order`)
        .toEqual([...ascending].reverse());

      // Reset to unsorted before exercising the next column.
      clickHeader(h, label);
    }
  });

  it('sorts names by the preferred entry, not the first in the array', async () => {
    const h = await renderComponent(PatientSummaryTableComponent, {
      inputs: {
        patients: [
          aPatient({
            id: 1,
            // First entry sorts last; preferred entry sorts first.
            names: [
              {nameFirst: 'Zed', nameLast: 'Zulu', nameType: 'N'},
              {nameFirst: 'Ada', nameLast: 'Lovelace', nameType: 'L'},
            ],
          }),
          aPatient({id: 2, names: [{nameFirst: 'Bob', nameLast: 'Brown', nameType: 'L'}]}),
        ],
        canEdit: true,
      },
    });

    clickHeader(h, 'Name');

    expect(columnText(h, 1)).toEqual(['Ada Lovelace', 'Bob Brown']);
  });
});
