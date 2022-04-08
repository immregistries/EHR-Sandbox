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

  getVXU(tenantId: number, facilityId: number, patientId: number, vaccinationId: number): Observable<string> {
    return this.http.get(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/${vaccinationId}/vxu`,
      { responseType: 'text' });
  }

  quickPostVXU(patientId: number, vaccinationId: number, vxu: string): Observable<string> {
    const tenantId: number = this.tenantService.getTenantId()
    const facilityId: number = this.facilityService.getFacilityId()
    return this.postVXU(tenantId,facilityId,patientId,vaccinationId,vxu)
  }

  postVXU(tenantId: number, facilityId: number, patientId: number, vaccinationId: number, vxu: string): Observable<string> {
    return this.http.post<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/${vaccinationId}/vxu`,
      vxu,
      httpOptions);
  }
}
