import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, input, output} from '@angular/core';
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
import {PatientNamePipe} from '../../../shared/pipes/patient-name.pipe';

export interface PatientToolRequest {
  action: RecordToolAction;
  patient: EhrPatient;
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
    PatientNamePipe,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <p-table
      #table
      [value]="patients()"
      [loading]="loading()"
      [scrollable]="true"
      scrollHeight="flex"
      [rowHover]="true"
      dataKey="id"
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
          <th pSortableColumn="names.0.nameLast">Name <p-sortIcon field="names.0.nameLast" /></th>
          <th pSortableColumn="birthDate">Birth date <p-sortIcon field="birthDate" /></th>
          <th>Sex</th>
          <th>MRN</th>
          <th>Issues</th>
        </tr>
      </ng-template>

      <ng-template #body let-patient let-expanded="expanded">
        <tr
          class="patient-row"
          [class.row--selected]="patient.id === selectedId()"
          (click)="patientSelect.emit(patient)"
        >
          <td>
            <p-button
              type="button"
              [pRowToggler]="patient"
              [text]="true"
              size="small"
              severity="secondary"
              [icon]="expanded ? 'pi pi-chevron-down' : 'pi pi-chevron-right'"
              ariaLabel="Toggle actions"
              (click)="$event.stopPropagation()"
            />
          </td>
          <td>{{ patient | patientName }}</td>
          <td>{{ patient.birthDate | date: 'mediumDate' }}</td>
          <td>{{ patient.sex ?? '—' }}</td>
          <td class="mono">{{ patient.identifiers?.[0]?.value ?? '—' }}</td>
          <td>
            @if (patient.feedbacksCount) {
              <p-tag [value]="patient.feedbacksCount" severity="warn" [rounded]="true" />
            } @else {
              <span class="muted">—</span>
            }
          </td>
        </tr>
      </ng-template>

      <ng-template #expandedrow let-patient>
        <tr class="detail-row">
          <td colspan="6">
            <app-record-tools
              [tools]="patientTools"
              (toolSelect)="toolSelect.emit({ action: $event, patient })"
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
  readonly globalFilterFields = ['names.0.nameFirst', 'names.0.nameLast', 'sex'];

  search = '';

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
