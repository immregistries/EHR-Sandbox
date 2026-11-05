import {inject, Injectable} from '@angular/core';
import {Actions, createEffect, ofType} from '@ngrx/effects';
import {concatLatestFrom} from '@ngrx/operators';
import {Store} from '@ngrx/store';
import {catchError, map, of, switchMap} from 'rxjs';
import {TenantService} from '../../services';
import {selectRouteTenantId} from '../router.selectors';
import {TenantActions} from './tenant.actions';

@Injectable()
export class TenantEffects {
  private readonly actions$ = inject(Actions);
  private readonly store = inject(Store);
  private readonly tenantService = inject(TenantService);

  loadTenants$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TenantActions.loadTenants, TenantActions.refresh),
      switchMap(() =>
        this.tenantService.readTenants().pipe(
          map((tenants) => TenantActions.loadTenantsSuccess({tenants})),
          catchError((error) => of(TenantActions.loadTenantsFailure({error: error.message}))),
        ),
      ),
    ),
  );

  loadTenant$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TenantActions.loadTenant),
      switchMap(({tenantId}) =>
        this.tenantService.readTenant(tenantId).pipe(
          map((tenant) => TenantActions.loadTenantSuccess({tenant})),
          catchError((error) => of(TenantActions.loadTenantFailure({error: error.message}))),
        ),
      ),
    ),
  );

  createTenant$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TenantActions.createTenant),
      switchMap(({tenant}) =>
        this.tenantService.postTenant(tenant).pipe(
          map((response) => TenantActions.createTenantSuccess({tenant: response.body ?? tenant})),
          catchError((error) => of(TenantActions.createTenantFailure({error: error.message}))),
        ),
      ),
    ),
  );

  loadFlavors$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TenantActions.loadFlavors),
      switchMap(() =>
        this.tenantService.readAllFlavors().pipe(
          map((flavors) => TenantActions.loadFlavorsSuccess({flavors})),
          catchError(() => of(TenantActions.loadFlavorsSuccess({flavors: []}))),
        ),
      ),
    ),
  );

  /** Ensures the tenant named in the url is present in the store. */
  hydrateFromRoute$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TenantActions.loadTenantsSuccess),
      concatLatestFrom(() => this.store.select(selectRouteTenantId)),
      map(([{tenants}, tenantId]) =>
        tenantId > 0 && !tenants.some((t) => t.id === tenantId)
          ? TenantActions.loadTenant({tenantId})
          : TenantActions.loadFlavors(),
      ),
    ),
  );
}
