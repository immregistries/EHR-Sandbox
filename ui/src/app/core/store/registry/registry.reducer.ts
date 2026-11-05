import {createEntityAdapter, EntityState} from '@ngrx/entity';
import {createReducer, on} from '@ngrx/store';
import {ImmunizationRegistry} from '../../models';
import {CallState} from '../call-state';
import {RegistryActions} from './registry.actions';

export const registryFeatureKey = 'registry';

export interface RegistryState extends EntityState<ImmunizationRegistry> {
  callState: CallState;
  selectedId: number | null;
}

export const adapter = createEntityAdapter<ImmunizationRegistry>({
  selectId: (registry) => registry.id ?? -1,
});

export const initialState: RegistryState = adapter.getInitialState({
  callState: 'init' as CallState,
  selectedId: null,
});

export const reducer = createReducer(
  initialState,
  on(RegistryActions.loadRegistries, (state) => ({
    ...state,
    callState: 'loading' as CallState,
  })),
  on(RegistryActions.loadRegistriesSuccess, (state, {registries}) =>
    adapter.setAll(registries, {
      ...state,
      callState: 'loaded' as CallState,
      // Fall back to the registry flagged as default by the API.
      selectedId: state.selectedId ?? registries.find((r) => r.default)?.id ?? null,
    }),
  ),
  on(RegistryActions.selectRegistry, (state, {registryId}) => ({
    ...state,
    selectedId: registryId,
  })),
  on(RegistryActions.saveRegistrySuccess, (state, {registry}) =>
    adapter.upsertOne(registry, state),
  ),
  on(RegistryActions.deleteRegistrySuccess, (state, {registryId}) =>
    adapter.removeOne(registryId, {
      ...state,
      selectedId: state.selectedId === registryId ? null : state.selectedId,
    }),
  ),
  on(
    RegistryActions.loadRegistriesFailure,
    RegistryActions.saveRegistryFailure,
    (state, {error}) => ({...state, callState: {error}}),
  ),
);
