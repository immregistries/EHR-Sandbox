import {createFeatureSelector, createSelector} from '@ngrx/store';
import {getError, isLoading} from '../call-state';
import {authFeatureKey, AuthState} from './auth.reducer';

export const selectAuthState = createFeatureSelector<AuthState>(authFeatureKey);

export const selectIsAuthenticated = createSelector(selectAuthState, (s) => s.authenticated);
export const selectUsername = createSelector(selectAuthState, (s) => s.username);
export const selectAuthLoading = createSelector(selectAuthState, (s) => isLoading(s.callState));
export const selectAuthError = createSelector(selectAuthState, (s) => getError(s.callState));

/**
 * Whether the session is known, and what it is. Guards wait for `resolved`
 * so a reload does not redirect before the probe answers, and start a probe
 * only when the session is still `unknown`.
 */
export const selectSessionStatus = createSelector(selectAuthState, (s) => ({
  /** Nothing known yet and no probe running — one needs to be started. */
  unknown: s.callState === 'init' && !s.authenticated,
  /** Either the probe settled, or a login already established a session. */
  resolved: s.authenticated || (s.callState !== 'init' && s.callState !== 'loading'),
  authenticated: s.authenticated,
}));
