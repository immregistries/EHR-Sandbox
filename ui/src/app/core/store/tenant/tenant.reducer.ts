import {createEntityAdapter, EntityState} from '@ngrx/entity';
import {createReducer, on} from '@ngrx/store';
import {Flavor, Tenant} from '../../models';
import {CallState} from '../call-state';
import {TenantActions} from './tenant.actions';

export const tenantFeatureKey = 'tenant';

export interface TenantState extends EntityState<Tenant> {
  callState: CallState;
  flavors: Flavor[];
}

export const adapter = createEntityAdapter<Tenant>({
  selectId: (tenant) => tenant.id,
  sortComparer: (a, b) => (a.nameDisplay ?? '').localeCompare(b.nameDisplay ?? ''),
});

export const initialState: TenantState = adapter.getInitialState({
  callState: 'init' as CallState,
  flavors: [],
});

export const reducer = createReducer(
  initialState,
  on(TenantActions.loadTenants, TenantActions.loadTenant, (state) => ({
    ...state,
    callState: 'loading' as CallState,
  })),
  on(TenantActions.loadTenantsSuccess, (state, {tenants}) =>
    adapter.setAll(tenants, {...state, callState: 'loaded' as CallState}),
  ),
  on(TenantActions.loadTenantSuccess, (state, {tenant}) =>
    adapter.upsertOne(tenant, {...state, callState: 'loaded' as CallState}),
  ),
  on(TenantActions.createTenantSuccess, (state, {tenant}) => adapter.upsertOne(tenant, state)),
  on(
    TenantActions.loadTenantsFailure,
    TenantActions.loadTenantFailure,
    TenantActions.createTenantFailure,
    (state, {error}) => ({...state, callState: {error}}),
  ),
  on(TenantActions.loadFlavorsSuccess, (state, {flavors}) => ({...state, flavors})),
);
