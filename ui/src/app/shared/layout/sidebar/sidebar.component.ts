import {ChangeDetectionStrategy, Component, computed, inject, input} from '@angular/core';
import {RouterLink, RouterLinkActive} from '@angular/router';
import {Store} from '@ngrx/store';
import {TooltipModule} from 'primeng/tooltip';
import {selectRouteFacilityId, selectRouteTenantId,} from '../../../core/store/router.selectors';
import {NAV_SECTIONS, NavItem, NavSection} from './nav-items';

interface ResolvedItem extends NavItem {
  link: unknown[];
}

interface ResolvedSection extends Omit<NavSection, 'items'> {
  items: ResolvedItem[];
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, TooltipModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <nav class="sidebar" [class.sidebar--collapsed]="collapsed()">
      <div class="sidebar-header">
        <i class="pi pi-shield"></i>
        @if (!collapsed()) {
          <span class="sidebar-title">EHR Sandbox</span>
        }
      </div>

      <ul class="sidebar-nav">
        @for (section of sections(); track section.title) {
          @if (!collapsed()) {
            <li class="nav-section">{{ section.title }}</li>
          } @else {
            <li class="nav-separator" aria-hidden="true"></li>
          }

          @for (item of section.items; track item.label) {
            <li>
              <a
                class="nav-link"
                [class.nav-link--pending]="item.pending"
                [routerLink]="item.link"
                routerLinkActive="active"
                [pTooltip]="collapsed() ? item.label : (item.tooltip ?? '')"
                tooltipPosition="right"
              >
                <i [class]="item.icon"></i>
                @if (!collapsed()) {
                  <span class="nav-label">{{ item.label }}</span>
                  @if (item.pending) {
                    <span class="nav-badge">soon</span>
                  }
                }
              </a>
            </li>
          }
        }
      </ul>

    </nav>
  `,
  styleUrl: './sidebar.component.scss',
})
export class SidebarComponent {
  private readonly store = inject(Store);

  readonly collapsed = input(false);

  private readonly tenantId = this.store.selectSignal(selectRouteTenantId);
  private readonly facilityId = this.store.selectSignal(selectRouteFacilityId);

  /**
   * Resolves each item's link against the current context and hides sections
   * whose scope is not reachable yet (no tenant, or no facility).
   */
  readonly sections = computed<ResolvedSection[]>(() => {
    const tenantId = this.tenantId();
    const facilityId = this.facilityId();
    const hasTenant = tenantId > 0;
    const hasFacility = hasTenant && facilityId > 0;

    return NAV_SECTIONS.filter((section) => {
      if (section.scope === 'tenant') return hasTenant;
      if (section.scope === 'facility') return hasFacility;
      return true;
    }).map((section) => ({
      ...section,
      items: section.items.map((item) => ({
        ...item,
        link: this.resolveLink(item, tenantId, facilityId),
      })),
    }));
  });

  private resolveLink(item: NavItem, tenantId: number, facilityId: number): unknown[] {
    switch (item.scope) {
      case 'tenant':
        return ['/t', tenantId, ...item.path];
      case 'facility':
        return ['/t', tenantId, 'f', facilityId, ...item.path];
      default:
        // Context-free pages keep the deepest prefix currently selected, so
        // visiting Home or Tenants never drops the tenant or facility.
        if (!item.tenantScopable || tenantId <= 0) {
          return item.path;
        }
        const tail = item.path.map((segment) => segment.replace(/^\//, ''));
        return facilityId > 0
          ? ['/t', tenantId, 'f', facilityId, ...tail]
          : ['/t', tenantId, ...tail];
    }
  }
}
