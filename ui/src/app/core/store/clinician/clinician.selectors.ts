import {createFeatureSelector, createSelector} from '@ngrx/store';
import {getError, isLoading} from '../call-state';
import {adapter, ClinicianState, clinicianFeatureKey} from './clinician.reducer';

export const selectClinicianState = createFeatureSelector<ClinicianState>(clinicianFeatureKey);

const {selectAll, selectEntities} = adapter.getSelectors();

export const selectAllClinicians = createSelector(selectClinicianState, selectAll);
export const selectClinicianEntities = createSelector(selectClinicianState, selectEntities);
export const selectCliniciansLoading = createSelector(selectClinicianState, (s) =>
  isLoading(s.callState),
);
export const selectCliniciansError = createSelector(selectClinicianState, (s) =>
  getError(s.callState),
);

/** Ready for a p-select bound to a clinician id. */
export const selectClinicianOptions = createSelector(selectAllClinicians, (clinicians) =>
  clinicians.map((clinician) => ({
    id: clinician.id,
    label:
      [clinician.namePrefix, clinician.nameFirst, clinician.nameLast]
        .filter(Boolean)
        .join(' ') || `Clinician ${clinician.id}`,
  })),
);
