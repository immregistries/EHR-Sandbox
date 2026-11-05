import {createEntityAdapter, EntityState} from '@ngrx/entity';
import {createReducer, on} from '@ngrx/store';
import {VaccinationEvent} from '../../models';
import {CallState} from '../call-state';
import {VaccinationActions} from './vaccination.actions';

export const vaccinationFeatureKey = 'vaccination';

export interface VaccinationState extends EntityState<VaccinationEvent> {
  callState: CallState;
}

export const adapter = createEntityAdapter<VaccinationEvent>({
  selectId: (vaccination) => vaccination.id ?? -1,
  sortComparer: (a, b) =>
    new Date(b.vaccine?.administeredDate ?? 0).getTime() -
    new Date(a.vaccine?.administeredDate ?? 0).getTime(),
});

export const initialState: VaccinationState = adapter.getInitialState({
  callState: 'init' as CallState,
});

export const reducer = createReducer(
  initialState,
  on(
    VaccinationActions.loadVaccinations,
    VaccinationActions.loadFacilityVaccinations,
    VaccinationActions.loadVaccination,
    (state) => ({...state, callState: 'loading' as CallState}),
  ),
  on(VaccinationActions.loadVaccinationsSuccess, (state, {vaccinations}) =>
    adapter.setAll(vaccinations, {...state, callState: 'loaded' as CallState}),
  ),
  on(VaccinationActions.loadVaccinationSuccess, (state, {vaccination}) =>
    adapter.upsertOne(vaccination, {...state, callState: 'loaded' as CallState}),
  ),
  on(VaccinationActions.updateVaccinationSuccess, (state, {vaccination}) =>
    adapter.upsertOne(vaccination, state),
  ),
  on(
    VaccinationActions.loadVaccinationsFailure,
    VaccinationActions.saveVaccinationFailure,
    (state, {error}) => ({...state, callState: {error}}),
  ),
  on(VaccinationActions.clear, () => initialState),
);
