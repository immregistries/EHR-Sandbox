import {createActionGroup, emptyProps, props} from '@ngrx/store';
import {ImmunizationRegistry} from '../../models';

export const RegistryActions = createActionGroup({
  source: 'Immunization Registry',
  events: {
    'Load Registries': emptyProps(),
    'Load Registries Success': props<{ registries: ImmunizationRegistry[] }>(),
    'Load Registries Failure': props<{ error: string }>(),

    /** The active registry is a user preference, not a url concern. */
    'Select Registry': props<{ registryId: number | null }>(),

    'Save Registry': props<{ registry: ImmunizationRegistry }>(),
    'Save Registry Success': props<{ registry: ImmunizationRegistry }>(),
    'Delete Registry': props<{ registryId: number }>(),
    'Delete Registry Success': props<{ registryId: number }>(),
    'Save Registry Failure': props<{ error: string }>(),
  },
});
