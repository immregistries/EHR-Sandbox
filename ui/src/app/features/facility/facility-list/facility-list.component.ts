import {ChangeDetectionStrategy, Component, computed, effect, inject} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {Store} from '@ngrx/store';
import {MessageService} from 'primeng/api';
import {ButtonModule} from 'primeng/button';
import {IconFieldModule} from 'primeng/iconfield';
import {InputIconModule} from 'primeng/inputicon';
import {InputTextModule} from 'primeng/inputtext';
import {TableModule} from 'primeng/table';
import {TagModule} from 'primeng/tag';
import {TooltipModule} from 'primeng/tooltip';
import {Facility} from '../../../core/models';
import {FacilityActions} from '../../../core/store/facility/facility.actions';
import {
  selectAllFacilities,
  selectFacilitiesLoading,
} from '../../../core/store/facility/facility.selectors';
import {
  selectRouteFacilityId,
  selectRouteTenantId,
} from '../../../core/store/router.selectors';
import {selectCurrentTenant} from '../../../core/store/tenant/tenant.selectors';

interface FacilityRow {
  facility: Facility;
  id: number;
  name: string;
  type: string;
  children: number;
  issues: number;
}

/**
 * Facility selection page for the tenant in the url. Picking a facility
 * navigates to /t/:tenantId/f/:facilityId/dashboard.
 */
@Component({
  selector: 'app-facility-list',
  standalone: true,
  imports: [
    FormsModule,
    ButtonModule,
    IconFieldModule,
    InputIconModule,
    InputTextModule,
    TableModule,
    TagModule,
    TooltipModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page">
      <header class="page-header">
        <div class="page-title">
          <h1>Facilities</h1>
          @if (tenant(); as current) {
            <span class="subtitle">in {{ current.nameDisplay }}</span>
          }
        </div>
        <p-button label="New facility" icon="pi pi-plus" (onClick)="create()" />
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
        [globalFilterFields]="['name', 'type']"
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
                placeholder="Search facilities"
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
            <th style="width: 10rem">Actions</th>
          </tr>
        </ng-template>

        <ng-template #body let-row>
          <tr
            class="facility-row"
            [class.row--active]="row.id === activeFacilityId()"
            (click)="select(row.facility)"
          >
            <td class="mono">{{ row.id }}</td>
            <td>
              {{ row.name }}
              @if (row.id === activeFacilityId()) {
                <p-tag value="active" severity="success" [rounded]="true" />
              }
            </td>
            <td>{{ row.type || '—' }}</td>
            <td>{{ row.children }}</td>
            <td>
              @if (row.issues) {
                <p-tag [value]="row.issues" severity="warn" [rounded]="true" />
              } @else {
                <span class="muted">—</span>
              }
            </td>
            <td>
              <p-button
                label="Open"
                icon="pi pi-arrow-right"
                iconPos="right"
                size="small"
                [text]="true"
                (onClick)="select(row.facility); $event.stopPropagation()"
              />
            </td>
          </tr>
        </ng-template>

        <ng-template #emptymessage>
          <tr>
            <td colspan="6" class="empty">
              @if (loading()) {
                Loading facilities…
              } @else {
                No facilities in this tenant yet. Create one to get started.
              }
            </td>
          </tr>
        </ng-template>
      </p-table>
    </div>
  `,
  styleUrl: './facility-list.component.scss',
})
export class FacilityListComponent {
  private readonly store = inject(Store);
  private readonly router = inject(Router);
  private readonly messageService = inject(MessageService);

  readonly tenantId = this.store.selectSignal(selectRouteTenantId);
  readonly tenant = this.store.selectSignal(selectCurrentTenant);
  readonly facilities = this.store.selectSignal(selectAllFacilities);
  readonly loading = this.store.selectSignal(selectFacilitiesLoading);
  readonly activeFacilityId = this.store.selectSignal(selectRouteFacilityId);

  readonly columns = [
    {field: 'id', header: 'ID'},
    {field: 'name', header: 'Name'},
    {field: 'type', header: 'Type'},
    {field: 'children', header: 'Sub-facilities'},
    {field: 'issues', header: 'Issues'},
  ];

  search = '';

  readonly rows = computed<FacilityRow[]>(() =>
    this.facilities().map((facility) => ({
      facility,
      id: facility.id ?? -1,
      name: facility.nameDisplay ?? '',
      type: facility.type ?? '',
      children: facility.childrenCount ?? 0,
      issues: facility.feedbacksCount ?? 0,
    })),
  );

  constructor() {
    effect(() => {
      const tenantId = this.tenantId();
      if (tenantId > 0) {
        this.store.dispatch(FacilityActions.loadFacilities({tenantId}));
      }
    });
  }

  select(facility: Facility): void {
    this.router.navigate(['/t', this.tenantId(), 'f', facility.id, 'dashboard']);
  }

  create(): void {
    this.messageService.add({
      severity: 'info',
      summary: 'Facility creation is not available yet',
      detail: 'This dialog has not been migrated to the new UI yet.',
      life: 3500,
    });
  }
}
