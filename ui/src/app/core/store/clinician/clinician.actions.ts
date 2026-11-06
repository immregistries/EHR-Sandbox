import {createActionGroup, emptyProps, props} from '@ngrx/store';
import {Clinician} from '../../models';

export const ClinicianActions = createActionGroup({
  source: 'Clinician',
  events: {
    'Load Clinicians': props<{ tenantId: number }>(),
    'Load Clinicians Success': props<{ clinicians: Clinician[] }>(),
    'Load Clinicians Failure': props<{ error: string }>(),

    'Save Clinician': props<{ tenantId: number; clinician: Clinician }>(),
    'Save Clinician Success': props<{ clinician: Clinician }>(),
    'Save Clinician Failure': props<{ error: string }>(),

    Refresh: emptyProps(),
    Clear: emptyProps(),
  },
});
