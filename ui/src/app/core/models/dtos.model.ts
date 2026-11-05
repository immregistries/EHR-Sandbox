import {EhrPatient, VaccinationEvent} from "./rest.model";

export interface ReceivedHistoryDTO {
  patient?: EhrPatient,
  vaccinationEvents: VaccinationEvent[],
}
