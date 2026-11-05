import {Routes} from '@angular/router';
import {authGuard} from './core/guards/auth.guard';
import {facilityGuard} from './core/guards/facility.guard';
import {tenantGuard} from './core/guards/tenant.guard';

/**
 * Tenant and facility context live in the url:
 *   /t/:tenantId/f/:facilityId/patients/:patientId/vaccinations/:vaccinationId
 * so any view is directly linkable and the store derives its context from the
 * router instead of a separately held "current selection".
 */
export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: '',
    canActivate: [authGuard],
    children: [
      {
        path: 'tenants',
        loadChildren: () => import('./features/tenant/tenant.routes').then((m) => m.TENANT_ROUTES),
      },
      {
        path: 't/:tenantId',
        canActivate: [tenantGuard],
        children: [
          {
            path: 'facilities',
            loadChildren: () =>
              import('./features/facility/facility.routes').then((m) => m.FACILITY_ROUTES),
          },
          {
            path: 'f/:facilityId',
            canActivate: [facilityGuard],
            children: [
              {
                path: 'dashboard',
                loadChildren: () =>
                  import('./features/dashboard/dashboard.routes').then((m) => m.DASHBOARD_ROUTES),
              },
              {
                path: 'patients',
                loadChildren: () =>
                  import('./features/patient/patient.routes').then((m) => m.PATIENT_ROUTES),
              },
              {path: '', redirectTo: 'dashboard', pathMatch: 'full'},
            ],
          },
          {path: '', redirectTo: 'facilities', pathMatch: 'full'},
        ],
      },
      {path: '', redirectTo: 'tenants', pathMatch: 'full'},
    ],
  },
  {path: '**', redirectTo: ''},
];
