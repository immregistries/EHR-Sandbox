import {isDevMode} from '@angular/core';
import {ActionReducerMap, MetaReducer} from '@ngrx/store';
import {routerReducer, RouterReducerState} from '@ngrx/router-store';
import * as fromAuth from './auth/auth.reducer';
import * as fromClinician from './clinician/clinician.reducer';
import * as fromCodeMaps from './code-maps/code-maps.reducer';
import * as fromTenant from './tenant/tenant.reducer';
import * as fromFacility from './facility/facility.reducer';
import * as fromPatient from './patient/patient.reducer';
import * as fromVaccination from './vaccination/vaccination.reducer';
import * as fromRegistry from './registry/registry.reducer';

export interface AppState {
  router: RouterReducerState;
  [fromAuth.authFeatureKey]: fromAuth.AuthState;
  [fromTenant.tenantFeatureKey]: fromTenant.TenantState;
  [fromFacility.facilityFeatureKey]: fromFacility.FacilityState;
  [fromPatient.patientFeatureKey]: fromPatient.PatientState;
  [fromVaccination.vaccinationFeatureKey]: fromVaccination.VaccinationState;
  [fromRegistry.registryFeatureKey]: fromRegistry.RegistryState;
  [fromCodeMaps.codeMapsFeatureKey]: fromCodeMaps.CodeMapsState;
  [fromClinician.clinicianFeatureKey]: fromClinician.ClinicianState;
}

export const reducers: ActionReducerMap<AppState> = {
  router: routerReducer,
  [fromAuth.authFeatureKey]: fromAuth.reducer,
  [fromTenant.tenantFeatureKey]: fromTenant.reducer,
  [fromFacility.facilityFeatureKey]: fromFacility.reducer,
  [fromPatient.patientFeatureKey]: fromPatient.reducer,
  [fromVaccination.vaccinationFeatureKey]: fromVaccination.reducer,
  [fromRegistry.registryFeatureKey]: fromRegistry.reducer,
  [fromCodeMaps.codeMapsFeatureKey]: fromCodeMaps.reducer,
  [fromClinician.clinicianFeatureKey]: fromClinician.reducer,
};

export const metaReducers: MetaReducer<AppState>[] = isDevMode() ? [] : [];
