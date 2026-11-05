import {Routes} from '@angular/router';

export const FACILITY_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./facility-list/facility-list.component').then((m) => m.FacilityListComponent),
  },
];
