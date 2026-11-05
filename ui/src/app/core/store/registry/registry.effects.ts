import {inject, Injectable} from '@angular/core';
import {Actions, createEffect, ofType} from '@ngrx/effects';
import {catchError, map, of, switchMap} from 'rxjs';
import {ImmunizationRegistryService} from '../../services';
import {RegistryActions} from './registry.actions';

@Injectable()
export class RegistryEffects {
  private readonly actions$ = inject(Actions);
  private readonly registryService = inject(ImmunizationRegistryService);

  loadRegistries$ = createEffect(() =>
    this.actions$.pipe(
      ofType(RegistryActions.loadRegistries),
      switchMap(() =>
        this.registryService.readImmRegistries().pipe(
          map((registries) => RegistryActions.loadRegistriesSuccess({registries})),
          catchError((error) => of(RegistryActions.loadRegistriesFailure({error: error.message}))),
        ),
      ),
    ),
  );

  saveRegistry$ = createEffect(() =>
    this.actions$.pipe(
      ofType(RegistryActions.saveRegistry),
      switchMap(({registry}) =>
        (registry.id
            ? this.registryService.putImmRegistry(registry)
            : this.registryService.postImmRegistry(registry)
        ).pipe(
          map((saved) => RegistryActions.saveRegistrySuccess({registry: saved})),
          catchError((error) => of(RegistryActions.saveRegistryFailure({error: error.message}))),
        ),
      ),
    ),
  );

  deleteRegistry$ = createEffect(() =>
    this.actions$.pipe(
      ofType(RegistryActions.deleteRegistry),
      switchMap(({registryId}) =>
        this.registryService.deleteImmRegistry(registryId).pipe(
          map(() => RegistryActions.deleteRegistrySuccess({registryId})),
          catchError((error) => of(RegistryActions.saveRegistryFailure({error: error.message}))),
        ),
      ),
    ),
  );
}
