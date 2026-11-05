import {createEntityAdapter, EntityState} from '@ngrx/entity';
import {createReducer, on} from '@ngrx/store';
import {EhrPatient, Revision} from '../../models';
import {CallState} from '../call-state';
import {PatientActions} from './patient.actions';

export const patientFeatureKey = 'patient';

export interface PatientState extends EntityState<EhrPatient> {
  callState: CallState;
  history: Revision<EhrPatient>[];
}

export const adapter = createEntityAdapter<EhrPatient>({
  selectId: (patient) => patient.id ?? -1,
});

export const initialState: PatientState = adapter.getInitialState({
  callState: 'init' as CallState,
  history: [],
});

export const reducer = createReducer(
  initialState,
  on(PatientActions.loadPatients, PatientActions.loadPatient, (state) => ({
    ...state,
    callState: 'loading' as CallState,
  })),
  on(PatientActions.loadPatientsSuccess, (state, {patients}) =>
    adapter.setAll(patients, {...state, callState: 'loaded' as CallState}),
  ),
  on(PatientActions.loadPatientSuccess, (state, {patient}) =>
    adapter.upsertOne(patient, {...state, callState: 'loaded' as CallState}),
  ),
  on(PatientActions.updatePatientSuccess, (state, {patient}) =>
    adapter.upsertOne(patient, state),
  ),
  on(
    PatientActions.loadPatientsFailure,
    PatientActions.loadPatientFailure,
    PatientActions.savePatientFailure,
    (state, {error}) => ({...state, callState: {error}}),
  ),
  on(PatientActions.loadHistorySuccess, (state, {history}) => ({...state, history})),
  /** Changing facility invalidates the patient list entirely. */
  on(PatientActions.clear, () => initialState),
);
