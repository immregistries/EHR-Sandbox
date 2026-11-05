import {ChangeDetectionStrategy, Component, signal} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {SidebarComponent} from '../sidebar/sidebar.component';
import {TopbarComponent} from '../topbar/topbar.component';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterOutlet, SidebarComponent, TopbarComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="layout" [class.layout--collapsed]="collapsed()">
      <app-sidebar [collapsed]="collapsed()" />
      <div class="layout-main">
        <app-topbar (menuToggle)="toggleSidebar()" />
        <main class="layout-content">
          <router-outlet />
        </main>
      </div>
    </div>
  `,
  styles: `
    @use 'variables' as v;

    .layout {
      display: flex;
      height: 100vh;
      overflow: hidden;
    }

    .layout-main {
      flex: 1;
      display: flex;
      flex-direction: column;
      min-width: 0;
    }

    .layout-content {
      flex: 1;
      overflow-y: auto;
      padding: v.$spacing-md;
      background: var(--app-bg);
    }
  `,
})
export class MainLayoutComponent {
  readonly collapsed = signal(false);

  toggleSidebar(): void {
    this.collapsed.update((value) => !value);
  }
}
