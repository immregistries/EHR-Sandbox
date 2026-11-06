import {inject, Injectable} from '@angular/core';
import {Actions, createEffect, ofType} from '@ngrx/effects';
import {concatLatestFrom} from '@ngrx/operators';
import {Store} from '@ngrx/store';
import {catchError, filter, map, of, switchMap} from 'rxjs';
import {ClinicianService} from '../../services';
import {selectRouteTenantId} from '../router.selectors';
import {ClinicianActions} from './clinician.actions';

@Injectable()
export class ClinicianEffects {
  private readonly actions$ = inject(Actions);
  private readonly store = inject(Store);
  private readonly clinicianService = inject(ClinicianService);

  loadClinicians$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ClinicianActions.loadClinicians),
      switchMap(({tenantId}) =>
        this.clinicianService.readClinicians(tenantId).pipe(
          map((clinicians) => ClinicianActions.loadCliniciansSuccess({clinicians})),
          catchError((error) =>
            of(ClinicianActions.loadCliniciansFailure({error: error.message})),
          ),
        ),
      ),
    ),
  );

  refresh$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ClinicianActions.refresh),
      concatLatestFrom(() => this.store.select(selectRouteTenantId)),
      filter(([, tenantId]) => tenantId > 0),
      map(([, tenantId]) => ClinicianActions.loadClinicians({tenantId})),
    ),
  );

  saveClinician$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ClinicianActions.saveClinician),
      switchMap(({tenantId, clinician}) =>
        (clinician.id
            ? this.clinicianService.putClinician(tenantId, clinician)
            : this.clinicianService.postClinician(tenantId, clinician)
        ).pipe(
          map((saved) => ClinicianActions.saveClinicianSuccess({clinician: saved})),
          catchError((error) => of(ClinicianActions.saveClinicianFailure({error: error.message}))),
        ),
      ),
    ),
  );
}
