import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, input, output} from '@angular/core';
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

export interface VaccinationToolRequest {
  action: RecordToolAction;
  vaccination: VaccinationEvent;
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
      [value]="vaccinations()"
      [loading]="loading()"
      [scrollable]="true"
      scrollHeight="flex"
      [rowHover]="true"
      dataKey="id"
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
          <th pSortableColumn="vaccine.administeredDate">
            Administered <p-sortIcon field="vaccine.administeredDate" />
          </th>
          <th>CVX</th>
          <th>Lot</th>
          <th>Status</th>
          <th>Issues</th>
        </tr>
      </ng-template>

      <ng-template #body let-vaccination let-expanded="expanded">
        <tr>
          <td>
            <p-button
              type="button"
              [pRowToggler]="vaccination"
              [text]="true"
              size="small"
              severity="secondary"
              [icon]="expanded ? 'pi pi-chevron-down' : 'pi pi-chevron-right'"
              ariaLabel="Toggle actions"
            />
          </td>
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

      <ng-template #expandedrow let-vaccination>
        <tr class="detail-row">
          <td colspan="6">
            <app-record-tools
              [tools]="vaccinationTools"
              (toolSelect)="toolSelect.emit({ action: $event, vaccination })"
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
