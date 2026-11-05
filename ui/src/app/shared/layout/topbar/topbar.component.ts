import {ChangeDetectionStrategy, Component, inject, output} from '@angular/core';
import {Store} from '@ngrx/store';
import {ButtonModule} from 'primeng/button';
import {TagModule} from 'primeng/tag';
import {TooltipModule} from 'primeng/tooltip';
import {ThemeService} from '../../../core/services';
import {AuthActions} from '../../../core/store/auth/auth.actions';
import {selectUsername} from '../../../core/store/auth/auth.selectors';
import {selectPatientsLoading} from '../../../core/store/patient/patient.selectors';
import {PatientActions} from '../../../core/store/patient/patient.actions';
import {FacilitySelectorComponent} from '../facility-selector/facility-selector.component';
import {TenantSelectorComponent} from '../tenant-selector/tenant-selector.component';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [
    ButtonModule,
    TagModule,
    TooltipModule,
    TenantSelectorComponent,
    FacilitySelectorComponent,
  ],
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
        <app-tenant-selector />
        <app-facility-selector />
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

  refresh(): void {
    this.store.dispatch(PatientActions.refresh());
  }

  logout(): void {
    this.store.dispatch(AuthActions.logout());
  }
}
