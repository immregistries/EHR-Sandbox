import {Routes} from '@angular/router';

export const CLINICIAN_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./clinician-list/clinician-list.component').then((m) => m.ClinicianListComponent),
  },
];
