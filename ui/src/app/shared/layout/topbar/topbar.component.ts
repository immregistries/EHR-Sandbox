import {ChangeDetectionStrategy, Component, inject, output} from '@angular/core';
import {RouterLink} from '@angular/router';
import {Store} from '@ngrx/store';
import {ButtonModule} from 'primeng/button';
import {TagModule} from 'primeng/tag';
import {TooltipModule} from 'primeng/tooltip';
import {ThemeService} from '../../../core/services';
import {AuthActions} from '../../../core/store/auth/auth.actions';
import {selectUsername} from '../../../core/store/auth/auth.selectors';
import {selectCurrentFacility} from '../../../core/store/facility/facility.selectors';
import {PatientActions} from '../../../core/store/patient/patient.actions';
import {selectPatientsLoading} from '../../../core/store/patient/patient.selectors';
import {
  selectRouteFacilityId,
  selectRouteTenantId,
} from '../../../core/store/router.selectors';
import {selectCurrentTenant} from '../../../core/store/tenant/tenant.selectors';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [RouterLink, ButtonModule, TagModule, TooltipModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <header class="topbar">
      <div class="topbar-left">
        <p-button
          icon="pi pi-bars"
          [text]="true"
          severity="secondary"
          size="small"
          ariaLabel="Toggle navigation"
          (onClick)="menuToggle.emit()"
        />

        <!-- Context is read from the url and changed by navigating to the
             tenant / facility pages, never by a dropdown. -->
        <nav class="breadcrumb" aria-label="Current context">
          <a routerLink="/tenants" class="crumb">Tenants</a>

          @if (tenantId() > 0) {
            <i class="pi pi-angle-right crumb-sep" aria-hidden="true"></i>
            <a [routerLink]="['/t', tenantId(), 'facilities']" class="crumb crumb--current">
              <i class="pi pi-building"></i>
              {{ tenant()?.nameDisplay ?? 'Tenant ' + tenantId() }}
            </a>
          }

          @if (facilityId() > 0) {
            <i class="pi pi-angle-right crumb-sep" aria-hidden="true"></i>
            <a
              [routerLink]="['/t', tenantId(), 'f', facilityId(), 'dashboard']"
              class="crumb crumb--current"
            >
              <i class="pi pi-sitemap"></i>
              {{ facility()?.nameDisplay ?? 'Facility ' + facilityId() }}
            </a>
          }
        </nav>
      </div>

      <div class="topbar-right">
        <p-tag value="Test data only" severity="warn" [rounded]="true" />

        <p-button
          icon="pi pi-refresh"
          [text]="true"
          severity="secondary"
          size="small"
          [loading]="refreshing()"
          pTooltip="Refresh current data"
          tooltipPosition="bottom"
          ariaLabel="Refresh"
          (onClick)="refresh()"
        />

        <p-button
          [icon]="theme.darkMode() ? 'pi pi-sun' : 'pi pi-moon'"
          [text]="true"
          severity="secondary"
          size="small"
          [pTooltip]="theme.darkMode() ? 'Switch to light mode' : 'Switch to dark mode'"
          tooltipPosition="bottom"
          ariaLabel="Toggle color scheme"
          (onClick)="theme.toggleDarkMode()"
        />

        @if (username(); as name) {
          <span class="username"><i class="pi pi-user"></i>{{ name }}</span>
        }

        <p-button
          icon="pi pi-sign-out"
          label="Logout"
          [text]="true"
          severity="secondary"
          size="small"
          (onClick)="logout()"
        />
      </div>
    </header>
  `,
  styleUrl: './topbar.component.scss',
})
export class TopbarComponent {
  private readonly store = inject(Store);
  readonly theme = inject(ThemeService);

  readonly menuToggle = output<void>();

  readonly username = this.store.selectSignal(selectUsername);
  readonly refreshing = this.store.selectSignal(selectPatientsLoading);
  readonly tenantId = this.store.selectSignal(selectRouteTenantId);
  readonly facilityId = this.store.selectSignal(selectRouteFacilityId);
  readonly tenant = this.store.selectSignal(selectCurrentTenant);
  readonly facility = this.store.selectSignal(selectCurrentFacility);

  refresh(): void {
    this.store.dispatch(PatientActions.refresh());
  }

  logout(): void {
    this.store.dispatch(AuthActions.logout());
  }
}
