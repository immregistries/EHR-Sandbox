import {inject, Injectable} from '@angular/core';
import {Actions, createEffect, ofType} from '@ngrx/effects';
import {concatLatestFrom} from '@ngrx/operators';
import {Store} from '@ngrx/store';
import {catchError, filter, map, of, switchMap} from 'rxjs';
import {PatientService} from '../../services';
import {selectRouteFacilityId, selectRouteTenantId} from '../router.selectors';
import {PatientActions} from './patient.actions';

@Injectable()
export class PatientEffects {
  private readonly actions$ = inject(Actions);
  private readonly store = inject(Store);
  private readonly patientService = inject(PatientService);

  loadPatients$ = createEffect(() =>
    this.actions$.pipe(
      ofType(PatientActions.loadPatients),
      switchMap(({tenantId, facilityId}) =>
        this.patientService.readPatients(tenantId, facilityId).pipe(
          map((patients) => PatientActions.loadPatientsSuccess({patients})),
          catchError((error) => of(PatientActions.loadPatientsFailure({error: error.message}))),
        ),
      ),
    ),
  );

  refresh$ = createEffect(() =>
    this.actions$.pipe(
      ofType(PatientActions.refresh),
      concatLatestFrom(() => [
        this.store.select(selectRouteTenantId),
        this.store.select(selectRouteFacilityId),
      ]),
      filter(([, tenantId, facilityId]) => tenantId > 0 && facilityId > 0),
      map(([, tenantId, facilityId]) => PatientActions.loadPatients({tenantId, facilityId})),
    ),
  );

  loadPatient$ = createEffect(() =>
    this.actions$.pipe(
      ofType(PatientActions.loadPatient),
      switchMap(({tenantId, facilityId, patientId}) =>
        this.patientService.readPatient(tenantId, facilityId, patientId).pipe(
          map((patient) => PatientActions.loadPatientSuccess({patient})),
          catchError((error) => of(PatientActions.loadPatientFailure({error: error.message}))),
        ),
      ),
    ),
  );

  createPatient$ = createEffect(() =>
    this.actions$.pipe(
      ofType(PatientActions.createPatient),
      switchMap(({tenantId, facilityId, patient}) =>
        this.patientService.postPatient(tenantId, facilityId, patient).pipe(
          map((response) => PatientActions.createPatientSuccess({patientId: Number(response.body)})),
          catchError((error) => of(PatientActions.savePatientFailure({error: error.message}))),
        ),
      ),
    ),
  );

  /** The API returns only the new id, so the list is re-read afterwards. */
  reloadAfterCreate$ = createEffect(() =>
    this.actions$.pipe(
      ofType(PatientActions.createPatientSuccess),
      map(() => PatientActions.refresh()),
    ),
  );

  updatePatient$ = createEffect(() =>
    this.actions$.pipe(
      ofType(PatientActions.updatePatient),
      switchMap(({tenantId, facilityId, patient}) =>
        this.patientService.putPatient(tenantId, facilityId, patient).pipe(
          map((updated) => PatientActions.updatePatientSuccess({patient: updated})),
          catchError((error) => of(PatientActions.savePatientFailure({error: error.message}))),
        ),
      ),
    ),
  );

  loadHistory$ = createEffect(() =>
    this.actions$.pipe(
      ofType(PatientActions.loadHistory),
      switchMap(({tenantId, facilityId, patientId}) =>
        this.patientService.readPatientHistory(tenantId, facilityId, patientId).pipe(
          map((history) => PatientActions.loadHistorySuccess({history})),
          catchError(() => of(PatientActions.loadHistorySuccess({history: []}))),
        ),
      ),
    ),
  );
}
