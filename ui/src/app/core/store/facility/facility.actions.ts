import {createActionGroup, emptyProps, props} from '@ngrx/store';
import {Facility} from '../../models';

export const FacilityActions = createActionGroup({
  source: 'Facility',
  events: {
    'Load Facilities': props<{ tenantId: number }>(),
    'Load Facilities Success': props<{ facilities: Facility[] }>(),
    'Load Facilities Failure': props<{ error: string }>(),

    'Load Facility': props<{ tenantId: number; facilityId: number }>(),
    'Load Facility Success': props<{ facility: Facility }>(),
    'Load Facility Failure': props<{ error: string }>(),

    'Create Facility': props<{ tenantId: number; facility: Facility; populate?: boolean }>(),
    'Create Facility Success': props<{ facility: Facility }>(),
    'Update Facility': props<{ tenantId: number; facility: Facility }>(),
    'Update Facility Success': props<{ facility: Facility }>(),
    'Save Facility Failure': props<{ error: string }>(),

    Refresh: emptyProps(),
    Clear: emptyProps(),
  },
});
