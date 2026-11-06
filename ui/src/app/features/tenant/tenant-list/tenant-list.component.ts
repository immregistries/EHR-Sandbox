import {ChangeDetectionStrategy, Component, computed, inject} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {Store} from '@ngrx/store';
import {ButtonModule} from 'primeng/button';
import {IconFieldModule} from 'primeng/iconfield';
import {InputIconModule} from 'primeng/inputicon';
import {InputTextModule} from 'primeng/inputtext';
import {TableModule} from 'primeng/table';
import {TagModule} from 'primeng/tag';
import {TooltipModule} from 'primeng/tooltip';
import {Tenant} from '../../../core/models';
import {selectRouteTenantId} from '../../../core/store/router.selectors';
import {TenantActions} from '../../../core/store/tenant/tenant.actions';
import {
  selectAllTenants,
  selectFlavors,
  selectTenantsLoading,
} from '../../../core/store/tenant/tenant.selectors';

interface TenantRow {
  tenant: Tenant;
  id: number;
  name: string;
  alias: string;
  flavors: string[];
}

/**
 * Tenant selection page. Picking a tenant navigates to /t/:tenantId, which is
 * what makes the choice persist — context lives in the url, not in a service.
 */
@Component({
  selector: 'app-tenant-list',
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
          <h1>Tenants</h1>
          <p-tag
            value="?"
            [rounded]="true"
            severity="info"
            pTooltip="A tenant is an isolated testing environment — one tenant stands in for one IIS. Facilities registered under it act as its information sources."
            tooltipPosition="right"
          />
        </div>
        <p-button
          label="New tenant"
          icon="pi pi-plus"
          (onClick)="create()"
        />
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
        [globalFilterFields]="['name', 'alias']"
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
                placeholder="Search tenants"
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
            class="tenant-row"
            [class.row--active]="row.id === activeTenantId()"
            (click)="select(row.tenant)"
          >
            <td class="mono">{{ row.id }}</td>
            <td>
              {{ row.name }}
              @if (row.id === activeTenantId()) {
                <p-tag value="active" severity="success" [rounded]="true" />
              }
            </td>
            <td>{{ row.alias || '—' }}</td>
            <td>
              @if (row.flavors.length) {
                <div class="flavors">
                  @for (flavor of row.flavors; track flavor) {
                    <p-tag [value]="flavor" severity="warn" [rounded]="true" />
                  }
                </div>
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
                (onClick)="select(row.tenant); $event.stopPropagation()"
              />
            </td>
          </tr>
        </ng-template>

        <ng-template #emptymessage>
          <tr>
            <td colspan="5" class="empty">
              @if (loading()) {
                Loading tenants…
              } @else {
                No tenants yet. Create one to get started.
              }
            </td>
          </tr>
        </ng-template>
      </p-table>
    </div>
  `,
  styleUrl: './tenant-list.component.scss',
})
export class TenantListComponent {
  private readonly store = inject(Store);
  private readonly router = inject(Router);

  readonly tenants = this.store.selectSignal(selectAllTenants);
  readonly loading = this.store.selectSignal(selectTenantsLoading);
  readonly activeTenantId = this.store.selectSignal(selectRouteTenantId);
  private readonly flavors = this.store.selectSignal(selectFlavors);

  readonly columns = [
    {field: 'id', header: 'ID'},
    {field: 'name', header: 'Name'},
    {field: 'alias', header: 'Alias'},
    {field: 'flavors', header: 'Flavors'},
  ];

  search = '';

  readonly rows = computed<TenantRow[]>(() => {
    const flavors = this.flavors();
    return this.tenants().map((tenant) => {
      const name = tenant.nameDisplay ?? '';
      return {
        tenant,
        id: tenant.id,
        name,
        alias: tenant.alias ?? '',
        // Flavors are encoded in the tenant name, as the backend does.
        flavors: flavors
          .filter((flavor) => !flavor.hidden && name.toUpperCase().includes(flavor.key))
          .map((flavor) => flavor.key),
      };
    });
  });

  constructor() {
    this.store.dispatch(TenantActions.loadTenants());
    this.store.dispatch(TenantActions.loadFlavors());
  }

  select(tenant: Tenant): void {
    this.router.navigate(['/t', tenant.id, 'facilities']);
  }

  create(): void {
    this.router.navigate(['/tenants'], {queryParams: {create: 1}});
  }
}
