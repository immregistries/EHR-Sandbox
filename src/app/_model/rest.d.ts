// Generated using typescript-generator version 1.25.322 on 2022-03-02 22:49:43.

export interface Patient {
  id?: number;
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
  vaccinationEvents?: VaccinationEvent[];
  nextOfKins?: NextOfKin[];
}

export interface VaccinationEvent {
  id: number;
  patient: Patient;
  enteringClinician: Clinician;
  orderingClinician: Clinician;
  administeringClinician: Clinician;
  vaccine: Vaccine;
  administeringFacility: Facility;
}

export interface Tenant {
  id: number;
  nameDisplay: string;
}

export interface Facility {
  id: number;
  nameDisplay: string;
  patients: Patient[];
}

export interface NextOfKin {
  id: number;
  patient: Patient;
  birthDate: LocalDate;
  nameLast: string;
  nameFirst: string;
  nameMiddle: string;
  motherMaiden: string;
  sex: string;
  race: string;
  addressLine1: string;
  addressLine2: string;
  addressCity: string;
  addressState: string;
  addressZip: string;
  addressCountry: string;
  addressCountyParish: string;
  phone: string;
  email: string;
  ethnicity: string;
}

export interface Clinician {
  id: number;
  nameLast: string;
  nameMiddle: string;
  nameFirst: string;
  vaccinationEventsEntering: VaccinationEvent[];
  vaccinationEventsOrdering: VaccinationEvent[];
  vaccinationEvents: VaccinationEvent[];
}

export interface Vaccine {
  id: number;
  createdDate: Date;
  updatedDate: Date;
  administeredDate: Date;
  vaccineCvxCode: string;
  vaccineNdcCode: string;
  vaccineMvxCode: string;
  administeredAmount: string;
  informationSource: string;
  lotNumber: string;
  expirationDate: Date;
  completionStatus: string;
  actionCode: string;
  refusalReasonCode: string;
  bodySite: string;
  bodyRoute: string;
  fundingSource: string;
  fundingEligibility: string;
  vaccinationEvents: VaccinationEvent[];
}

export interface LocalDate extends Temporal, TemporalAdjuster, ChronoLocalDate, Serializable {
  year: number;
  month: Month;
  dayOfYear: number;
  dayOfWeek: DayOfWeek;
  dayOfMonth: number;
  monthValue: number;
  chronology: IsoChronology;
}

export interface Era extends TemporalAccessor, TemporalAdjuster {
  value: number;
}

export interface IsoChronology extends AbstractChronology, Serializable {
}

export interface Temporal extends TemporalAccessor {
}

export interface TemporalAdjuster {
}

export interface ChronoLocalDate extends Temporal, TemporalAdjuster, Comparable<ChronoLocalDate> {
  era: Era;
  leapYear: boolean;
  chronology: Chronology;
}

export interface Serializable {
}

export interface TemporalAccessor {
}

export interface AbstractChronology extends Chronology {
}

export interface Chronology extends Comparable<Chronology> {
  calendarType: string;
  id: string;
}

export interface Comparable<T> {
}

export type Month = "JANUARY" | "FEBRUARY" | "MARCH" | "APRIL" | "MAY" | "JUNE" | "JULY" | "AUGUST" | "SEPTEMBER" | "OCTOBER" | "NOVEMBER" | "DECEMBER";

export type DayOfWeek = "MONDAY" | "TUESDAY" | "WEDNESDAY" | "THURSDAY" | "FRIDAY" | "SATURDAY" | "SUNDAY";
