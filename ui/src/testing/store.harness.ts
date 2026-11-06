/**
 * Store testing harness.
 *
 * `appStateFactory()` builds a complete, typed AppState so a spec overrides
 * only the slice it exercises — selectors that cross slices (for example
 * code-maps reading the tenant from the router) keep working.
 */
import {Provider} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {Action, MemoizedSelector} from '@ngrx/store';
import {MockStore, provideMockStore} from '@ngrx/store/testing';
import {RouterReducerState} from '@ngrx/router-store';

import {AppState} from '../app/core/store';
import * as fromAuth from '../app/core/store/auth/auth.reducer';
import * as fromClinician from '../app/core/store/clinician/clinician.reducer';
import * as fromCodeMaps from '../app/core/store/code-maps/code-maps.reducer';
import * as fromFacility from '../app/core/store/facility/facility.reducer';
import * as fromPatient from '../app/core/store/patient/patient.reducer';
import * as fromRegistry from '../app/core/store/registry/registry.reducer';
import * as fromTenant from '../app/core/store/tenant/tenant.reducer';
import * as fromVaccination from '../app/core/store/vaccination/vaccination.reducer';

export interface RouteContext {
  tenantId?: number;
  facilityId?: number;
  patientId?: number;
  vaccinationId?: number;
  url?: string;
}

/**
 * Builds the router slice the way @ngrx/router-store would, so the real
 * router selectors (selectRouteTenantId and friends) resolve against it.
 */
export function routerStateFactory(context: RouteContext = {}): RouterReducerState {
  const params: Record<string, string> = {};
  if (context.tenantId !== undefined) params['tenantId'] = String(context.tenantId);
  if (context.facilityId !== undefined) params['facilityId'] = String(context.facilityId);
  if (context.patientId !== undefined) params['patientId'] = String(context.patientId);
  if (context.vaccinationId !== undefined) params['vaccinationId'] = String(context.vaccinationId);

  const derivedUrl =
    [
      context.tenantId !== undefined ? `/t/${context.tenantId}` : '',
      context.facilityId !== undefined ? `/f/${context.facilityId}` : '',
      context.patientId !== undefined ? `/patients/${context.patientId}` : '',
    ].join('') || '/';
  const url = context.url ?? derivedUrl;

  return {
    state: {
      url,
      params,
      queryParams: {},
      data: {},
      // `root` mirrors the shape the default serializer produces.
      root: {params, queryParams: {}, data: {}, url: [], outlet: 'primary', children: []},
    },
    navigationId: 1,
  } as unknown as RouterReducerState;
}

export interface AppStateOverrides extends Partial<Omit<AppState, 'router'>> {
  router?: RouterReducerState;
  /** Shorthand for building the router slice from route params. */
  route?: RouteContext;
}

export function appStateFactory(overrides: AppStateOverrides = {}): AppState {
  const {route, ...rest} = overrides;
  return {
    router: overrides.router ?? routerStateFactory(route),
    [fromAuth.authFeatureKey]: fromAuth.initialState,
    [fromTenant.tenantFeatureKey]: fromTenant.initialState,
    [fromFacility.facilityFeatureKey]: fromFacility.initialState,
    [fromPatient.patientFeatureKey]: fromPatient.initialState,
    [fromVaccination.vaccinationFeatureKey]: fromVaccination.initialState,
    [fromRegistry.registryFeatureKey]: fromRegistry.initialState,
    [fromCodeMaps.codeMapsFeatureKey]: fromCodeMaps.initialState,
    [fromClinician.clinicianFeatureKey]: fromClinician.initialState,
    ...rest,
  };
}

/** Provider for TestBed, pre-populated with a full AppState. */
export function provideTestStore(overrides: AppStateOverrides = {}): Provider[] {
  return [provideMockStore({initialState: appStateFactory(overrides)})];
}

/**
 * `overrideSelector` mutates the memoized selector itself, and selectors are
 * module-level singletons, so without this an override in one spec leaks into
 * every later spec in the run.
 *
 * Registered once at module load — `provideTestStore` is often called from
 * inside an `it`, where calling `afterEach` would throw.
 */
if (typeof afterEach === 'function') {
  afterEach(() => {
    TestBed.inject(MockStore, null, {optional: true})?.resetSelectors();
  });
}

/**
 * Overrides a selector and pushes the new value to subscribers in one step.
 * `store.overrideSelector` alone does not emit until refreshState() is called.
 */
export function setSelector<T>(
  store: MockStore,
  selector: MemoizedSelector<object, T>,
  value: T,
): void {
  store.overrideSelector(selector, value);
  store.refreshState();
}

/** Collects dispatched actions; assert on them after exercising a component. */
export function recordDispatches(store: MockStore): { actions: Action[] } {
  const recorder = {actions: [] as Action[]};
  // Store.dispatch is overloaded (action | effect fn), which jasmine's
  // callFake cannot match; spy on the loosened signature instead.
  const spy = spyOn(store, 'dispatch') as unknown as jasmine.Spy<(action: Action) => void>;
  spy.and.callFake((action: Action) => {
    recorder.actions.push(action);
  });
  return recorder;
}
