import {createFeatureSelector, createSelector} from '@ngrx/store';
import {getError, isLoading} from '../call-state';
import {selectRouteFacilityId} from '../router.selectors';
import {adapter, facilityFeatureKey, FacilityState} from './facility.reducer';

export const selectFacilityState = createFeatureSelector<FacilityState>(facilityFeatureKey);

const {selectAll, selectEntities} = adapter.getSelectors();

export const selectAllFacilities = createSelector(selectFacilityState, selectAll);
export const selectFacilityEntities = createSelector(selectFacilityState, selectEntities);
export const selectFacilitiesLoading = createSelector(selectFacilityState, (s) =>
  isLoading(s.callState),
);
export const selectFacilitiesError = createSelector(selectFacilityState, (s) =>
  getError(s.callState),
);

/** Top-level facilities only — children are reached through the tree. */
export const selectRootFacilities = createSelector(selectAllFacilities, (facilities) =>
  facilities.filter((f) => !f.parentFacility),
);

export const selectCurrentFacility = createSelector(
  selectFacilityEntities,
  selectRouteFacilityId,
  (entities, facilityId) => (facilityId > 0 ? entities[facilityId] : undefined),
);

export const selectCurrentFacilityId = selectRouteFacilityId;
