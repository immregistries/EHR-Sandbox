import {Routes} from '@angular/router';

export const TENANT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./tenant-list/tenant-list.component').then((m) => m.TenantListComponent),
  },
];
