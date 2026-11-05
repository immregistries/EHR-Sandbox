import {Routes} from '@angular/router';

export const DASHBOARD_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./dashboard.component').then((m) => m.DashboardComponent),
    // The selected patient is part of the dashboard url, so the dashboard
    // stays mounted while :patientId changes.
    children: [{path: 'patients/:patientId', children: []}],
  },
];
