import {createFeatureSelector, createSelector} from '@ngrx/store';
import {Clinician} from '../../models';
import {getError, isLoading} from '../call-state';
import {adapter, clinicianFeatureKey, ClinicianState} from './clinician.reducer';

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

/** A clinician's display name, as the old UI rendered it. */
export function clinicianLabel(clinician: Clinician): string {
  return (
    [clinician.namePrefix, clinician.nameFirst, clinician.nameLast].filter(Boolean).join(' ') ||
    `Clinician ${clinician.id}`
  );
}

/**
 * Full clinicians carrying a label. The select binds the whole object, not
 * an id: the API stores a reference but expects the entity back, and an
 * `{id}` stub is an unresolvable detached entity that fails to save.
 */
export const selectClinicianOptions = createSelector(selectAllClinicians, (clinicians) =>
  clinicians.map((clinician) => ({...clinician, label: clinicianLabel(clinician)})),
);
