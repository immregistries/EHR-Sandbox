import {Routes} from '@angular/router';

export const PATIENT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./patient-list/patient-list.component').then((m) => m.PatientListComponent),
  },
];
