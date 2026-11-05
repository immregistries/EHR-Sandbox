import {inject, Injectable} from '@angular/core';
import {Actions, createEffect, ofType} from '@ngrx/effects';
import {concatLatestFrom} from '@ngrx/operators';
import {Store} from '@ngrx/store';
import {catchError, filter, map, of, switchMap} from 'rxjs';
import {FacilityService} from '../../services';
import {selectRouteTenantId} from '../router.selectors';
import {FacilityActions} from './facility.actions';

@Injectable()
export class FacilityEffects {
  private readonly actions$ = inject(Actions);
  private readonly store = inject(Store);
  private readonly facilityService = inject(FacilityService);

  loadFacilities$ = createEffect(() =>
    this.actions$.pipe(
      ofType(FacilityActions.loadFacilities),
      switchMap(({tenantId}) =>
        this.facilityService.readFacilities(tenantId).pipe(
          map((facilities) => FacilityActions.loadFacilitiesSuccess({facilities})),
          catchError((error) => of(FacilityActions.loadFacilitiesFailure({error: error.message}))),
        ),
      ),
    ),
  );

  /** A refresh re-reads the facilities of whichever tenant the url points at. */
  refresh$ = createEffect(() =>
    this.actions$.pipe(
      ofType(FacilityActions.refresh),
      concatLatestFrom(() => this.store.select(selectRouteTenantId)),
      filter(([, tenantId]) => tenantId > 0),
      map(([, tenantId]) => FacilityActions.loadFacilities({tenantId})),
    ),
  );

  loadFacility$ = createEffect(() =>
    this.actions$.pipe(
      ofType(FacilityActions.loadFacility),
      switchMap(({tenantId, facilityId}) =>
        this.facilityService.readFacility(tenantId, facilityId).pipe(
          map((facility) => FacilityActions.loadFacilitySuccess({facility})),
          catchError((error) => of(FacilityActions.loadFacilityFailure({error: error.message}))),
        ),
      ),
    ),
  );

  createFacility$ = createEffect(() =>
    this.actions$.pipe(
      ofType(FacilityActions.createFacility),
      switchMap(({tenantId, facility, populate}) =>
        this.facilityService.postFacility(tenantId, facility, populate).pipe(
          map((response) =>
            FacilityActions.createFacilitySuccess({facility: response.body ?? facility}),
          ),
          catchError((error) => of(FacilityActions.saveFacilityFailure({error: error.message}))),
        ),
      ),
    ),
  );

  updateFacility$ = createEffect(() =>
    this.actions$.pipe(
      ofType(FacilityActions.updateFacility),
      switchMap(({tenantId, facility}) =>
        this.facilityService.putFacility(tenantId, facility).pipe(
          map((response) =>
            FacilityActions.updateFacilitySuccess({facility: response.body ?? facility}),
          ),
          catchError((error) => of(FacilityActions.saveFacilityFailure({error: error.message}))),
        ),
      ),
    ),
  );
}
