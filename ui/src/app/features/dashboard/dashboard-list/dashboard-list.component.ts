import {ChangeDetectionStrategy, Component} from '@angular/core';

/** Placeholder — implemented in the components step. */
@Component({
  selector: 'app-dashboard-list',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<h2>Dashboard</h2>`,
})
export class DashboardListComponent {
}
