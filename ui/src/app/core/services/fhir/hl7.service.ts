import {Injectable} from '@angular/core';
import {MessagingBase} from './messaging.base';

/** Raw HL7v2 messaging. */
@Injectable({providedIn: 'root'})
export class Hl7Service extends MessagingBase {
  protected readonly suffix = '';
}
