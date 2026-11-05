import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, input, output} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {ButtonModule} from 'primeng/button';
import {IconFieldModule} from 'primeng/iconfield';
import {InputIconModule} from 'primeng/inputicon';
import {InputTextModule} from 'primeng/inputtext';
import {TableModule} from 'primeng/table';
import {TagModule} from 'primeng/tag';
import {TooltipModule} from 'primeng/tooltip';
import {EhrPatient} from '../../../core/models';
import {
  PATIENT_TOOLS,
  RecordToolAction,
  RecordToolsComponent,
} from '../../../shared/components/record-tools/record-tools.component';
import {patientDisplayName} from '../../../shared/pipes/patient-name.pipe';
import {parseApiDate} from '../../../shared/util/date';

export interface PatientToolRequest {
  action: RecordToolAction;
  patient: EhrPatient;
}

/**
 * Flattened row. Sorting and filtering operate on these fields rather than on
 * the nested patient, so every column sorts on exactly the value displayed —
 * `names` is an array whose preferred ('L') entry is not necessarily first.
 */
interface PatientRow {
  patient: EhrPatient;
  id: number;
  name: string;
  birthDate: Date | undefined;
  sex: string;
  mrn: string;
  issues: number;
}

@Component({
  selector: 'app-patient-summary-table',
  standalone: true,
  imports: [
    DatePipe,
    FormsModule,
    ButtonModule,
    IconFieldModule,
    InputIconModule,
    InputTextModule,
    TableModule,
    TagModule,
    TooltipModule,
    RecordToolsComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <p-table
      #table
      [value]="rows()"
      [loading]="loading()"
      [scrollable]="true"
      scrollHeight="flex"
      [rowHover]="true"
      dataKey="id"
      sortMode="single"
      [globalFilterFields]="globalFilterFields"
      [tableStyle]="{ 'min-width': '32rem' }"
    >
      <ng-template #caption>
        <div class="table-toolbar">
          <p-button
            icon="pi pi-plus"
            label="New patient"
            size="small"
            [disabled]="!canEdit()"
            [pTooltip]="canEdit() ? 'Create a patient' : 'Select a facility first'"
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
          <span class="toolbar-spacer"></span>
          <p-iconfield iconPosition="left">
            <p-inputicon styleClass="pi pi-search" />
            <input
              pInputText
              type="search"
              placeholder="Search patients"
              [(ngModel)]="search"
              (ngModelChange)="table.filterGlobal($event, 'contains')"
            />
          </p-iconfield>
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
        <tr
          class="patient-row"
          [class.row--selected]="row.id === selectedId()"
          (click)="patientSelect.emit(row.patient)"
        >
          <td>
            <p-button
              type="button"
              [pRowToggler]="row"
              [text]="true"
              size="small"
              severity="secondary"
              [icon]="expanded ? 'pi pi-chevron-down' : 'pi pi-chevron-right'"
              ariaLabel="Toggle actions"
              (click)="$event.stopPropagation()"
            />
          </td>
          <td>{{ row.name || 'Unnamed patient' }}</td>
          <td>{{ row.birthDate ? (row.birthDate | date: 'mediumDate') : '—' }}</td>
          <td>{{ row.sex || '—' }}</td>
          <td class="mono">{{ row.mrn || '—' }}</td>
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
              [tools]="patientTools"
              (toolSelect)="toolSelect.emit({ action: $event, patient: row.patient })"
            />
          </td>
        </tr>
      </ng-template>

      <ng-template #emptymessage>
        <tr>
          <td colspan="6" class="empty">
            @if (loading()) {
              Loading patients…
            } @else {
              No patients in this facility yet.
            }
          </td>
        </tr>
      </ng-template>
    </p-table>
  `,
  styleUrl: './patient-summary-table.component.scss',
})
export class PatientSummaryTableComponent {
  readonly patients = input.required<EhrPatient[]>();
  readonly loading = input(false);
  readonly selectedId = input(-1);
  /** False until a facility is in the url — create/populate need one. */
  readonly canEdit = input(false);

  readonly patientSelect = output<EhrPatient>();
  readonly toolSelect = output<PatientToolRequest>();
  readonly create = output<void>();
  readonly populate = output<void>();

  readonly patientTools = PATIENT_TOOLS;

  /** Every column is sortable; `field` names a flat key on PatientRow. */
  readonly columns = [
    {field: 'name', header: 'Name'},
    {field: 'birthDate', header: 'Birth date'},
    {field: 'sex', header: 'Sex'},
    {field: 'mrn', header: 'MRN'},
    {field: 'issues', header: 'Issues'},
  ];

  readonly globalFilterFields = ['name', 'sex', 'mrn'];

  search = '';

  readonly rows = computed<PatientRow[]>(() =>
    this.patients().map((patient) => ({
      patient,
      id: patient.id ?? -1,
      name: patientDisplayName(patient),
      birthDate: parseApiDate(patient.birthDate),
      sex: patient.sex ?? '',
      mrn: patient.identifiers?.[0]?.value ?? '',
      issues: patient.feedbacksCount ?? 0,
    })),
  );

  /** Populate seeds an empty facility, so it is offered only when empty. */
  canPopulate(): boolean {
    return this.canEdit() && this.patients().length === 0;
  }

  populateTooltip(): string {
    if (!this.canEdit()) {
      return 'Select a facility first';
    }
    return this.patients().length
      ? 'Only available while the facility is empty'
      : 'Populate with random patients';
  }
}
