import {aVaccinationEvent, renderComponent, resetFixtureIds} from '../../../../testing';
import {VaccinationSummaryTableComponent} from './vaccination-summary-table.component';

describe('VaccinationSummaryTableComponent', () => {
  beforeEach(() => resetFixtureIds());

  it('flattens every column so each sorts on the value displayed', async () => {
    const h = await renderComponent(VaccinationSummaryTableComponent, {
      inputs: {
        vaccinations: [
          aVaccinationEvent({
            id: 4,
            vaccine: {
              administeredDate: '2024-06-01' as unknown as Date,
              vaccineCvxCode: '08',
              lotNumber: 'LOT-123',
              completionStatus: 'CP',
            },
            feedbacksCount: 2,
          }),
        ],
        canEdit: true,
      },
    });

    const [row] = h.component.rows();

    expect(row.cvx).toBe('08');
    expect(row.lot).toBe('LOT-123');
    expect(row.status).toBe('CP');
    expect(row.issues).toBe(2);
    // Date-only values must not drift to the previous day.
    expect(row.administeredDate?.getDate()).toBe(1);
  });

  it('declares a sortable field for every visible column', async () => {
    const h = await renderComponent(VaccinationSummaryTableComponent, {
      inputs: {vaccinations: [aVaccinationEvent({id: 1})], canEdit: true},
    });

    const headers = h.queryAll<HTMLElement>('thead th');
    // PrimeNG marks a pSortableColumn header with this class and aria-sort.
    const sortable = h.queryAll<HTMLElement>('thead th.p-datatable-sortable-column');

    expect(sortable.length).toBe(headers.length - 1);
    expect(sortable.every((th) => th.hasAttribute('aria-sort'))).toBeTrue();
    expect(h.component.columns.map((column) => column.field)).toEqual([
      'administeredDate',
      'cvx',
      'lot',
      'status',
      'issues',
    ]);
  });

  it('withdraws populate once the patient has immunizations', async () => {
    const h = await renderComponent(VaccinationSummaryTableComponent, {
      inputs: {vaccinations: [aVaccinationEvent({id: 1})], canEdit: true},
    });

    expect(h.component.canPopulate()).toBeFalse();
    expect(h.component.populateTooltip()).toContain('no immunizations');
  });

  it('emits a tool request carrying the row it came from', async () => {
    const h = await renderComponent(VaccinationSummaryTableComponent, {
      inputs: {vaccinations: [aVaccinationEvent({id: 11})], canEdit: true},
    });
    const requests: string[] = [];
    h.component.toolSelect.subscribe((request) =>
      requests.push(`${request.action}:${request.vaccination.id}`),
    );

    h.queryAll<HTMLButtonElement>('button[aria-label="Toggle actions"]')[0]?.click();
    h.detectChanges();
    h.queryAll<HTMLButtonElement>('app-record-tools button')[0]?.click();

    expect(requests).toEqual(['edit:11']);
  });
});
