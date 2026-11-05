import {inject, Injectable} from '@angular/core';
import {Actions, createEffect, ofType} from '@ngrx/effects';
import {routerNavigatedAction} from '@ngrx/router-store';
import {concatLatestFrom} from '@ngrx/operators';
import {Store} from '@ngrx/store';
import {distinctUntilChanged, filter, map, pairwise, startWith} from 'rxjs';
import {FacilityActions} from './facility/facility.actions';
import {PatientActions} from './patient/patient.actions';
import {selectRouteFacilityId, selectRouteTenantId} from './router.selectors';
import {VaccinationActions} from './vaccination/vaccination.actions';

/**
 * Keeps store slices in sync with the tenant/facility context carried by the
 * url. This replaces the old pattern where each service subscribed to the
 * "current selected" subject of its parent service.
 */
@Injectable()
export class ContextEffects {
  private readonly actions$ = inject(Actions);
  private readonly store = inject(Store);

  private readonly tenantId$ = this.store
    .select(selectRouteTenantId)
    .pipe(distinctUntilChanged());

  private readonly facilityId$ = this.store
    .select(selectRouteFacilityId)
    .pipe(distinctUntilChanged());

  /** Entering a tenant loads its facilities. */
  tenantChanged$ = createEffect(() =>
    this.actions$.pipe(
      ofType(routerNavigatedAction),
      concatLatestFrom(() => this.tenantId$),
      map(([, tenantId]) => tenantId),
      distinctUntilChanged(),
      filter((tenantId) => tenantId > 0),
      map((tenantId) => FacilityActions.loadFacilities({tenantId})),
    ),
  );

  /** Leaving a facility (or switching to another) invalidates its children. */
  facilityChanged$ = createEffect(() =>
    this.facilityId$.pipe(
      startWith(-1),
      pairwise(),
      filter(([previous, current]) => previous !== current && previous > 0),
      map(() => PatientActions.clear()),
    ),
  );

  facilityChangedVaccinations$ = createEffect(() =>
    this.facilityId$.pipe(
      startWith(-1),
      pairwise(),
      filter(([previous, current]) => previous !== current && previous > 0),
      map(() => VaccinationActions.clear()),
    ),
  );
}
