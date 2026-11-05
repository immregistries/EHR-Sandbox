import {createFeatureSelector, createSelector} from '@ngrx/store';
import {getError, isLoading} from '../call-state';
import {adapter, registryFeatureKey, RegistryState} from './registry.reducer';

export const selectRegistryState = createFeatureSelector<RegistryState>(registryFeatureKey);

const {selectAll, selectEntities} = adapter.getSelectors();

export const selectAllRegistries = createSelector(selectRegistryState, selectAll);
export const selectRegistryEntities = createSelector(selectRegistryState, selectEntities);
export const selectRegistriesLoading = createSelector(selectRegistryState, (s) =>
  isLoading(s.callState),
);
export const selectRegistriesError = createSelector(selectRegistryState, (s) =>
  getError(s.callState),
);

export const selectCurrentRegistryId = createSelector(selectRegistryState, (s) => s.selectedId);

export const selectCurrentRegistry = createSelector(
  selectRegistryEntities,
  selectCurrentRegistryId,
  (entities, id) => (id ? entities[id] : undefined),
);
