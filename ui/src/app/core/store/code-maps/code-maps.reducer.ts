import {createReducer, on} from '@ngrx/store';
import {CodeSystem} from 'fhir/r5';
import {CodeBaseMap} from '../../models/code-base-map';
import {CallState} from '../call-state';
import {CodeMapsActions} from './code-maps.actions';

export const codeMapsFeatureKey = 'codeMaps';

export interface CodeMapsState {
  codeBaseMap: CodeBaseMap;
  identifierTypeCodeSystem: CodeSystem | null;
  qualificationCodeSystem: CodeSystem | null;
  callState: CallState;
}

export const initialState: CodeMapsState = {
  codeBaseMap: {},
  identifierTypeCodeSystem: null,
  qualificationCodeSystem: null,
  callState: 'init',
};

export const reducer = createReducer(
  initialState,
  on(CodeMapsActions.load, (state) => ({...state, callState: 'loading' as CallState})),
  on(
    CodeMapsActions.loadSuccess,
    (state, {codeBaseMap, identifierTypeCodeSystem, qualificationCodeSystem}) => ({
      ...state,
      codeBaseMap,
      identifierTypeCodeSystem,
      qualificationCodeSystem,
      callState: 'loaded' as CallState,
    }),
  ),
  on(CodeMapsActions.refreshSuccess, (state, {codeBaseMap}) => ({
    ...state,
    codeBaseMap,
    callState: 'loaded' as CallState,
  })),
  on(CodeMapsActions.loadFailure, (state, {error}) => ({...state, callState: {error}})),
);
