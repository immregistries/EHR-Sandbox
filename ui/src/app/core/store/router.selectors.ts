import {createFeatureSelector, createSelector} from '@ngrx/store';
import {getRouterSelectors, RouterReducerState} from '@ngrx/router-store';

export const selectRouterState = createFeatureSelector<RouterReducerState>('router');

export const {
  selectCurrentRoute,
  selectUrl,
  selectQueryParams,
  selectRouteParams,
  selectRouteData,
} = getRouterSelectors(selectRouterState);

/** Parses an id route param, returning -1 when absent or malformed. */
const numericParam = (name: string) =>
  createSelector(selectRouteParams, (params): number => {
    const raw = params?.[name];
    const parsed = Number(raw);
    return raw !== undefined && Number.isFinite(parsed) ? parsed : -1;
  });

/** Tenant/facility context is owned by the URL — these are the source of truth. */
export const selectRouteTenantId = numericParam('tenantId');
export const selectRouteFacilityId = numericParam('facilityId');
export const selectRoutePatientId = numericParam('patientId');
export const selectRouteVaccinationId = numericParam('vaccinationId');

export const selectHasTenantContext = createSelector(selectRouteTenantId, (id) => id > 0);
export const selectHasFacilityContext = createSelector(
  selectRouteTenantId,
  selectRouteFacilityId,
  (tenantId, facilityId) => tenantId > 0 && facilityId > 0,
);
