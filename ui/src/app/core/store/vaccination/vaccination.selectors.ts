import {createFeatureSelector, createSelector} from '@ngrx/store';
import {getError, isLoading} from '../call-state';
import {selectRouteVaccinationId} from '../router.selectors';
import {adapter, vaccinationFeatureKey, VaccinationState} from './vaccination.reducer';

export const selectVaccinationState =
  createFeatureSelector<VaccinationState>(vaccinationFeatureKey);

const {selectAll, selectEntities} = adapter.getSelectors();

export const selectAllVaccinations = createSelector(selectVaccinationState, selectAll);
export const selectVaccinationEntities = createSelector(selectVaccinationState, selectEntities);
export const selectVaccinationsLoading = createSelector(selectVaccinationState, (s) =>
  isLoading(s.callState),
);
export const selectVaccinationsError = createSelector(selectVaccinationState, (s) =>
  getError(s.callState),
);

export const selectCurrentVaccination = createSelector(
  selectVaccinationEntities,
  selectRouteVaccinationId,
  (entities, id) => (id > 0 ? entities[id] : undefined),
);
