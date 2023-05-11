import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, of, throwError } from 'rxjs';
import { SettingsService } from './settings.service';
import { EhrPatient, Revision} from '../_model/rest';
import { FacilityService } from './facility.service';
import { TenantService } from './tenant.service';
import { RefreshService } from './refresh.service';
import { CurrentSelectedService } from './current-selected.service';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
/**
 * Patient Service interacting with the API, and providing the global selected patient as an observable
 */
export class PatientService extends  CurrentSelectedService<EhrPatient> {

  constructor(private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService ) {
      super(new BehaviorSubject<EhrPatient>({id:-1}))
      this.facilityService.getCurrentObservable().subscribe((facility) => {
        this.setCurrent({})
      })
    }


  /**
   *
   * @returns Patient object filled with random information
   */
  readRandom(): Observable<EhrPatient> {
    return this.http.get<EhrPatient>(
      this.settings.getApiUrl() + '/$random_patient', httpOptions);
  }

  /**
   *
   * @returns list of patients associated to the tenant and facility selected in their respected services
   */
   quickReadPatients(): Observable<EhrPatient[]> {
    const tenantId = this.tenantService.getCurrentId()
    const facilityId = this.facilityService.getCurrentId()
    return this.readPatients(tenantId, facilityId)
  }


  /**
   *
   * @param tenantId
   * @param facilityId
   * @returns list of patients associated to the tenant and facility
   */
  readPatients(tenantId: number, facilityId: number): Observable<EhrPatient[]> {
    if (tenantId > 0 && facilityId > 0){
      return this.http.get<EhrPatient[]>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients`,
        httpOptions);
    }
    return of([])
  }

  /**
   *
   * @param tenantId
   * @returns list of patients associated to the tenant
   */
  readAllPatients(tenantId: number): Observable<EhrPatient[]> {
    if (tenantId > 0){
      return this.http.get<EhrPatient[]>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/patients`,
        httpOptions);
    }
    return of([])
  }

  quickReadPatient(patientId: number):  Observable<EhrPatient> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.readPatient(tenantId,facilityId,patientId)
  }

  /**
   *
   * @param tenantId
   * @param facilityId
   * @param patientId
   * @returns patient Get Response
   */
  readPatient(tenantId: number, facilityId: number, patientId: number): Observable<EhrPatient> {
    return this.http.get<EhrPatient>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}`,
      httpOptions);
  }

  readPatientHistory(patientId: number): Observable<Revision<EhrPatient>[]> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.http.get<Revision<EhrPatient>[]>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/$history`,
      httpOptions);
  }

  /**
   * Helping function for posting patient, automatically retrieving current facility and tenantId
   * @param patient
   * @returns Post patient Response
   */
  quickPostPatient(patient: EhrPatient): Observable<HttpResponse<string>>  {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.postPatient(tenantId,facilityId,patient)
  }

  postPatient(tenantId: number, facilityId: number, patient: EhrPatient): Observable<HttpResponse<string>> {
    if (tenantId > 0 && facilityId > 0){
      return this.http.post<string>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients`,
        patient,
        {observe: 'response'});
    } else {
      throw throwError(() => new Error("No facility selected"))
    }
  }

  postPatientOnFacilityOnly(facilityId: number, patient: EhrPatient): Observable<HttpResponse<string>> {
    if (facilityId > 0){
      return this.http.post<string>(
        `${this.settings.getApiUrl()}/facilities/${facilityId}/patients`,
        patient,
        {observe: 'response'});
    } else {
      throw throwError(() => new Error("No facility selected"))
    }
  }

  quickPutPatient(patient: EhrPatient): Observable<EhrPatient>  {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    return this.putPatient(tenantId,facilityId,patient)
  }

  putPatient(tenantId: number, facilityId: number, patient: EhrPatient,): Observable<EhrPatient> {
    if (tenantId > 0 && facilityId > 0 ){
      return this.http.put<EhrPatient>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients`,
        patient, httpOptions);
    } else {
      throw throwError(() => new Error("No facility selected"))
    }
  }


}
