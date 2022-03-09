import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

import { Facility, Tenant } from '../_model/rest';
import { Observable } from 'rxjs';
import { SettingsService } from './settings.service';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class FacilityService {

  constructor(private http: HttpClient, private settings: SettingsService ) { }

  getFacilities(): Observable<Facility> {
    return this.http.get<Facility>(
      this.settings.getApiUrl() + '/facilities', httpOptions);
  }

  getFacility(tenantId: number, facilityId: number): Observable<Facility> {
    return this.http.get<Facility>(
      this.settings.getApiUrl()
      + '/tenants/' + tenantId
      + '/facilities/' + facilityId, httpOptions);
  }

  postFacility(tenantId: number, facility: Facility): Observable<Facility> {
    return this.http.post<Facility>(
      this.settings.getApiUrl()
      + '/tenants/' + tenantId
      + '/facilities',
      facility, httpOptions);
  }
}
