import {createActionGroup, emptyProps, props} from '@ngrx/store';
import {VaccinationEvent} from '../../models';

export const VaccinationActions = createActionGroup({
  source: 'Vaccination',
  events: {
    'Load Vaccinations': props<{ tenantId: number; facilityId: number; patientId: number }>(),
    'Load Facility Vaccinations': props<{ tenantId: number; facilityId: number }>(),
    'Load Vaccinations Success': props<{ vaccinations: VaccinationEvent[] }>(),
    'Load Vaccinations Failure': props<{ error: string }>(),

    'Load Vaccination': props<{
      tenantId: number;
      facilityId: number;
      patientId: number;
      vaccinationId: number;
    }>(),
    'Load Vaccination Success': props<{ vaccination: VaccinationEvent }>(),

    'Create Vaccination': props<{
      tenantId: number;
      facilityId: number;
      patientId: number;
      vaccination: VaccinationEvent;
    }>(),
    'Create Vaccination Success': props<{ vaccinationId: number }>(),
    'Update Vaccination': props<{
      tenantId: number;
      facilityId: number;
      patientId: number;
      vaccination: VaccinationEvent;
    }>(),
    'Update Vaccination Success': props<{ vaccination: VaccinationEvent }>(),
    'Save Vaccination Failure': props<{ error: string }>(),

    Refresh: emptyProps(),
    Clear: emptyProps(),
  },
});
