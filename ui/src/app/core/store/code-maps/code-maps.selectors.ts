import {createFeatureSelector, createSelector} from '@ngrx/store';
import {Code, CodeSet} from '../../models/code-base-map';
import {getError, isLoading} from '../call-state';
import {selectCurrentTenant} from '../tenant/tenant.selectors';
import {codeMapsFeatureKey, CodeMapsState} from './code-maps.reducer';

export const selectCodeMapsState = createFeatureSelector<CodeMapsState>(codeMapsFeatureKey);

export const selectCodeBaseMap = createSelector(selectCodeMapsState, (s) => s.codeBaseMap);
export const selectIdentifierTypeCodeSystem = createSelector(
  selectCodeMapsState,
  (s) => s.identifierTypeCodeSystem,
);
export const selectQualificationCodeSystem = createSelector(
  selectCodeMapsState,
  (s) => s.qualificationCodeSystem,
);
export const selectCodeMapsLoading = createSelector(selectCodeMapsState, (s) =>
  isLoading(s.callState),
);
export const selectCodeMapsError = createSelector(selectCodeMapsState, (s) =>
  getError(s.callState),
);
export const selectCodeMapsLoaded = createSelector(
  selectCodeMapsState,
  (s) => s.callState === 'loaded',
);

/**
 * Deprecated codes stay visible only for the PAIN_PERDU flavor.
 * TODO: read flavors from the API instead of matching on the tenant name,
 * to stay consistent with the backend.
 */
export const selectIncludeDeprecatedCodes = createSelector(selectCurrentTenant, (tenant) =>
  Boolean(tenant?.nameDisplay?.toUpperCase().includes('PAIN_PERDU')),
);

const withoutDeprecated = (codeSet: CodeSet): CodeSet =>
  Object.fromEntries(
    Object.entries<Code>(codeSet).filter(
      ([, code]) => code.codeStatus?.status?.toLowerCase() !== 'deprecated',
    ),
  );

/** The codemaps as the current tenant should see them. */
export const selectVisibleCodeBaseMap = createSelector(
  selectCodeBaseMap,
  selectIncludeDeprecatedCodes,
  (codeBaseMap, includeDeprecated) =>
    includeDeprecated
      ? codeBaseMap
      : Object.fromEntries(
        Object.entries(codeBaseMap).map(([label, codeSet]) => [label, withoutDeprecated(codeSet)]),
      ),
);

/** Factory selector for a single code set, e.g. 'VACCINATION_CVX_CODE'. */
export const selectCodeSet = (label: string | undefined) =>
  createSelector(selectVisibleCodeBaseMap, (codeBaseMap) =>
    label ? codeBaseMap[label] : undefined,
  );

/** Factory selector resolving one code within a set. */
export const selectCode = (label: string | undefined, value: string | undefined) =>
  createSelector(selectCodeSet(label), (codeSet) =>
    value ? codeSet?.[value] : undefined,
  );

/** A code set as an array, ready for p-select / p-autocomplete options. */
export const selectCodeSetOptions = (label: string | undefined) =>
  createSelector(selectCodeSet(label), (codeSet) => Object.values(codeSet ?? {}));
