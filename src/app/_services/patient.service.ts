import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SettingsService } from './settings.service';
import { Facility, Patient, Tenant } from '../_model/rest';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
@Injectable({
  providedIn: 'root'
})
export class PatientService {

  constructor(private http: HttpClient, private settings: SettingsService ) { }

  getEmpty(): Observable<Patient> {
    return this.http.get<Patient>(
      this.settings.getApiUrl() + '/new_patient', httpOptions);
  }

  getRandom(): Observable<Patient> {
    return this.http.get<Patient>(
      this.settings.getApiUrl() + '/random_patient', httpOptions);
  }

  getPatients(tenantId: number, facilityId: number): Observable<Patient> {
    return this.http.get<Patient>(
      this.settings.getApiUrl()
      + '/tenants/' + tenantId
      +'/facilities/' + facilityId , httpOptions);
  }

  getPatient(tenantId: number, facilityId: number, patientId: number): Observable<Patient> {
    return this.http.get<Patient>(
      this.settings.getApiUrl()
      + '/tenants/' + tenantId
      + '/facilities/' + facilityId
      + '/patients/' + patientId, httpOptions);
  }

  postPatient(tenantId: number, facilityId: number, patient: Patient,): Observable<Patient> {
    return this.http.post<Patient>(
      this.settings.getApiUrl()
      + '/tenants/' + tenantId
      + '/facilities/' + facilityId
      + '/patients',
      patient, httpOptions);
  }

  putPatient(tenantId: number, facilityId: number, patient: Patient,): Observable<Patient> {
    return this.http.put<Patient>(
      this.settings.getApiUrl()
      + '/tenants/' + tenantId
      + '/facilities/' + facilityId
      + '/patients',
      patient, httpOptions);
  }


}
