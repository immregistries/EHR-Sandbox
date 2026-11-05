import {createFeatureSelector, createSelector} from '@ngrx/store';
import {getError, isLoading} from '../call-state';
import {authFeatureKey, AuthState} from './auth.reducer';

export const selectAuthState = createFeatureSelector<AuthState>(authFeatureKey);

export const selectIsAuthenticated = createSelector(selectAuthState, (s) => s.authenticated);
export const selectUsername = createSelector(selectAuthState, (s) => s.username);
export const selectAuthLoading = createSelector(selectAuthState, (s) => isLoading(s.callState));
export const selectAuthError = createSelector(selectAuthState, (s) => getError(s.callState));
