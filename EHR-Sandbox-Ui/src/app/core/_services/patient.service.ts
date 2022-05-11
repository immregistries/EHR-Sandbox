import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, of, throwError } from 'rxjs';
import { SettingsService } from './settings.service';
import { Patient} from '../_model/rest';
import { FacilityService } from './facility.service';
import { TenantService } from './tenant.service';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
/**
 * Patient Service interacting with the API, and providing the global selected patient as an observable
 */
export class PatientService {

  private patient: BehaviorSubject<Patient>;

  /**
   * Global observable used to trigger a refresh for all the lists of patients, when a new patient was created
   */
  private refresh: BehaviorSubject<boolean>;

  public getRefresh(): Observable<boolean> {
    return this.refresh.asObservable();
  }

  public doRefresh(): void{
    this.refresh.next(!this.refresh.value)
  }

  public getObservablePatient(): Observable<Patient> {
    return this.patient.asObservable();
  }

  public getPatient(): Patient {
    return this.patient.value
  }

  public getPatientId(): number {
    if (this.patient.value.id) {
      return this.patient.value.id
    } else {
      return -1
    }
  }

  public setPatient(patient: Patient) {
    this.patient.next(patient)
  }

  constructor(private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService ) {
      this.patient = new BehaviorSubject<Patient>({id:-1})
      this.refresh = new BehaviorSubject<boolean>(false)
      this.facilityService.getObservableFacility().subscribe((facility) => {
        this.setPatient({})
      })
    }

  /**
   *
   * @returns Empty patient object
   */
  readEmpty(): Observable<Patient> {
    return this.http.get<Patient>(
      this.settings.getApiUrl() + '/new_patient', httpOptions);
  }

  /**
   *
   * @returns Patient object filled with random information
   */
  readRandom(): Observable<Patient> {
    return this.http.get<Patient>(
      this.settings.getApiUrl() + '/random_patient', httpOptions);
  }

  /**
   *
   * @param tenantId
   * @param facilityId
   * @returns list of patients associated to the tenant and facility
   */
  readPatients(tenantId: number, facilityId: number): Observable<Patient[]> {
    if (tenantId > 0 && facilityId > 0){
      return this.http.get<Patient[]>(
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
  readAllPatients(tenantId: number): Observable<Patient[]> {
    if (tenantId > 0){
      return this.http.get<Patient[]>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/patients`,
        httpOptions);
    }
    return of([])
  }

  /**
   *
   * @param tenantId
   * @param facilityId
   * @param patientId
   * @returns patient Get Response
   */
  readPatient(tenantId: number, facilityId: number, patientId: number): Observable<Patient> {
    return this.http.get<Patient>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patient/${patientId}`,
      httpOptions);
  }

  /**
   * Helping function for posting patient, automatically retrieving current facility and tenantId
   * @param patient
   * @returns Post patient Response
   */
  quickPostPatient(patient: Patient): Observable<HttpResponse<string>>  {
    const tenantId: number = this.tenantService.getTenantId()
    const facilityId: number = this.facilityService.getFacilityId()
    return this.postPatient(tenantId,facilityId,patient)
  }

  postPatient(tenantId: number, facilityId: number, patient: Patient): Observable<HttpResponse<string>> {
    if (tenantId > 0 && facilityId > 0){
      return this.http.post<string>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients`,
        patient,
        {observe: 'response'});
    } else {
      throw throwError(() => new Error("No facility selected"))
    }
  }

  quickPutPatient(patient: Patient): Observable<Patient>  {
    const tenantId: number = this.tenantService.getTenantId()
    const facilityId: number = this.facilityService.getFacilityId()
    return this.putPatient(tenantId,facilityId,patient)
  }

  putPatient(tenantId: number, facilityId: number, patient: Patient,): Observable<Patient> {
    if (tenantId > 0 && facilityId > 0 ){
      return this.http.put<Patient>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients`,
        patient, httpOptions);
    } else {
      throw throwError(() => new Error("No facility selected"))
    }
  }


}
