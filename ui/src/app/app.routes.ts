import {Routes} from '@angular/router';
import {authGuard} from './core/guards/auth.guard';
import {facilityGuard} from './core/guards/facility.guard';
import {tenantGuard} from './core/guards/tenant.guard';
import {MainLayoutComponent} from './shared/layout/main-layout/main-layout.component';

/**
 * Tenant and facility context live in the url:
 *   /t/:tenantId/f/:facilityId/dashboard/patients/:patientId
 *
 * Context-free pages (home, tenants) are mounted at the root, under
 * /t/:tenantId and under /t/:tenantId/f/:facilityId, so navigating to them
 * preserves whatever context is already selected — the IIS Sandbox approach,
 * extended one level for facilities.
 */
const CONTEXT_FREE_ROUTES: Routes = [
  {
    path: 'home',
    loadChildren: () => import('./features/home/home.routes').then((m) => m.HOME_ROUTES),
  },
  {
    path: 'tenants',
    loadChildren: () => import('./features/tenant/tenant.routes').then((m) => m.TENANT_ROUTES),
  },
];

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      ...CONTEXT_FREE_ROUTES,
      {
        path: 't/:tenantId',
        canActivate: [tenantGuard],
        children: [
          ...CONTEXT_FREE_ROUTES,
          {
            path: 'facilities',
            loadChildren: () =>
              import('./features/facility/facility.routes').then((m) => m.FACILITY_ROUTES),
          },
          {
            path: 'f/:facilityId',
            canActivate: [facilityGuard],
            children: [
              ...CONTEXT_FREE_ROUTES,
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
      {path: '', redirectTo: 'home', pathMatch: 'full'},
    ],
  },
  {path: '**', redirectTo: ''},
];
