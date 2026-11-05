import {createFeatureSelector, createSelector} from '@ngrx/store';
import {getError, isLoading} from '../call-state';
import {selectRoutePatientId} from '../router.selectors';
import {adapter, patientFeatureKey, PatientState} from './patient.reducer';

export const selectPatientState = createFeatureSelector<PatientState>(patientFeatureKey);

const {selectAll, selectEntities} = adapter.getSelectors();

export const selectAllPatients = createSelector(selectPatientState, selectAll);
export const selectPatientEntities = createSelector(selectPatientState, selectEntities);
export const selectPatientsLoading = createSelector(selectPatientState, (s) =>
  isLoading(s.callState),
);
export const selectPatientsError = createSelector(selectPatientState, (s) => getError(s.callState));
export const selectPatientHistory = createSelector(selectPatientState, (s) => s.history);

export const selectCurrentPatient = createSelector(
  selectPatientEntities,
  selectRoutePatientId,
  (entities, patientId) => (patientId > 0 ? entities[patientId] : undefined),
);

export const selectCurrentPatientId = selectRoutePatientId;
