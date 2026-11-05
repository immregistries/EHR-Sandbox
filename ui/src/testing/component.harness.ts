/**
 * Component testing harness.
 *
 *   const h = await renderComponent(PatientListComponent, {
 *     state: { route: { tenantId: 1, facilityId: 2 } },
 *   });
 *   expect(h.text('h2')).toBe('Patients');
 *
 * Wires a MockStore with a full AppState, noop animations, a stubbed router
 * and the PrimeNG services components inject.
 *
 * Call it at most ONCE per spec: TestBed cannot be reconfigured after it is
 * instantiated. To compare two states, write two specs (or re-render via the
 * component's inputs with `fixture.componentRef.setInput`).
 */
import {provideHttpClient} from '@angular/common/http';
import {provideHttpClientTesting} from '@angular/common/http/testing';
import {Provider, Type} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {provideNoopAnimations} from '@angular/platform-browser/animations';
import {ActivatedRoute, provideRouter} from '@angular/router';
import {MockStore} from '@ngrx/store/testing';
import {MessageService} from 'primeng/api';
import {providePrimeNG} from 'primeng/config';
import {DialogService} from 'primeng/dynamicdialog';
import {BehaviorSubject} from 'rxjs';
import {EhrPreset} from '../app/styles/theme-preset';
import {AppStateOverrides, provideTestStore, RouteContext} from './store.harness';

export interface RenderConfig {
  state?: AppStateOverrides;
  providers?: Provider[];
  /** Route params exposed through ActivatedRoute (for withComponentInputBinding). */
  route?: RouteContext;
  /** Set component inputs before the first change detection. */
  inputs?: Record<string, unknown>;
}

export interface ComponentHarness<T> {
  fixture: ComponentFixture<T>;
  component: T;
  store: MockStore;

  detectChanges(): void;

  /** First element matching a CSS selector, or null. */
  query<E = HTMLElement>(selector: string): E | null;

  queryAll<E = HTMLElement>(selector: string): E[];

  /** Trimmed text content of the first match. */
  text(selector: string): string | null;

  click(selector: string): void;
}

function activatedRouteStub(route: RouteContext = {}) {
  const params: Record<string, string> = {};
  for (const [key, value] of Object.entries(route)) {
    if (key !== 'url' && value !== undefined) {
      params[key] = String(value);
    }
  }
  const paramsSubject = new BehaviorSubject(params);
  return {
    snapshot: {
      params,
      paramMap: new Map(Object.entries(params)),
      queryParams: {},
      data: {},
    },
    params: paramsSubject.asObservable(),
    queryParams: new BehaviorSubject({}).asObservable(),
    data: new BehaviorSubject({}).asObservable(),
  };
}

export async function renderComponent<T>(
  componentType: Type<T>,
  config: RenderConfig = {},
): Promise<ComponentHarness<T>> {
  await TestBed.configureTestingModule({
    imports: [componentType],
    providers: [
      provideNoopAnimations(),
      provideHttpClient(),
      provideHttpClientTesting(),
      provideRouter([]),
      ...provideTestStore(config.state ?? {}),
      providePrimeNG({theme: {preset: EhrPreset, options: {darkModeSelector: '.app-dark'}}}),
      MessageService,
      DialogService,
      {provide: ActivatedRoute, useValue: activatedRouteStub(config.route)},
      ...(config.providers ?? []),
    ],
  }).compileComponents();

  const fixture = TestBed.createComponent(componentType);

  for (const [key, value] of Object.entries(config.inputs ?? {})) {
    fixture.componentRef.setInput(key, value);
  }

  fixture.detectChanges();

  const queryAll = <E, >(selector: string): E[] =>
    fixture.debugElement.queryAll(By.css(selector)).map((de) => de.nativeElement as E);

  return {
    fixture,
    component: fixture.componentInstance,
    store: TestBed.inject(MockStore),
    detectChanges: () => fixture.detectChanges(),
    query: <E, >(selector: string): E | null => queryAll<E>(selector)[0] ?? null,
    queryAll,
    text: (selector: string): string | null => {
      const el = queryAll<HTMLElement>(selector)[0];
      return el ? (el.textContent ?? '').trim() : null;
    },
    click: (selector: string): void => {
      const el = queryAll<HTMLElement>(selector)[0];
      if (!el) {
        throw new Error(`click(): no element matches "${selector}"`);
      }
      el.click();
      fixture.detectChanges();
    },
  };
}
