import {inject, Injectable} from '@angular/core';
import {Actions, createEffect, ofType} from '@ngrx/effects';
import {concatLatestFrom} from '@ngrx/operators';
import {Store} from '@ngrx/store';
import {catchError, filter, map, of, switchMap} from 'rxjs';
import {VaccinationService} from '../../services';
import {selectRouteFacilityId, selectRoutePatientId, selectRouteTenantId,} from '../router.selectors';
import {VaccinationActions} from './vaccination.actions';

@Injectable()
export class VaccinationEffects {
  private readonly actions$ = inject(Actions);
  private readonly store = inject(Store);
  private readonly vaccinationService = inject(VaccinationService);

  loadVaccinations$ = createEffect(() =>
    this.actions$.pipe(
      ofType(VaccinationActions.loadVaccinations),
      switchMap(({tenantId, facilityId, patientId}) =>
        this.vaccinationService.readVaccinations(tenantId, facilityId, patientId).pipe(
          map((vaccinations) => VaccinationActions.loadVaccinationsSuccess({vaccinations})),
          catchError((error) =>
            of(VaccinationActions.loadVaccinationsFailure({error: error.message})),
          ),
        ),
      ),
    ),
  );

  loadFacilityVaccinations$ = createEffect(() =>
    this.actions$.pipe(
      ofType(VaccinationActions.loadFacilityVaccinations),
      switchMap(({tenantId, facilityId}) =>
        this.vaccinationService.readVaccinationsFromFacility(tenantId, facilityId).pipe(
          map((vaccinations) => VaccinationActions.loadVaccinationsSuccess({vaccinations})),
          catchError((error) =>
            of(VaccinationActions.loadVaccinationsFailure({error: error.message})),
          ),
        ),
      ),
    ),
  );

  /** Refreshes the patient's list when one is selected, else the facility's. */
  refresh$ = createEffect(() =>
    this.actions$.pipe(
      ofType(VaccinationActions.refresh),
      concatLatestFrom(() => [
        this.store.select(selectRouteTenantId),
        this.store.select(selectRouteFacilityId),
        this.store.select(selectRoutePatientId),
      ]),
      filter(([, tenantId, facilityId]) => tenantId > 0 && facilityId > 0),
      map(([, tenantId, facilityId, patientId]) =>
        patientId > 0
          ? VaccinationActions.loadVaccinations({tenantId, facilityId, patientId})
          : VaccinationActions.loadFacilityVaccinations({tenantId, facilityId}),
      ),
    ),
  );

  loadVaccination$ = createEffect(() =>
    this.actions$.pipe(
      ofType(VaccinationActions.loadVaccination),
      switchMap(({tenantId, facilityId, patientId, vaccinationId}) =>
        this.vaccinationService
          .readVaccination(tenantId, facilityId, patientId, vaccinationId)
          .pipe(
            map((vaccination) => VaccinationActions.loadVaccinationSuccess({vaccination})),
            catchError((error) =>
              of(VaccinationActions.loadVaccinationsFailure({error: error.message})),
            ),
          ),
      ),
    ),
  );

  createVaccination$ = createEffect(() =>
    this.actions$.pipe(
      ofType(VaccinationActions.createVaccination),
      switchMap(({tenantId, facilityId, patientId, vaccination}) =>
        this.vaccinationService.postVaccination(tenantId, facilityId, patientId, vaccination).pipe(
          map((response) =>
            VaccinationActions.createVaccinationSuccess({vaccinationId: Number(response.body)}),
          ),
          catchError((error) =>
            of(VaccinationActions.saveVaccinationFailure({error: error.message})),
          ),
        ),
      ),
    ),
  );

  reloadAfterCreate$ = createEffect(() =>
    this.actions$.pipe(
      ofType(VaccinationActions.createVaccinationSuccess),
      map(() => VaccinationActions.refresh()),
    ),
  );

  updateVaccination$ = createEffect(() =>
    this.actions$.pipe(
      ofType(VaccinationActions.updateVaccination),
      switchMap(({tenantId, facilityId, patientId, vaccination}) =>
        this.vaccinationService.putVaccination(tenantId, facilityId, patientId, vaccination).pipe(
          map((updated) => VaccinationActions.updateVaccinationSuccess({vaccination: updated})),
          catchError((error) =>
            of(VaccinationActions.saveVaccinationFailure({error: error.message})),
          ),
        ),
      ),
    ),
  );
}
