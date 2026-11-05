import {createEntityAdapter, EntityState} from '@ngrx/entity';
import {createReducer, on} from '@ngrx/store';
import {Facility} from '../../models';
import {CallState} from '../call-state';
import {FacilityActions} from './facility.actions';

export const facilityFeatureKey = 'facility';

export interface FacilityState extends EntityState<Facility> {
  callState: CallState;
}

export const adapter = createEntityAdapter<Facility>({
  selectId: (facility) => facility.id ?? -1,
  sortComparer: (a, b) => (a.nameDisplay ?? '').localeCompare(b.nameDisplay ?? ''),
});

export const initialState: FacilityState = adapter.getInitialState({
  callState: 'init' as CallState,
});

export const reducer = createReducer(
  initialState,
  on(FacilityActions.loadFacilities, FacilityActions.loadFacility, (state) => ({
    ...state,
    callState: 'loading' as CallState,
  })),
  on(FacilityActions.loadFacilitiesSuccess, (state, {facilities}) =>
    adapter.setAll(facilities, {...state, callState: 'loaded' as CallState}),
  ),
  on(FacilityActions.loadFacilitySuccess, (state, {facility}) =>
    adapter.upsertOne(facility, {...state, callState: 'loaded' as CallState}),
  ),
  on(
    FacilityActions.createFacilitySuccess,
    FacilityActions.updateFacilitySuccess,
    (state, {facility}) => adapter.upsertOne(facility, state),
  ),
  on(
    FacilityActions.loadFacilitiesFailure,
    FacilityActions.loadFacilityFailure,
    FacilityActions.saveFacilityFailure,
    (state, {error}) => ({...state, callState: {error}}),
  ),
  on(FacilityActions.clear, () => initialState),
);
