import {createFeatureSelector, createSelector} from '@ngrx/store';
import {getError, isLoading} from '../call-state';
import {selectRouteTenantId} from '../router.selectors';
import {adapter, tenantFeatureKey, TenantState} from './tenant.reducer';

export const selectTenantState = createFeatureSelector<TenantState>(tenantFeatureKey);

const {selectAll, selectEntities} = adapter.getSelectors();

export const selectAllTenants = createSelector(selectTenantState, selectAll);
export const selectTenantEntities = createSelector(selectTenantState, selectEntities);
export const selectTenantsLoading = createSelector(selectTenantState, (s) => isLoading(s.callState));
export const selectTenantsError = createSelector(selectTenantState, (s) => getError(s.callState));
export const selectFlavors = createSelector(selectTenantState, (s) => s.flavors);

/**
 * The selected tenant is derived from the URL, never from a separate
 * "current selection" subject — navigating is what changes context.
 */
export const selectCurrentTenant = createSelector(
  selectTenantEntities,
  selectRouteTenantId,
  (entities, tenantId) => (tenantId > 0 ? entities[tenantId] : undefined),
);

export const selectCurrentTenantId = selectRouteTenantId;
