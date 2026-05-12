import {EhrPatient, VaccinationEvent} from "./rest";

export interface ReceivedHistoryDTO {
  patient?: EhrPatient,
  vaccinationEvents: VaccinationEvent[],
}
