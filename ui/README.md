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
- **Authentication is token-based.** `POST /auth` returns a JWT, which
  `TokenStorageService` keeps in `localStorage` so a login survives closing
  the browser; `authInterceptor` sends it as `Authorization: Bearer …` on
  every request. No cookies are involved, so requests are not sent
  `withCredentials`. Storing the token in `localStorage` makes it readable by
  any script on this origin — a deliberate tradeoff for persistence. Sign-out
  removes only the auth keys, since the colour scheme shares that storage.
- **Session restore across reloads.** The stored token *is* the session:
  `restoreSession` skips the HTTP call entirely when there is no token, and
  clears a token the API rejects. On a refresh the store starts empty while
  the validating call is in flight, so `authGuard` waits for
  `selectSessionStatus().resolved` instead of reading `authenticated`
  immediately — otherwise a signed-in user is bounced to the login page. The
  guard starts a probe only when the session is **unknown**; re-probing an
  established one would undo a fresh login. `exhaustMap` in the effect drops
  the duplicate the app shell sends, so only one call runs.
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

## Forms

`shared/forms` is a generic, config-driven form engine reused by every entity
form (patient today; vaccination, facility and clinician next).

A form is declared as data — `FormCard[]`, each card holding `FormField`s that
name an attribute, its `FormType`, validators and its `hl7Location`. See
`features/patient/patient-form/patient-form.config.ts`.

| Piece                      | Role                                                                                       |
|----------------------------|--------------------------------------------------------------------------------------------|
| `form-config.ts`           | `FormType`, `FormField`, `FormCard`, HL7 location helpers                                  |
| `form-engine.service.ts`   | Builds a typed `FormGroup` from config; `reset()`, `toEntity()`, and required-rule lifting |
| `form-field.component.ts`  | Renders one field — the `FormType` switch                                                  |
| `form-list.component.ts`   | A repeating field, one nested card per entry                                               |
| `form-card.component.ts`   | One section                                                                                |
| `entity-form.component.ts` | Shell: cards, JSON tab, lift toggle, random fill                                           |

Carried over from the previous UI:

- **Required fields and custom validators**, declared per field.
- **Lifting required rules** — the old "Form with Requirements lifted" tab is
  now a toggle in the form header, so incomplete data can still be submitted
  to test how the registry responds. Validators are re-applied in place, so
  values already entered survive the switch and custom validators are kept.
- **Registry feedback as inline warnings** — `form-warnings.ts` matches
  `Feedback.hl7Locations` to the `hl7Location` of each field, including the
  right entry of a repeating field (HL7 repetitions are 1-based).
  `unmatchedWarnings()` returns feedback that matched nothing, so it is never
  silently dropped.
- **Comparison hints** — "Previously: x" against another record.
- **JSON tab** and **Fill random**.

### Layout

Within a card, runs of scalar fields share a responsive grid while each
repeating field claims a **full row of its own**; its entries then run
horizontally in a scrolling track, so a patient with many identifiers does
not stretch the form downwards. Cards that contain a repeating field span the
full width of the form (`card--wide`) to give that track room.

### Code sources

A `FormType.code` field reads its options either from the codemaps
(`codeMapLabel`) or from a bundled FHIR CodeSystem (`codeSystem`). Identifier
types (HL7 table 0203) and clinician qualifications (table 0360) are
published as code systems, not codemap entries — binding them to a
`codeMapLabel` yields an empty dropdown.

### Tenant flavors

The backend encodes flavors in the tenant name, so a tenant called
"SINGLENAME Hospital" has the SINGLENAME flavor active. Fields declare their
own adjustments, so adding a flavor is a config change:

```ts
flavors: {SINGLENAME: {singleEntry: true, omitItemFields: ['nameType']}}
```

`form-flavors.ts` resolves the active keys from the tenant name and returns an
adapted config; an override can collapse a list to a single entry
(`singleEntry`), drop sub-fields (`omitItemFields`), hide a field (`omit`) or
change whether it is required. A single-entry list still holds an array, so
the API contract does not change.

