import {createEntityAdapter, EntityState} from '@ngrx/entity';
import {createReducer, on} from '@ngrx/store';
import {Clinician} from '../../models';
import {CallState} from '../call-state';
import {ClinicianActions} from './clinician.actions';

export const clinicianFeatureKey = 'clinician';

export interface ClinicianState extends EntityState<Clinician> {
  callState: CallState;
}

export const adapter = createEntityAdapter<Clinician>({
  selectId: (clinician) => clinician.id ?? -1,
  sortComparer: (a, b) => (a.nameLast ?? '').localeCompare(b.nameLast ?? ''),
});

export const initialState: ClinicianState = adapter.getInitialState({
  callState: 'init' as CallState,
});

export const reducer = createReducer(
  initialState,
  on(ClinicianActions.loadClinicians, (state) => ({...state, callState: 'loading' as CallState})),
  on(ClinicianActions.loadCliniciansSuccess, (state, {clinicians}) =>
    adapter.setAll(clinicians, {...state, callState: 'loaded' as CallState}),
  ),
  on(ClinicianActions.saveClinicianSuccess, (state, {clinician}) =>
    adapter.upsertOne(clinician, state),
  ),
  on(
    ClinicianActions.loadCliniciansFailure,
    ClinicianActions.saveClinicianFailure,
    (state, {error}) => ({...state, callState: {error}}),
  ),
  on(ClinicianActions.clear, () => initialState),
);
