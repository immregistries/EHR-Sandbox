/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 2.35.1025 on 2022-03-11 10:41:27.

export interface ObjectWithID {
  id?: number;
}

export interface EhrSubscription {
  identifier?: number;
  name?: string;
  status?: string;
  topic?: string;
  end?: Date;
  reason?: string;
  channelType?: string;
  header?: string;
  heartbeatPeriod?: number;
  timeout?: number;
  contentType?: string;
  content?: string;
  notificationUrlLocation?: string;
  maxCount?: number;
  subscriptionInfo: {
    eventsSinceSubscriptionStart: number;
  }
}
export interface Feedback {
  id?: number;
  iis?: string;
  patient?: EhrPatient | number;
  vaccinationEvent?: VaccinationEvent | number;
  facility?: Facility | number;
  content?: string;
  code?: string;
  severity?: string;
  timestamp?: number;
}


export interface Clinician {
  id?: number;
  tenant?: number | Tenant;
  nameLast?: string;
  nameMiddle?: string;
  nameFirst?: string;
  nameSuffix?: string;
  namePrefix?: string;
  identifiers?: EhrIdentifier[];
  qualification?: string
}

export interface Facility {
  id?: number;
  tenant?: Tenant | number;
  nameDisplay?: string;
  facilities?: Facility[];
  parentFacility?: Facility | number;
  childrenCount?: number;
  identifiers?: EhrIdentifier[],
  type?: string;
  addresses?: EhrAddress[]
}

export interface ImmunizationRegistry {
  id?: number;
  name?: string;
  user?: User | number;
  iisHl7Url?: string;
  iisFhirUrl?: string;
  iisUsername?: string;
  iisFacilityId?: string;
  iisPassword?: string;
  headers?: string;
  default?: boolean;
  description?: string;
}

export interface NextOfKin {
  id?: number;
  birthDate?: Date;
  nameLast?: string;
  nameFirst?: string;
  nameMiddle?: string;
  nameSuffix?: string;
  motherMaiden?: string;
  sex?: string;
  race?: string;
  addresses?: EhrAddress[];
  phones?: EhrPhoneNumber[];
  email?: string;
  ethnicity?: string;
}

export interface EhrPatient extends ObjectWithID {
  id?: number;
  identifiers?: EhrIdentifier[];
  createdDate?: Date;
  updatedDate?: Date;
  birthDate?: Date;
  nameLast?: string;
  nameFirst?: string;
  nameMiddle?: string;
  nameSuffix?: string;
  motherMaiden?: string;
  sex?: string;
  races?: EhrRace[];
  addresses?: EhrAddress[];
  phones?: EhrPhoneNumber[];
  email?: string;
  ethnicity?: string;
  birthFlag?: string;
  birthOrder?: string;
  deathFlag?: string;
  deathDate?: Date;
  publicityIndicator?: string;
  publicityIndicatorDate?: Date;
  protectionIndicator?: string;
  protectionIndicatorDate?: Date;
  registryStatusIndicator?: string;
  registryStatusIndicatorDate?: Date;
  guardianLast?: string;
  guardianFirst?: string;
  guardianMiddle?: string;
  guardianSuffix?: string;
  guardianRelationship?: string;
  // nextOfKins?: NextOfKin[];
  nextOfKinRelationships?: NextOfKinRelationship[];
  facility?: Facility | number;
  feedbacks?: (Feedback)[];
  groupNames?: String[];
  financialStatus?: string;
  generalPractitioner?: Clinician | number;
}

export interface Tenant {
  id: number;
  nameDisplay?: string;
  // facilities?: Facility[];
}

export interface User {
  id?: number;
  username?: string;
  password?: string;
  immunizationRegistries?: ImmunizationRegistry[];
  tenants?: (Tenant | number)[];
}

export interface VaccinationEvent {
  id?: number;
  patient?: number
  enteringClinician?: Clinician;
  orderingClinician?: Clinician;
  administeringClinician?: Clinician;
  vaccine: Vaccine;
  feedbacks?: Feedback[];
  primarySource?: boolean;
}

export interface Vaccine {
  id?: number;
  createdDate?: Date;
  updatedDate?: Date;
  administeredDate?: Date;
  vaccineCvxCode?: string;
  vaccineNdcCode?: string;
  vaccineMvxCode?: string;
  administeredAmount?: string;
  informationSource?: string;
  lotNumber?: string;
  expirationDate?: Date;
  completionStatus?: string;
  actionCode?: string;
  refusalReasonCode?: string;
  bodySite?: string;
  bodyRoute?: string;
  fundingSource?: string;
  financialStatus?: string;
  vaccinationEvents?: VaccinationEvent[];
  informationStatement?: string;
  informationStatementCvx?: string;
  informationStatementPublishedDate?: Date;
  informationStatementPresentedDate?: Date;
}


export interface EhrGroup {
  id?: number;
  facility?: Facility | number;
  name?: string;
  description?: string;
  type?: string;
  code?: string;
  immunizationRegistry?: ImmunizationRegistry;
  patientList?: (EhrPatient)[];
  identifiers?: EhrIdentifier[];
  ehrGroupCharacteristics?: EhrGroupCharacteristic[];
}


export interface EhrGroupCharacteristic extends Serializable {
  groupId?: string;
  codeValue?: string;
  codeSystem?: string;
  value?: string;
  exclude?: boolean;
  periodStart?: Date;
  periodEnd?: Date;
}
export interface EhrIdentifier extends Serializable {
  system?: string,
  value?: string,
  type?: string,
  assignerReference?: string,
}

export interface EhrPhoneNumber extends Serializable {
  number?: string,
  type?: string,
  use?: string,
}
export interface EhrRace extends Serializable {
  value?: string
}
export interface EhrAddress extends Serializable {
  addressLine1?: string;
  addressLine2?: string;
  addressCity?: string;
  addressState?: string;
  addressZip?: string;
  addressCountry?: string;
  addressCountyParish?: string;
}

export interface NextOfKinRelationship {
  nextOfKin?: NextOfKin;
  relationshipKind?: string;
}

export interface Serializable {
}


/**
 * Used for History of resources with default hibernate envers fields
 * TODO
 */
export interface Revision<T> {
  entity: T,
  metadata: {
    delegate: {
      id: number,
      immunizationRegistryId: string,
      subscriptionId: string,
      user: string,
    }
    revisionType: string,
    revisionDate?: Date,
    requiredRevisionInstant: Date,
    requiredRevisionNumber: number,
    revisionInstant?: Date,
    revisionNumber?: number,
  },
  requiredRevisionInstant: Date,
  requiredRevisionNumber: number,
  revisionInstant?: Date,
  revisionNumber?: number,
}

export interface Revisions<T> {
  content: Revision<T>[],
  latestRevision: Revision<T>,
  empty: boolean
}
