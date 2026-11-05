import {AuthEffects} from './auth/auth.effects';
import {CodeMapsEffects} from './code-maps/code-maps.effects';
import {ContextEffects} from './context.effects';
import {FacilityEffects} from './facility/facility.effects';
import {PatientEffects} from './patient/patient.effects';
import {RegistryEffects} from './registry/registry.effects';
import {TenantEffects} from './tenant/tenant.effects';
import {VaccinationEffects} from './vaccination/vaccination.effects';

export const appEffects = [
  AuthEffects,
  ContextEffects,
  TenantEffects,
  FacilityEffects,
  PatientEffects,
  VaccinationEffects,
  RegistryEffects,
  CodeMapsEffects,
];
