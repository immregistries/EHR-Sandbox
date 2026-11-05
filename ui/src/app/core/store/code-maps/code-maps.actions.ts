import {createActionGroup, emptyProps, props} from '@ngrx/store';
import {CodeSystem} from 'fhir/r5';
import {CodeBaseMap} from '../../models/code-base-map';

export const CodeMapsActions = createActionGroup({
  source: 'Code Maps',
  events: {
    /** Loads the codemaps and both bundled FHIR code systems. */
    Load: emptyProps(),
    'Load Success': props<{
      codeBaseMap: CodeBaseMap;
      identifierTypeCodeSystem: CodeSystem;
      qualificationCodeSystem: CodeSystem;
    }>(),
    'Load Failure': props<{ error: string }>(),

    /** Re-reads only the codemaps, leaving the static code systems alone. */
    Refresh: emptyProps(),
    'Refresh Success': props<{ codeBaseMap: CodeBaseMap }>(),
  },
});
