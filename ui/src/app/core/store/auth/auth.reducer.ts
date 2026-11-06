import {createReducer, on} from '@ngrx/store';
import {CallState} from '../call-state';
import {AuthActions} from './auth.actions';

export const authFeatureKey = 'auth';

export interface AuthState {
  username: string | null;
  authenticated: boolean;
  callState: CallState;
}

export const initialState: AuthState = {
  username: null,
  authenticated: false,
  callState: 'init',
};

export const reducer = createReducer(
  initialState,
  on(AuthActions.login, (state) => ({...state, callState: 'loading' as CallState})),
  on(AuthActions.restoreSession, (state) => ({...state, callState: 'loading' as CallState})),
  on(AuthActions.loginSuccess, (state, {username}) => ({
    ...state,
    username,
    authenticated: true,
    callState: 'loaded' as CallState,
  })),
  on(AuthActions.restoreSessionSuccess, (state, {username}) => ({
    ...state,
    username,
    authenticated: true,
    callState: 'loaded' as CallState,
  })),
  on(AuthActions.loginFailure, (state, {error}) => ({
    ...initialState,
    callState: {error},
  })),
  on(AuthActions.logout, AuthActions.sessionExpired, () => initialState),
  // Not signed in, but the probe has answered — guards must not keep waiting.
  on(AuthActions.restoreSessionFailure, () => ({
    ...initialState,
    callState: 'loaded' as CallState,
  })),
);
