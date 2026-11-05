/**
 * Effect testing harness.
 *
 * Effects here are plain action-in / action-out pipes, so most specs need
 * nothing more than: feed an action, assert the emitted one.
 *
 *   const effects = createEffects(TenantEffects, {
 *     actions: of(TenantActions.loadTenants()),
 *     providers: [{ provide: TenantService, useValue: tenantServiceSpy }],
 *     state: { route: { tenantId: 1 } },
 *   });
 *   await expectEmissions(effects.loadTenants$, [TenantActions.loadTenantsSuccess({ tenants })]);
 */
import {Provider, Type} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {provideMockActions} from '@ngrx/effects/testing';
import {Action} from '@ngrx/store';
import {MockStore} from '@ngrx/store/testing';
import {firstValueFrom, Observable, of, toArray} from 'rxjs';
import {AppStateOverrides, provideTestStore} from './store.harness';

export interface EffectsHarnessConfig {
  /** Actions stream fed to the effect under test. */
  actions?: Observable<Action>;
  providers?: Provider[];
  state?: AppStateOverrides;
}

export interface EffectsHarness<T> {
  effects: T;
  store: MockStore;
}

export function createEffectsHarness<T>(
  effectsType: Type<T>,
  config: EffectsHarnessConfig = {},
): EffectsHarness<T> {
  TestBed.configureTestingModule({
    providers: [
      effectsType,
      provideMockActions(() => config.actions ?? of()),
      ...provideTestStore(config.state ?? {}),
      ...(config.providers ?? []),
    ],
  });

  return {
    effects: TestBed.inject(effectsType),
    store: TestBed.inject(MockStore),
  };
}

/** Asserts the full sequence an effect emits for a finite actions stream. */
export async function expectEmissions<T>(effect$: Observable<T>, expected: T[]): Promise<void> {
  const emitted = await firstValueFrom(effect$.pipe(toArray()));
  expect(emitted).toEqual(expected);
}

/** Asserts a single emitted action. */
export async function expectEmission<T>(effect$: Observable<T>, expected: T): Promise<void> {
  await expectEmissions(effect$, [expected]);
}

/** Asserts an effect emits nothing (e.g. filtered out by missing context). */
export async function expectNoEmission(effect$: Observable<unknown>): Promise<void> {
  await expectEmissions(effect$, []);
}
