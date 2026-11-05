import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, input, output} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {ButtonModule} from 'primeng/button';
import {TableModule} from 'primeng/table';
import {TagModule} from 'primeng/tag';
import {TooltipModule} from 'primeng/tooltip';
import {VaccinationEvent} from '../../../core/models';
import {
  RecordToolAction,
  RecordToolsComponent,
  VACCINATION_TOOLS,
} from '../../../shared/components/record-tools/record-tools.component';
import {parseApiDate} from '../../../shared/util/date';

export interface VaccinationToolRequest {
  action: RecordToolAction;
  vaccination: VaccinationEvent;
}

/**
 * Flattened row, so each column sorts on the value it displays rather than
 * on a path into the nested `vaccine` object.
 */
interface VaccinationRow {
  vaccination: VaccinationEvent;
  id: number;
  administeredDate: Date | undefined;
  cvx: string;
  lot: string;
  status: string;
  issues: number;
}

@Component({
  selector: 'app-vaccination-summary-table',
  standalone: true,
  imports: [
    DatePipe,
    FormsModule,
    ButtonModule,
    TableModule,
    TagModule,
    TooltipModule,
    RecordToolsComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <p-table
      [value]="rows()"
      [loading]="loading()"
      [scrollable]="true"
      scrollHeight="flex"
      [rowHover]="true"
      dataKey="id"
      sortMode="single"
      [tableStyle]="{ 'min-width': '32rem' }"
    >
      <ng-template #caption>
        <div class="table-toolbar">
          <p-button
            icon="pi pi-plus"
            label="New immunization"
            size="small"
            [disabled]="!canEdit()"
            [pTooltip]="canEdit() ? 'Record an immunization' : 'Select a patient first'"
            tooltipPosition="bottom"
            (onClick)="create.emit()"
          />
          <p-button
            icon="pi pi-sparkles"
            label="Populate"
            size="small"
            [outlined]="true"
            severity="secondary"
            [disabled]="!canPopulate()"
            [pTooltip]="populateTooltip()"
            tooltipPosition="bottom"
            (onClick)="populate.emit()"
          />
        </div>
      </ng-template>

      <ng-template #header>
        <tr>
          <th style="width: 3rem"></th>
          @for (column of columns; track column.field) {
            <th [pSortableColumn]="column.field">
              {{ column.header }}
              <p-sortIcon [field]="column.field" />
            </th>
          }
        </tr>
      </ng-template>

      <ng-template #body let-row let-expanded="expanded">
        <tr>
          <td>
            <p-button
              type="button"
              [pRowToggler]="row"
              [text]="true"
              size="small"
              severity="secondary"
              [icon]="expanded ? 'pi pi-chevron-down' : 'pi pi-chevron-right'"
              ariaLabel="Toggle actions"
            />
          </td>
          <td>{{ row.administeredDate ? (row.administeredDate | date: 'mediumDate') : '—' }}</td>
          <td class="mono">{{ row.cvx || '—' }}</td>
          <td class="mono">{{ row.lot || '—' }}</td>
          <td>{{ row.status || '—' }}</td>
          <td>
            @if (row.issues) {
              <p-tag [value]="row.issues" severity="warn" [rounded]="true" />
            } @else {
              <span class="muted">—</span>
            }
          </td>
        </tr>
      </ng-template>

      <ng-template #expandedrow let-row>
        <tr class="detail-row">
          <td colspan="6">
            <app-record-tools
              [tools]="vaccinationTools"
              (toolSelect)="toolSelect.emit({ action: $event, vaccination: row.vaccination })"
            />
          </td>
        </tr>
      </ng-template>

      <ng-template #emptymessage>
        <tr>
          <td colspan="6" class="empty">
            @if (loading()) {
              Loading immunizations…
            } @else {
              No immunizations recorded for this patient.
            }
          </td>
        </tr>
      </ng-template>
    </p-table>
  `,
  styleUrl: './vaccination-summary-table.component.scss',
})
export class VaccinationSummaryTableComponent {
  readonly vaccinations = input.required<VaccinationEvent[]>();
  readonly loading = input(false);
  /** False until a patient is selected. */
  readonly canEdit = input(false);

  readonly toolSelect = output<VaccinationToolRequest>();
  readonly create = output<void>();
  readonly populate = output<void>();

  readonly vaccinationTools = VACCINATION_TOOLS;

  /** Every column is sortable; `field` names a flat key on VaccinationRow. */
  readonly columns = [
    {field: 'administeredDate', header: 'Administered'},
    {field: 'cvx', header: 'CVX'},
    {field: 'lot', header: 'Lot'},
    {field: 'status', header: 'Status'},
    {field: 'issues', header: 'Issues'},
  ];

  readonly rows = computed<VaccinationRow[]>(() =>
    this.vaccinations().map((vaccination) => ({
      vaccination,
      id: vaccination.id ?? -1,
      administeredDate: parseApiDate(vaccination.vaccine?.administeredDate),
      cvx: vaccination.vaccine?.vaccineCvxCode ?? '',
      lot: vaccination.vaccine?.lotNumber ?? '',
      status: vaccination.vaccine?.completionStatus ?? '',
      issues: vaccination.feedbacksCount ?? 0,
    })),
  );

  canPopulate(): boolean {
    return this.canEdit() && this.vaccinations().length === 0;
  }

  populateTooltip(): string {
    if (!this.canEdit()) {
      return 'Select a patient first';
    }
    return this.vaccinations().length
      ? 'Only available while the patient has no immunizations'
      : 'Populate with random immunizations';
  }
}
