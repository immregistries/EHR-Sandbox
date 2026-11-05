import {ChangeDetectionStrategy, Component, computed, inject} from '@angular/core';
import {Router} from '@angular/router';
import {Store} from '@ngrx/store';
import {ButtonModule} from 'primeng/button';
import {MenuModule} from 'primeng/menu';
import {MenuItem} from 'primeng/api';
import {Tenant} from '../../../core/models';
import {selectRouteTenantId} from '../../../core/store/router.selectors';
import {TenantActions} from '../../../core/store/tenant/tenant.actions';
import {selectAllTenants, selectCurrentTenant,} from '../../../core/store/tenant/tenant.selectors';

/**
 * Switching tenant is a navigation, not a state mutation: selecting one
 * routes to /t/:tenantId so the choice is reflected in the url.
 */
@Component({
  selector: 'app-tenant-selector',
  standalone: true,
  imports: [ButtonModule, MenuModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <p-button
      [label]="label()"
      icon="pi pi-building"
      iconPos="left"
      size="small"
      [text]="!hasTenant()"
      [severity]="hasTenant() ? 'primary' : 'secondary'"
      (onClick)="menu.toggle($event)"
    />
    <p-menu #menu [model]="items()" [popup]="true" appendTo="body" />
  `,
})
export class TenantSelectorComponent {
  private readonly store = inject(Store);
  private readonly router = inject(Router);

  private readonly tenants = this.store.selectSignal(selectAllTenants);
  private readonly tenantId = this.store.selectSignal(selectRouteTenantId);
  readonly current = this.store.selectSignal(selectCurrentTenant);

  readonly hasTenant = computed(() => this.tenantId() > 0);

  readonly label = computed(() => {
    const tenant = this.current();
    if (tenant?.nameDisplay) {
      return tenant.nameDisplay;
    }
    return this.hasTenant() ? `Tenant ${this.tenantId()}` : 'Select tenant';
  });

  readonly items = computed<MenuItem[]>(() => {
    const tenants = this.tenants();
    const currentId = this.tenantId();

    const tenantItems: MenuItem[] = tenants.map((tenant) => ({
      label: tenant.nameDisplay ?? `Tenant ${tenant.id}`,
      icon: tenant.id === currentId ? 'pi pi-check' : 'pi pi-fw',
      command: () => this.select(tenant),
    }));

    return [
      ...(tenantItems.length
        ? [{label: 'Tenants', items: tenantItems}]
        : [{label: 'No tenant available', disabled: true} as MenuItem]),
      {
        separator: true,
      },
      {
        label: 'Manage tenants',
        icon: 'pi pi-cog',
        command: () => this.router.navigate(['/tenants']),
      },
    ];
  });

  constructor() {
    this.store.dispatch(TenantActions.loadTenants());
  }

  private select(tenant: Tenant): void {
    this.router.navigate(['/t', tenant.id]);
  }
}
