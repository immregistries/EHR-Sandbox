import { Observable } from "rxjs"
import { Feedback } from "../../_model/rest";
import { AcknowledgementObject } from "../../_model/form-structure";

export interface IMessagingService {
  getVXU(patientId: number, vaccinationId: number): Observable<string>

  getVXUAll(patientId: number): Observable<string>

  getQBP(patientId: number): Observable<string>

  quickPostVXU(patientId: number, vaccinationId: number | undefined, vxu: string): Observable<AcknowledgementObject<Feedback>>;

  quickPostQBP(patientId: number, qbp: string): Observable<AcknowledgementObject<Feedback>>;
}
