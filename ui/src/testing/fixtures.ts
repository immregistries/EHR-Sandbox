/**
 * Typed fixture builders. Every builder returns a complete, valid object and
 * accepts a partial override, so a test states only what it cares about:
 *
 *   aPatient({ sex: 'M' })
 *   aFacility({ id: 7, nameDisplay: 'North Clinic' })
 *
 * Ids are deterministic by default — pass one explicitly when a test depends
 * on a specific value, and use `resetFixtureIds()` in a beforeEach if a spec
 * asserts on generated ids.
 */
import {
  AcknowledgementObject,
  Clinician,
  EhrAddress,
  EhrGroup,
  EhrHumanName,
  EhrIdentifier,
  EhrPatient,
  EhrPhoneNumber,
  Facility,
  Feedback,
  ImmunizationRegistry,
  Revision,
  Tenant,
  User,
  VaccinationEvent,
  Vaccine,
} from '../app/core/models';
import {Code, CodeBaseMap} from '../app/core/models/code-base-map';

let nextId = 1;

/** Call in a beforeEach when a spec asserts on generated ids. */
export function resetFixtureIds(): void {
  nextId = 1;
}

function id(explicit?: number): number {
  return explicit ?? nextId++;
}

export function anIdentifier(overrides: Partial<EhrIdentifier> = {}): EhrIdentifier {
  return {system: 'urn:oid:1.2.3.4', value: 'MRN-001', type: 'MR', ...overrides};
}

export function anAddress(overrides: Partial<EhrAddress> = {}): EhrAddress {
  return {
    addressLine1: '1 Test Street',
    addressCity: 'Testville',
    addressState: 'MN',
    addressZip: '55401',
    addressCountry: 'USA',
    ...overrides,
  };
}

export function aPhoneNumber(overrides: Partial<EhrPhoneNumber> = {}): EhrPhoneNumber {
  return {number: '555-0100', type: 'PH', use: 'HP', ...overrides};
}

export function aHumanName(overrides: Partial<EhrHumanName> = {}): EhrHumanName {
  return {nameFirst: 'Ada', nameLast: 'Lovelace', nameType: 'L', ...overrides};
}

export function aTenant(overrides: Partial<Tenant> = {}): Tenant {
  const tenantId = id(overrides.id);
  return {id: tenantId, nameDisplay: `Tenant ${tenantId}`, ...overrides,};
}

export function aFacility(overrides: Partial<Facility> = {}): Facility {
  const facilityId = id(overrides.id);
  return {
    id: facilityId,
    nameDisplay: `Facility ${facilityId}`,
    identifiers: [anIdentifier()],
    addresses: [anAddress()],
    childrenCount: 0,
    feedbacksCount: 0,
    ...overrides,
  };
}

export function aPatient(overrides: Partial<EhrPatient> = {}): EhrPatient {
  const patientId = id(overrides.id);
  return {
    id: patientId,
    names: [aHumanName()],
    birthDate: new Date('1990-01-01'),
    sex: 'F',
    identifiers: [anIdentifier({value: `MRN-${patientId}`})],
    addresses: [anAddress()],
    phones: [aPhoneNumber()],
    feedbacksCount: 0,
    ...overrides,
  };
}

export function aVaccine(overrides: Partial<Vaccine> = {}): Vaccine {
  return {
    id: id(overrides.id),
    administeredDate: new Date('2024-06-01'),
    vaccineCvxCode: '08',
    lotNumber: 'LOT-123',
    completionStatus: 'CP',
    actionCode: 'A',
    ...overrides,
  };
}

export function aVaccinationEvent(overrides: Partial<VaccinationEvent> = {}): VaccinationEvent {
  return {
    id: id(overrides.id),
    patient: 1,
    vaccine: aVaccine(),
    primarySource: true,
    feedbacksCount: 0,
    ...overrides,
  };
}

export function aClinician(overrides: Partial<Clinician> = {}): Clinician {
  const clinicianId = id(overrides.id);
  return {
    id: clinicianId,
    nameFirst: 'Grace',
    nameLast: 'Hopper',
    identifiers: [anIdentifier({type: 'NPI'})],
    ...overrides,
  };
}

export function aGroup(overrides: Partial<EhrGroup> = {}): EhrGroup {
  const groupId = id(overrides.id);
  return {
    id: groupId,
    name: `Group ${groupId}`,
    type: 'person',
    patientList: [],
    ...overrides,
  };
}

export function aFeedback(overrides: Partial<Feedback> = {}): Feedback {
  return {
    id: id(overrides.id),
    content: 'Something needs attention',
    severity: 'WARN',
    code: 'W001',
    timestamp: Date.parse('2024-06-01T00:00:00Z'),
    ...overrides,
  };
}

export function anAcknowledgement(
  overrides: Partial<AcknowledgementObject<Feedback>> = {},
): AcknowledgementObject<Feedback> {
  return {
    id: id(overrides.id),
    messageId: 'MSG-1',
    sortedResult: {errors: [], warnings: [], notices: [], infos: []},
    ...overrides,
  };
}

export function aRegistry(overrides: Partial<ImmunizationRegistry> = {}): ImmunizationRegistry {
  const registryId = id(overrides.id);
  return {
    id: registryId,
    name: `Registry ${registryId}`,
    iisHl7Url: 'http://localhost:8080/iis/soap',
    iisFhirUrl: 'http://localhost:8080/iis/fhir',
    iisUsername: 'user',
    default: false,
    ...overrides,
  };
}

export function aUser(overrides: Partial<User> = {}): User {
  return {id: id(overrides.id), username: 'tester', ...overrides};
}

export function aRevision<T>(entity: T, overrides: Partial<Revision<T>> = {}): Revision<T> {
  const instant = new Date('2024-06-01');
  return {
    entity,
    metadata: {
      delegate: {id: 1, immunizationRegistryId: '', subscriptionId: '', user: 'tester'},
      revisionType: 'MOD',
      requiredRevisionInstant: instant,
      requiredRevisionNumber: 1,
    },
    requiredRevisionInstant: instant,
    requiredRevisionNumber: 1,
    ...overrides,
  };
}

export function aCode(overrides: Partial<Code> = {}): Code {
  return {value: '08', label: 'Hep B, adolescent or pediatric', ...overrides};
}

/** A small codemap, including one deprecated entry for filter tests. */
export function aCodeBaseMap(overrides: CodeBaseMap = {}): CodeBaseMap {
  return {
    VACCINATION_CVX_CODE: {
      '08': aCode(),
      '999': aCode({
        value: '999',
        label: 'Unknown',
        codeStatus: {status: 'Deprecated'},
      }),
    },
    ...overrides,
  };
}

/** Convenience: a tenant/facility/patient trio with consistent ids. */
export function aContext(
  overrides: { tenantId?: number; facilityId?: number; patientId?: number } = {},
) {
  const tenantId = overrides.tenantId ?? 1;
  const facilityId = overrides.facilityId ?? 2;
  const patientId = overrides.patientId ?? 3;
  return {
    tenantId,
    facilityId,
    patientId,
    tenant: aTenant({id: tenantId}),
    facility: aFacility({id: facilityId}),
    patient: aPatient({id: patientId}),
  };
}