Two details worth knowing when extending this:

- A `FormControl` is not a signal, so `computed()` over `touched` or validator
  state never recomputes. `form-field.component.ts` subscribes to
  `control.events` and bumps a counter signal the computeds depend on.
- Fields whose value is nested in the model (next of kin) declare a dotted
  `path`; the engine reads and writes through it.
- `store.selectSignal` must be called in an injection context, never inside a
  `computed` — each recomputation would otherwise open a new subscription.
- A `computed` returning a mutated-in-place array (such as
  `FormArray.controls`) needs `{equal: () => false}`, or Angular's identity
  check sees no change and the view never re-renders.
- Repopulating a form pushes entries with `emitEvent: false` and then emits
  once, so views bound to a list update exactly one time.

Form validation is covered at three levels: `form-engine.service.spec.ts`
(validators built and lifted correctly), `form-field.component.spec.ts`
(errors, required markers and hints actually *render* — control state alone
can be right while the DOM is stale) and `patient-form.component.spec.ts`
(submission is blocked, feedback is attached, "Fill random" replaces every
section including repeating ones).

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

## Layout

`shared/layout` holds the app shell: `MainLayoutComponent` (sidebar + topbar +
outlet), a collapsible `SidebarComponent`, and a `TopbarComponent` carrying a
context breadcrumb, refresh, theme toggle and logout.

There are **no tenant/facility dropdowns**. Context is selected by navigating
to the tenant and facility list pages, and is read back from the url — so it
survives reloads, deep links and the browser's back button, and two tabs can
sit in different tenants. The topbar breadcrumb (Tenants › tenant › facility)
shows the current context and links back to each selection page.

Context-free pages (Home, Tenants) are mounted three times in `app.routes.ts`
— at the root, under `/t/:tenantId`, and under `/t/:tenantId/f/:facilityId`.
The sidebar links them with the deepest prefix currently selected, so moving
to Home from a dashboard gives `/t/1/f/5/home` and returning to the dashboard
is still one click. Items marked `tenantScopable` in `sidebar/nav-items.ts`
opt into this.

Sidebar sections are scoped (`global` / `tenant` / `facility`) and appear as
the url provides the ids they need; not-yet-built destinations are marked
`pending` and render a "soon" badge.

Selecting a patient on the dashboard likewise navigates to
`…/dashboard/patients/:patientId`, so a dashboard view is shareable as a link.

### Row actions

`shared/components/record-tools` is the translation of the old
`patient-tools` / `vaccination-tools` components. The tool sets are declared
as data (`PATIENT_TOOLS`, `VACCINATION_TOOLS`) and rendered in a table's
expanded row; the component emits a `RecordToolAction` and the host decides
what to do. Actions whose dialog has not been migrated are flagged `pending`
and raise an explicit "not available yet" toast rather than doing nothing.

Both tables flatten their data into view rows (`PatientRow`,
`VaccinationRow`) before handing it to `p-table`. Every column is therefore
sortable on exactly the value it displays — `names` is an array whose
preferred ('L') entry is not necessarily first, so sorting the nested model
would disagree with the rendering.

Date-only values from the API (`YYYY-MM-DD`) go through
`shared/util/date.ts#parseApiDate`: `new Date('1985-12-09')` is UTC midnight,
which renders as the previous day anywhere west of UTC.

Working today: create/populate toolbars, search, sorting on all columns, row
expansion, and patient selection. Populate calls the real endpoints
(`/facilities/{id}/$populate`, `/patients/{id}/$populate`) and is offered only
while the target is empty, matching the old UI.

## Status

Models, services, stores, routing, theming and auth are in place, along with
the layout shell, login page, home page and facility dashboard (patients +
immunizations live with their full action toolbars; data-quality and
recommendation panels are framed placeholders). Tenant, facility and patient
list pages are still placeholders, and the dialogs behind the row tools
(forms, HL7v2/FHIR messaging, local copy, remote history, JSON viewer) are
not built yet.
