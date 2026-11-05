import {inject} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivateFn, Router} from '@angular/router';
import {Store} from '@ngrx/store';
import {TenantActions} from '../store/tenant/tenant.actions';

/** Guards /t/:tenantId and makes sure the tenant is loaded in the store. */
export const tenantGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const store = inject(Store);
  const router = inject(Router);

  const tenantId = Number(route.paramMap.get('tenantId'));
  if (!Number.isFinite(tenantId) || tenantId <= 0) {
    return router.createUrlTree(['/tenants']);
  }

  store.dispatch(TenantActions.loadTenant({tenantId}));
  return true;
};
