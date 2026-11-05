import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, input, output} from '@angular/core';
import {TableModule, TableRowSelectEvent} from 'primeng/table';
import {TagModule} from 'primeng/tag';
import {EhrPatient} from '../../../core/models';
import {PatientNamePipe} from '../../../shared/pipes/patient-name.pipe';

@Component({
  selector: 'app-patient-summary-table',
  standalone: true,
  imports: [DatePipe, TableModule, TagModule, PatientNamePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <p-table
      [value]="patients()"
      [loading]="loading()"
      [scrollable]="true"
      scrollHeight="flex"
      [rowHover]="true"
      dataKey="id"
      selectionMode="single"
      [tableStyle]="{ 'min-width': '28rem' }"
      (onRowSelect)="onRowSelect($event)"
    >
      <ng-template #header>
        <tr>
          <th>Name</th>
          <th>Birth date</th>
          <th>Sex</th>
          <th>MRN</th>
          <th>Issues</th>
        </tr>
      </ng-template>

      <ng-template #body let-patient>
        <tr [pSelectableRow]="patient" [class.row--selected]="patient.id === selectedId()">
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

      <ng-template #emptymessage>
        <tr>
          <td colspan="5" class="empty">No patients in this facility yet.</td>
        </tr>
      </ng-template>
    </p-table>
  `,
  styles: `
    .mono {
      font-family: var(--font-family-mono, monospace);
      font-size: 0.78rem;
    }
    .muted,
    .empty {
      color: var(--app-text-muted);
    }
    .empty {
      text-align: center;
      padding: 1.5rem;
      font-size: 0.82rem;
    }
    .row--selected {
      background: var(--app-surface-alt);
    }
  `,
})
export class PatientSummaryTableComponent {
  readonly patients = input.required<EhrPatient[]>();
  readonly loading = input(false);
  readonly selectedId = input(-1);

  readonly patientSelect = output<EhrPatient>();

  /** p-table types onRowSelect's data loosely; narrow it to a single row. */
  onRowSelect(event: TableRowSelectEvent): void {
    const patient = event.data as EhrPatient | undefined;
    if (patient) {
      this.patientSelect.emit(patient);
    }
  }
}
