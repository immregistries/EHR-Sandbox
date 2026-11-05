/**
 * Reducer exemplar: call the reducer directly, assert on the next state.
 * No TestBed — reducers are pure functions.
 */
import {aPatient, resetFixtureIds} from '../../../../testing';
import {PatientActions} from './patient.actions';
import {adapter, initialState, PatientState, reducer} from './patient.reducer';

describe('patient reducer', () => {
  beforeEach(() => resetFixtureIds());

  it('returns the initial state for an unknown action', () => {
    const state = reducer(undefined, {type: 'unknown'});
    expect(state).toBe(initialState);
  });

  it('marks the slice as loading while patients are fetched', () => {
    const state = reducer(initialState, PatientActions.loadPatients({tenantId: 1, facilityId: 2}));
    expect(state.callState).toBe('loading');
  });

  it('replaces the collection on load success', () => {
    const patients = [aPatient({id: 1}), aPatient({id: 2})];
    const loading = reducer(
      initialState,
      PatientActions.loadPatients({tenantId: 1, facilityId: 2}),
    );

    const state = reducer(loading, PatientActions.loadPatientsSuccess({patients}));

    expect(state.callState).toBe('loaded');
    expect(adapter.getSelectors().selectAll(state)).toEqual(patients);
  });

  it('upserts rather than replaces on single patient success', () => {
    const existing = reducer(
      initialState,
      PatientActions.loadPatientsSuccess({patients: [aPatient({id: 1}), aPatient({id: 2})]}),
    );
    const updated = aPatient({id: 2, sex: 'M'});

    const state = reducer(existing, PatientActions.loadPatientSuccess({patient: updated}));

    expect(state.ids.length).toBe(2);
    expect(state.entities[2]?.sex).toBe('M');
  });

  it('records the error message on failure', () => {
    const state = reducer(initialState, PatientActions.loadPatientsFailure({error: 'boom'}));
    expect(state.callState).toEqual({error: 'boom'});
  });

  it('drops every patient when the slice is cleared', () => {
    const populated: PatientState = reducer(
      initialState,
      PatientActions.loadPatientsSuccess({patients: [aPatient()]}),
    );

    expect(reducer(populated, PatientActions.clear())).toEqual(initialState);
  });
});
