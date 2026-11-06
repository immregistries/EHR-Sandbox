import {ChangeDetectionStrategy, Component, computed, effect, inject, signal} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {Store} from '@ngrx/store';
import {ButtonModule} from 'primeng/button';
import {DialogModule} from 'primeng/dialog';
import {IconFieldModule} from 'primeng/iconfield';
import {InputIconModule} from 'primeng/inputicon';
import {InputTextModule} from 'primeng/inputtext';
import {TableModule} from 'primeng/table';
import {TagModule} from 'primeng/tag';
import {TooltipModule} from 'primeng/tooltip';
import {Clinician} from '../../../core/models';
import {ClinicianActions} from '../../../core/store/clinician/clinician.actions';
import {
  clinicianLabel,
  selectAllClinicians,
  selectCliniciansLoading,
} from '../../../core/store/clinician/clinician.selectors';
import {selectRouteTenantId} from '../../../core/store/router.selectors';
import {selectCurrentTenant} from '../../../core/store/tenant/tenant.selectors';
import {ClinicianFormComponent} from '../clinician-form/clinician-form.component';

interface ClinicianRow {
  clinician: Clinician;
  id: number;
  name: string;
  qualification: string;
  identifier: string;
  contact: string;
}

/**
 * Clinicians of the tenant in the url. Clinicians are tenant-scoped, not
 * facility-scoped, so this sits beside Facilities rather than under one.
 */
@Component({
  selector: 'app-clinician-list',
  standalone: true,
  imports: [
    FormsModule,
    ButtonModule,
    DialogModule,
    IconFieldModule,
    InputIconModule,
    InputTextModule,
    TableModule,
    TagModule,
    TooltipModule,
    ClinicianFormComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page">
      <header class="page-header">
        <div class="page-title">
          <h1>Clinicians</h1>
          @if (tenant(); as current) {
            <span class="subtitle">in {{ current.nameDisplay }}</span>
          }
        </div>
        <p-button label="New clinician" icon="pi pi-plus" (onClick)="openCreate()" />
      </header>

      <p-table
        #table
        [value]="rows()"
        [loading]="loading()"
        [rowHover]="true"
        dataKey="id"
        sortMode="single"
        [paginator]="rows().length > 10"
        [rows]="10"
        [globalFilterFields]="['name', 'qualification', 'identifier']"
        styleClass="p-datatable-sm"
      >
        <ng-template #caption>
          <div class="table-toolbar">
            <span class="toolbar-spacer"></span>
            <p-iconfield iconPosition="left">
              <p-inputicon styleClass="pi pi-search" />
              <input
                pInputText
                type="search"
                placeholder="Search clinicians"
                [(ngModel)]="search"
                (ngModelChange)="table.filterGlobal($event, 'contains')"
              />
            </p-iconfield>
          </div>
        </ng-template>

        <ng-template #header>
          <tr>
            @for (column of columns; track column.field) {
              <th [pSortableColumn]="column.field">
                {{ column.header }}
                <p-sortIcon [field]="column.field" />
              </th>
            }
            <th style="width: 8rem">Actions</th>
          </tr>
        </ng-template>

        <ng-template #body let-row>
          <tr class="clinician-row" (click)="openEdit(row.clinician)">
            <td class="mono">{{ row.id }}</td>
            <td>{{ row.name }}</td>
            <td>
              @if (row.qualification) {
                <p-tag [value]="row.qualification" severity="info" [rounded]="true" />
              } @else {
                <span class="muted">—</span>
              }
            </td>
            <td class="mono">{{ row.identifier || '—' }}</td>
            <td>{{ row.contact || '—' }}</td>
            <td>
              <p-button
                icon="pi pi-pencil"
                [text]="true"
                size="small"
                severity="secondary"
                pTooltip="Edit clinician"
                tooltipPosition="left"
                ariaLabel="Edit clinician"
                (onClick)="openEdit(row.clinician); $event.stopPropagation()"
              />
            </td>
          </tr>
        </ng-template>

        <ng-template #emptymessage>
          <tr>
            <td colspan="6" class="empty">
              @if (loading()) {
                Loading clinicians…
              } @else {
                No clinicians in this tenant yet. Create one to get started.
              }
            </td>
          </tr>
        </ng-template>
      </p-table>

      <p-dialog
        [header]="editing() ? 'Edit clinician' : 'New clinician'"
        [visible]="formOpen()"
        (visibleChange)="formOpen.set($event)"
        [modal]="true"
        [draggable]="false"
        styleClass="form-dialog"
      >
        @if (formOpen()) {
          <app-clinician-form
            [clinician]="editing()"
            (saved)="onSaved()"
            (cancelled)="formOpen.set(false)"
          />
        }
      </p-dialog>
    </div>
  `,
  styleUrl: './clinician-list.component.scss',
})
export class ClinicianListComponent {
  private readonly store = inject(Store);

  readonly tenantId = this.store.selectSignal(selectRouteTenantId);
  readonly tenant = this.store.selectSignal(selectCurrentTenant);
  readonly clinicians = this.store.selectSignal(selectAllClinicians);
  readonly loading = this.store.selectSignal(selectCliniciansLoading);

  readonly formOpen = signal(false);
  readonly editing = signal<Clinician | undefined>(undefined);

  readonly columns = [
    {field: 'id', header: 'ID'},
    {field: 'name', header: 'Name'},
    {field: 'qualification', header: 'Qualification'},
    {field: 'identifier', header: 'Identifier'},
    {field: 'contact', header: 'Contact'},
  ];

  search = '';

  readonly rows = computed<ClinicianRow[]>(() =>
    this.clinicians().map((clinician) => ({
      clinician,
      id: clinician.id ?? -1,
      name: clinicianLabel(clinician),
      qualification: clinician.qualification ?? '',
      identifier: clinician.identifiers?.[0]?.value ?? '',
      contact: clinician.phones?.[0]?.number ?? '',
    })),
  );

  constructor() {
    effect(() => {
      const tenantId = this.tenantId();
      if (tenantId > 0) {
        this.store.dispatch(ClinicianActions.loadClinicians({tenantId}));
      }
    });
  }

  openCreate(): void {
    this.editing.set(undefined);
    this.formOpen.set(true);
  }

  openEdit(clinician: Clinician): void {
    this.editing.set(clinician);
    this.formOpen.set(true);
  }

  onSaved(): void {
    this.formOpen.set(false);
    this.store.dispatch(ClinicianActions.refresh());
  }
}
