import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { SettingsService } from './settings.service';
import { FacilityService } from './facility.service';
import { TenantService } from './tenant.service';

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
    private tenantService: TenantService ) { }

  quickGetVXU(patientId: number, vaccinationId: number): Observable<string> {
    const tenantId: number = this.tenantService.getTenantId()
    const facilityId: number = this.facilityService.getFacilityId()
    return this.getVXU(tenantId,facilityId,patientId,vaccinationId)
  }

  /**
   *
   * @param tenantId
   * @param facilityId
   * @param patientId
   * @param vaccinationId
   * @returns Hl7v2 VXU message
   */
  getVXU(tenantId: number, facilityId: number, patientId: number, vaccinationId: number): Observable<string> {
    return this.http.get(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/${vaccinationId}/vxu`,
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
    const tenantId: number = this.tenantService.getTenantId()
    const facilityId: number = this.facilityService.getFacilityId()
    return this.postVXU(tenantId,facilityId,patientId,vaccinationId,vxu)
  }

  /**
   * Sends post request with Vxu Message to the IIS
   * @param tenantId
   * @param facilityId
   * @param patientId
   * @param vaccinationId
   * @param vxu
   * @returns IIS answer
   */
  postVXU(tenantId: number, facilityId: number, patientId: number, vaccinationId: number, vxu: string): Observable<string> {
    return this.http.post<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/${vaccinationId}/vxu`,
      vxu,
      httpOptions);
  }
}
