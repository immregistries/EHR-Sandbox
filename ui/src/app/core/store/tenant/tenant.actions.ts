import {createActionGroup, emptyProps, props} from '@ngrx/store';
import {Flavor, Tenant} from '../../models';

export const TenantActions = createActionGroup({
  source: 'Tenant',
  events: {
    'Load Tenants': emptyProps(),
    'Load Tenants Success': props<{ tenants: Tenant[] }>(),
    'Load Tenants Failure': props<{ error: string }>(),

    'Load Tenant': props<{ tenantId: number }>(),
    'Load Tenant Success': props<{ tenant: Tenant }>(),
    'Load Tenant Failure': props<{ error: string }>(),

    'Create Tenant': props<{ tenant: Tenant }>(),
    'Create Tenant Success': props<{ tenant: Tenant }>(),
    'Create Tenant Failure': props<{ error: string }>(),

    'Load Flavors': emptyProps(),
    'Load Flavors Success': props<{ flavors: Flavor[] }>(),

    /** Replaces the old RefreshService broadcast. */
    Refresh: emptyProps(),
  },
});
