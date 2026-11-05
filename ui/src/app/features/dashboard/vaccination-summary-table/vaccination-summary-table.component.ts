import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, input} from '@angular/core';
import {TableModule} from 'primeng/table';
import {TagModule} from 'primeng/tag';
import {VaccinationEvent} from '../../../core/models';

@Component({
  selector: 'app-vaccination-summary-table',
  standalone: true,
  imports: [DatePipe, TableModule, TagModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <p-table
      [value]="vaccinations()"
      [loading]="loading()"
      [scrollable]="true"
      scrollHeight="flex"
      [rowHover]="true"
      dataKey="id"
      [tableStyle]="{ 'min-width': '28rem' }"
    >
      <ng-template #header>
        <tr>
          <th>Administered</th>
          <th>CVX</th>
          <th>Lot</th>
          <th>Status</th>
          <th>Issues</th>
        </tr>
      </ng-template>

      <ng-template #body let-vaccination>
        <tr>
          <td>{{ vaccination.vaccine?.administeredDate | date: 'mediumDate' }}</td>
          <td class="mono">{{ vaccination.vaccine?.vaccineCvxCode ?? '—' }}</td>
          <td class="mono">{{ vaccination.vaccine?.lotNumber ?? '—' }}</td>
          <td>{{ vaccination.vaccine?.completionStatus ?? '—' }}</td>
          <td>
            @if (vaccination.feedbacksCount) {
              <p-tag [value]="vaccination.feedbacksCount" severity="warn" [rounded]="true" />
            } @else {
              <span class="muted">—</span>
            }
          </td>
        </tr>
      </ng-template>

      <ng-template #emptymessage>
        <tr>
          <td colspan="5" class="empty">No immunizations recorded for this patient.</td>
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
  `,
})
export class VaccinationSummaryTableComponent {
  readonly vaccinations = input.required<VaccinationEvent[]>();
  readonly loading = input(false);
}
