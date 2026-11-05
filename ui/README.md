# EHR Sandbox UI (modernized)

Angular 19 + PrimeNG rewrite of `EHR-Sandbox-Ui`. The old app is untouched.

## Requirements

Node `v24.16.0` (see `.nvmrc`): `nvm use`.

## Commands

| Command           | Description                                              |
|-------------------|----------------------------------------------------------|
| `npm start`       | Dev server on :4200, proxying `/ehr` to `localhost:8080` |
| `npm run build`   | Production build                                         |
| `npm test`        | Unit tests, watch mode                                   |
| `npm run test:ci` | Single headless run with coverage + junit                |

## Architecture

### State — NgRx

`RefreshService` / `CurrentSelectedService` are gone. In their place:

- **The URL is the source of truth for context.** `/t/:tenantId/f/:facilityId/...`
  Router params are read through `core/store/router.selectors.ts`, so
  `selectCurrentTenant` and `selectCurrentFacility` derive from the route — no
  separate "current selection" subject to keep in sync.
  `paramsInheritanceStrategy: 'always'` makes those params visible at any depth.
- **`Refresh` actions** replace the `doRefresh()` broadcast. Each slice's
  `refresh$` effect re-reads using the ids currently in the URL.
- **`ContextEffects`** replaces the cross-service subscriptions (e.g. changing
  facility used to reset the selected patient from inside `PatientService`).
- **`CallState`** (`core/store/call-state.ts`) gives every slice uniform
  loading/error handling.
- **Code maps** load once per session (on login or session restore) into the
  `codeMaps` slice. Views read them through factory selectors —
  `selectCodeSet(label)`, `selectCode(label, value)`,
  `selectCodeSetOptions(label)` — which already hide deprecated codes unless
  the tenant flavor asks for them.

### Services

API services are **stateless**: tenant/facility ids are always explicit
arguments, never read from another service. This is what made the old
`quickRead*` variants necessary; they are no longer needed.

### Theming

- PrimeNG palette: `src/app/styles/theme-preset.ts` (Aura preset).
- Project tokens: `src/styles/_theme.scss`, as CSS custom properties defined
  for `:root` and `:root.app-dark`.
- `ThemeService` toggles `.app-dark` on `<html>` and supports a `system` mode.
- Scheme-independent tokens (spacing, radius, type) live in `_variables.scss`.

## Testing

Karma + Jasmine. Specs run in **random order** and a spec with no
expectations **fails** — both are deliberate, to catch order-dependent and
vacuous tests before CI does.

Harnesses live in `src/testing` (import from `src/testing`):

| Helper                 | Use                                                                                                                                                                     |
|------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `fixtures.ts`          | `aPatient()`, `aFacility()`, `aTenant()`, … — complete objects taking a partial override. `resetFixtureIds()` when asserting on generated ids.                          |
| `store.harness.ts`     | `appStateFactory()` builds a full typed `AppState` (override one slice, cross-slice selectors still work); `provideTestStore()`, `setSelector()`, `recordDispatches()`. |
| `effects.harness.ts`   | `createEffectsHarness(Effects, { actions, providers, state })` plus `expectEmission` / `expectEmissions` / `expectNoEmission`.                                          |
| `http.harness.ts`      | `createHttpHarness(Service)` with `expectOne(method, url)`; the API root is stubbed to `/ehr` so specs assert full urls.                                                |
| `component.harness.ts` | `renderComponent(Cmp, { state, route, inputs })` with MockStore, PrimeNG, noop animations, router stub, and `query` / `text` / `click`.                                 |

Exemplars to pattern new tests on:

- reducer — `core/store/patient/patient.reducer.spec.ts`
- selector — `core/store/code-maps/code-maps.selectors.spec.ts`
- effect — `core/store/facility/facility.effects.spec.ts`
- API service — `core/services/patient.service.spec.ts`
- component — `features/auth/login/login.component.spec.ts`

Two gotchas the harness already handles, worth knowing:

- `overrideSelector` mutates a module-level singleton, so an override leaks
  across specs. `store.harness.ts` registers a global `resetSelectors()`
  cleanup; use `setSelector()` and set a selector explicitly when a spec
  depends on its *absence*.
- Route context in specs comes from `state: { route: { tenantId, facilityId } }`,
  which builds the router slice the real router selectors read.

Coverage thresholds in `karma.conf.js` are a **ratchet** — currently at the
floor the exemplars reach (45/30/25/45). Raise them as suites land; target is
80/70/80/80.

## Status

Models, services, stores, routing, theming and auth are in place. Feature
components (dashboard, tenant/facility/patient lists) are placeholders.
