/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 2.35.1025 on 2022-03-11 10:41:27.

export interface SubscriptionStore {
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
  patient?: Patient  | number;
  vaccinationEvent?: VaccinationEvent | number;
  facility?: Facility | number;
  content?: string;
  code?: string;
  severity?: string;
  timestamp?: number;
}

export interface Clinician {
  id?: number;
  nameLast?: string;
  nameMiddle?: string;
  nameFirst?: string;
}

export interface Facility {
  id: number;
  nameDisplay?: string;
  facilities?: (Facility | number)[];
  patients?: (Patient | number)[];
  feedbacks?: (Feedback | number)[];
  tenant?: number;
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
}

export interface NextOfKin {
  id?: number;
  birthDate?: Date;
  nameLast?: string;
  nameFirst?: string;
  nameMiddle?: string;
  motherMaiden?: string;
  sex?: string;
  race?: string;
  addressLine1?: string;
  addressLine2?: string;
  addressCity?: string;
  addressState?: string;
  addressZip?: string;
  addressCountry?: string;
  addressCountyParish?: string;
  phone?: string;
  email?: string;
  ethnicity?: string;
}

export interface Patient {
  id?: number;
  mrn?: string;
  createdDate?: Date;
  updatedDate?: Date;
  birthDate?: Date;
  nameLast?: string;
  nameFirst?: string;
  nameMiddle?: string;
  motherMaiden?: string;
  sex?: string;
  race?: string;
  addressLine1?: string;
  addressLine2?: string;
  addressCity?: string;
  addressState?: string;
  addressZip?: string;
  addressCountry?: string;
  addressCountyParish?: string;
  phone?: string;
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
  guardianRelationship?: string;
  nextOfKins?: NextOfKin[];
  facility?: Facility | number;
  feedbacks?: (Feedback)[];
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
  enteringClinician: Clinician;
  orderingClinician: Clinician;
  administeringClinician: Clinician;
  vaccine: Vaccine;
  feedbacks?: (Feedback)[];
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
  fundingEligibility?: string;
  vaccinationEvents?: VaccinationEvent[];
}

export interface revision {

}
