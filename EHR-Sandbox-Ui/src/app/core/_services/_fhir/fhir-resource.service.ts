import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of, switchMap } from 'rxjs';
import { SettingsService } from '../settings.service';
import { FacilityService } from '../facility.service';
import { TenantService } from '../tenant.service';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
@Injectable({
  providedIn: 'root'
})
/**
 * Service to get FHIR resources equivalents from local data objects
 */
export class FhirResourceService {

  constructor(private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService) { }

  /**
   *
   * @param patientId
   * @param vaccinationId
   * @returns Vaccination information converted to XML FHIR Immunization resource
   */
  quickGetImmunizationResource(patientId: number, vaccinationId: number): Observable<string> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.http.get(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/vaccinations/${vaccinationId}/resource`,
      { responseType: 'text' });
  }

  quickGetPatientResource(patientId: number): Observable<string> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    if (tenantId < 0 || facilityId < 0 || patientId < 0) {
      return of('')
    } else {
      return this.http.get(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/resource`,
        { responseType: 'text' });
    }
  }

  quickGetGroupResource(groupId: number): Observable<string> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    if (tenantId < 0 || facilityId < 0 || groupId < 0) {
      return of('')
    } else {
      return this.http.get(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/groups/${groupId}/resource`,
        { responseType: 'text' });
    }
  }

  quickGetOrganizationResource(facilityId: number): Observable<string> {
    const tenantId: number = this.tenantService.getCurrentId()
    if (tenantId < 0 || facilityId < 0) {
      return of('')
    } else {
      return this.http.get(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/resource`,
        { responseType: 'text' });
    }
  }

  getClinicianResource(clinicianId: number): Observable<string> {
    const tenantId: number = this.tenantService.getCurrentId()
    if (tenantId < 0 || clinicianId < 0) {
      return of('')
    } else {
      return this.http.get(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/clinicians/${clinicianId}/resource`,
        { responseType: 'text' });
    }
  }

  getVaccinationExportBundle(patientId: number, vaccinationId: number): Observable<string> {
    return new Observable((subscriber) => subscriber.next(this.tenantService.getCurrentId() > 0 && this.facilityService.getCurrentId() > 0))
      .pipe(switchMap((value) => {
        if (value) {
          return this.http.get(`${this.settings.getApiUrl()}/tenants/${this.tenantService.getCurrentId()}/facilities/${this.facilityService.getCurrentId()}/patients/${patientId}/vaccinations/${vaccinationId}/bundle`,
            { responseType: 'text' });
        } else {
          return of("")
        }
      }))
  }

  getPatientExportBundle(patientId: number): Observable<string> {
    return new Observable((subscriber) => subscriber.next(this.tenantService.getCurrentId() > 0 && this.facilityService.getCurrentId() > 0))
      .pipe(switchMap((value) => {
        if (value) {
          return this.http.get(`${this.settings.getApiUrl()}/tenants/${this.tenantService.getCurrentId()}/facilities/${this.facilityService.getCurrentId()}/patients/${patientId}/bundle`,
            { responseType: 'text' });
        } else {
          return of("")
        }
      }))
  }

  getFacilityExportBundle(facilityId: number): Observable<string> {
    return new Observable((subscriber) => subscriber.next(this.tenantService.getCurrentId() > 0))
      .pipe(switchMap((value) => {
        if (value) {
          return this.http.get(`${this.settings.getApiUrl()}/tenants/${this.tenantService.getCurrentId()}/facilities/${facilityId}/bundle`,
            { responseType: 'text' });
        } else {
          return of("")
        }
      }))
  }

}
