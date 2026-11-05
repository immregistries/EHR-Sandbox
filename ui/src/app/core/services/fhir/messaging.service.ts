import {Observable} from 'rxjs';
import {AcknowledgementObject, Feedback} from '../../models';

/**
 * Common contract for the HL7v2 and FHIR-flavored messaging services.
 * Implementations differ only by the endpoint suffix they target.
 */
export interface MessagingContext {
  tenantId: number;
  facilityId: number;
  patientId: number;
}

export interface IMessagingService {
  getVXU(context: MessagingContext, vaccinationId: number): Observable<string>;

  getVXUAll(context: MessagingContext): Observable<string>;

  getQBP(context: MessagingContext): Observable<string>;

  postVXU(
    context: MessagingContext,
    vaccinationId: number | undefined,
    vxu: string,
    registryId?: number,
  ): Observable<AcknowledgementObject<Feedback>>;

  postQBP(
    context: MessagingContext,
    qbp: string,
    registryId?: number,
  ): Observable<AcknowledgementObject<Feedback>>;
}
