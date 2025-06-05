import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { SettingsService } from '../settings.service';
import { FacilityService } from '../facility.service';
import { TenantService } from '../tenant.service';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';
import { AcknowledgementObject } from '../../_model/form-structure';
import { Feedback } from '../../_model/rest';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
/**
 * Service allowing the use of the HL7 and Vxu related functionnalities of the API
 */
export class FhirV2Service {

  constructor(private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService,
    private registryService: ImmunizationRegistryService) { }

  /**
   *
   * @param patientId
   * @param vaccinationId
   * @returns Hl7v2 VXU message for single vaccination
   */
  getVXU(patientId: number, vaccinationId: number): Observable<string> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.http.get(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/${vaccinationId}/vxu/fhir`,
      { ...httpOptions, responseType: 'text' });
  }

  /**
   *
   * @param patientId
   * @param vaccinationId
   * @returns Hl7v2 VXU message
   */
  getVXUAll(patientId: number): Observable<string> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.http.get(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vxu/fhir`,
      { ...httpOptions, responseType: 'text' });
  }

  /**
   *
   * @param patientId
   * @returns Hl7v2 QBP message
   */
  getQBP(patientId: number): Observable<string> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.http.get(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/qbp`,
      { ...httpOptions, responseType: 'text' });
  }

  /**
   * Helping function for sending post request with Vxu Message to the IIS
   * @param patientId
   * @param vaccinationId
   * @param vxu
   * @returns IIS answer
   */
  quickPostVXU(patientId: number, vaccinationId: number | undefined, vxu: string): Observable<AcknowledgementObject<Feedback>> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    const registryId = this.registryService.getCurrentId()
    let baseUri = `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}`;
    if (vaccinationId && vaccinationId > -1) {
      baseUri += `/vaccinations/${vaccinationId}`
    }
    return this.http.post<AcknowledgementObject<Feedback>>(
      `${baseUri}/vxu/fhir`,
      vxu,
      {
        ...httpOptions,
        // responseType: 'text',
        params: { registryId: registryId }
      });
  }

  /**
   * Helping function for sending post request with Vxu Message to the IIS
   * @param patientId
   * @param vaccinationId
   * @param qbp
   * @returns IIS answer
   */
  quickPostQBP(patientId: number, qbp: string): Observable<AcknowledgementObject<Feedback>> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    const registryId = this.registryService.getCurrentId()
    return this.http.post<AcknowledgementObject<Feedback>>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/qbp/fhir`,
      qbp,
      {
        ...httpOptions,
        // responseType: 'text',
        params: { registryId: registryId }
      });
  }

}
