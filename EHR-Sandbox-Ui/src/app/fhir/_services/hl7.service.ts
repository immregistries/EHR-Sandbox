import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { SettingsService } from '../../core/_services/settings.service';
import { FacilityService } from '../../core/_services/facility.service';
import { TenantService } from '../../core/_services/tenant.service';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
/**
 * Service allowing the use of the HL7 and Vxu related functionnalities of the API
 */
export class Hl7Service {

  constructor(private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService,
    private registryService: ImmunizationRegistryService) { }

  /**
   *
   * @param tenantId
   * @param facilityId
   * @param patientId
   * @param vaccinationId
   * @returns Hl7v2 VXU message
   */
  getVXU(patientId: number, vaccinationId: number): Observable<string> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.http.get(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/${vaccinationId}/vxu`,
      { responseType: 'text' });
  }

  /**
   *
   * @param tenantId
   * @param facilityId
   * @param patientId
   * @returns Hl7v2 VXU message
   */
  getQBP(patientId: number): Observable<string> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.http.get(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/qbp`,
      { responseType: 'text' });
  }

  /**
   * Helping function for sending post request with Vxu Message to the IIS
   * @param patientId
   * @param vaccinationId
   * @param vxu
   * @returns IIS answer
   */
  quickPostVXU(patientId: number, vaccinationId: number, vxu: string): Observable<string> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    const registryId = this.registryService.getCurrentId()
    return this.http.post<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/${vaccinationId}/vxu/registry/${registryId}`,
      vxu,
      httpOptions);
  }

  /**
   * Helping function for sending post request with Vxu Message to the IIS
   * @param patientId
   * @param vaccinationId
   * @param qbp
   * @returns IIS answer
   */
  quickPostQBP(patientId: number, qbp: string): Observable<string> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    const registryId = this.registryService.getCurrentId()
    return this.http.post<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/qbp/registry/${registryId}`,
      qbp,
      httpOptions);
  }

}
