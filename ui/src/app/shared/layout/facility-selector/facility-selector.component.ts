import {ChangeDetectionStrategy, Component, computed, inject} from '@angular/core';
import {Router} from '@angular/router';
import {Store} from '@ngrx/store';
import {MenuItem} from 'primeng/api';
import {ButtonModule} from 'primeng/button';
import {MenuModule} from 'primeng/menu';
import {TooltipModule} from 'primeng/tooltip';
import {Facility} from '../../../core/models';
import {selectAllFacilities, selectCurrentFacility,} from '../../../core/store/facility/facility.selectors';
import {selectRouteFacilityId, selectRouteTenantId,} from '../../../core/store/router.selectors';

/** Selecting a facility routes to /t/:tenantId/f/:facilityId. */
@Component({
  selector: 'app-facility-selector',
  standalone: true,
  imports: [ButtonModule, MenuModule, TooltipModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <span
      [pTooltip]="disabled() ? 'Select a tenant first' : ''"
      tooltipPosition="bottom"
    >
      <p-button
        [label]="label()"
        icon="pi pi-sitemap"
        size="small"
        [text]="!hasFacility()"
        [severity]="hasFacility() ? 'primary' : 'secondary'"
        [disabled]="disabled()"
        (onClick)="menu.toggle($event)"
      />
    </span>
    <p-menu #menu [model]="items()" [popup]="true" appendTo="body" />
  `,
})
export class FacilitySelectorComponent {
  private readonly store = inject(Store);
  private readonly router = inject(Router);

  private readonly facilities = this.store.selectSignal(selectAllFacilities);
  private readonly tenantId = this.store.selectSignal(selectRouteTenantId);
  private readonly facilityId = this.store.selectSignal(selectRouteFacilityId);
  readonly current = this.store.selectSignal(selectCurrentFacility);

  readonly disabled = computed(() => this.tenantId() <= 0);
  readonly hasFacility = computed(() => this.facilityId() > 0);

  readonly label = computed(() => {
    const facility = this.current();
    if (facility?.nameDisplay) {
      return facility.nameDisplay;
    }
    return this.hasFacility() ? `Facility ${this.facilityId()}` : 'Select facility';
  });

  readonly items = computed<MenuItem[]>(() => {
    const facilities = this.facilities();
    const currentId = this.facilityId();

    const facilityItems: MenuItem[] = facilities.map((facility) => ({
      label: facility.nameDisplay ?? `Facility ${facility.id}`,
      icon: facility.id === currentId ? 'pi pi-check' : 'pi pi-fw',
      command: () => this.select(facility),
    }));

    return [
      ...(facilityItems.length
        ? [{label: 'Facilities', items: facilityItems}]
        : [{label: 'No facility in this tenant', disabled: true} as MenuItem]),
      {separator: true},
      {
        label: 'Manage facilities',
        icon: 'pi pi-cog',
        command: () => this.router.navigate(['/t', this.tenantId(), 'facilities']),
      },
    ];
  });

  private select(facility: Facility): void {
    this.router.navigate(['/t', this.tenantId(), 'f', facility.id]);
  }
}
