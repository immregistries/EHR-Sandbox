import {createActionGroup, emptyProps, props} from '@ngrx/store';
import {EhrPatient, Revision} from '../../models';

export const PatientActions = createActionGroup({
  source: 'Patient',
  events: {
    'Load Patients': props<{ tenantId: number; facilityId: number }>(),
    'Load Patients Success': props<{ patients: EhrPatient[] }>(),
    'Load Patients Failure': props<{ error: string }>(),

    'Load Patient': props<{ tenantId: number; facilityId: number; patientId: number }>(),
    'Load Patient Success': props<{ patient: EhrPatient }>(),
    'Load Patient Failure': props<{ error: string }>(),

    'Create Patient': props<{ tenantId: number; facilityId: number; patient: EhrPatient }>(),
    'Create Patient Success': props<{ patientId: number }>(),
    'Update Patient': props<{ tenantId: number; facilityId: number; patient: EhrPatient }>(),
    'Update Patient Success': props<{ patient: EhrPatient }>(),
    'Save Patient Failure': props<{ error: string }>(),

    /** Seeds an empty facility with random patients. */
    'Populate Facility': props<{ tenantId: number; facilityId: number }>(),
    'Populate Facility Success': emptyProps(),
    'Populate Facility Failure': props<{ error: string }>(),

    /** Seeds a patient with random vaccinations. */
    'Populate Patient': props<{ tenantId: number; facilityId: number; patientId: number }>(),
    'Populate Patient Success': emptyProps(),

    'Load History': props<{ tenantId: number; facilityId: number; patientId: number }>(),
    'Load History Success': props<{ history: Revision<EhrPatient>[] }>(),

    Refresh: emptyProps(),
    Clear: emptyProps(),
  },
});
