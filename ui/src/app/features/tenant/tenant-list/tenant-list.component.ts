import {ChangeDetectionStrategy, Component} from '@angular/core';

/** Placeholder — implemented in the components step. */
@Component({
  selector: 'app-tenant-list',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<h2>Tenants</h2>`,
})
export class TenantListComponent {
}
