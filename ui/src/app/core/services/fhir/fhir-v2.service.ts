import {Injectable} from '@angular/core';
import {MessagingBase} from './messaging.base';

/** HL7v2 messages produced through the FHIR conversion pipeline. */
@Injectable({providedIn: 'root'})
export class FhirV2Service extends MessagingBase {
  protected readonly suffix = '/fhir';
}
