import {inject} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivateFn, Router} from '@angular/router';
import {Store} from '@ngrx/store';
import {FacilityActions} from '../store/facility/facility.actions';

/** Guards /t/:tenantId/f/:facilityId and loads the facility. */
export const facilityGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const store = inject(Store);
  const router = inject(Router);

  const tenantId = Number(route.paramMap.get('tenantId'));
  const facilityId = Number(route.paramMap.get('facilityId'));

  if (!Number.isFinite(facilityId) || facilityId <= 0) {
    return router.createUrlTree(['/t', tenantId, 'facilities']);
  }

  store.dispatch(FacilityActions.loadFacility({tenantId, facilityId}));
  return true;
};
