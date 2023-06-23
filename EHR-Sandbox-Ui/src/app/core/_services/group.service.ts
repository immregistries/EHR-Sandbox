import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Group } from 'fhir/r5';
import { map, Observable, of } from 'rxjs';
import { FacilityService } from './facility.service';
import { SettingsService } from './settings.service';
import { TenantService } from './tenant.service';
import { ImmunizationRegistryService } from './immunization-registry.service';
import { RefreshService } from './refresh.service';


const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
@Injectable({
  providedIn: 'root'
})
export class GroupService extends RefreshService {

  constructor(private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService,
    private immunizationRegistryService: ImmunizationRegistryService) {
      super()
  }
  readGroups(): Observable<Group[]>{
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    const registryId: number | undefined = this.immunizationRegistryService.getCurrentId()
    if (tenantId > 0 && facilityId > 0 && registryId && registryId > 0){
      return this.http.get<string[]>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/imm-registry/${registryId}/groups`,
        httpOptions)
          .pipe(map((array: string[]) => {return array.map((json) => { return (JSON.parse(json) as Group)})}));
    } else {
      return of([])
    }
  }

  addMember(patientId: string): Observable<Group[]>{
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    const registryId: number | undefined = this.immunizationRegistryService.getCurrentId()
    let params: HttpParams = new HttpParams().append("patientId", patientId)
    if (tenantId > 0 && facilityId > 0 && registryId && registryId > 0){
      return this.http.post<string[]>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/imm-registry/${registryId}/groups/$member-add`, null,
        {
          headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
          params: params
        }
        ).pipe(map((array: string[]) => {return array.map((json) => { return (JSON.parse(json) as Group)})}));
    } else {
      return of([])
    }
  }

  triggerFetch(): Observable<Group[]>{
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    const registryId: number | undefined = this.immunizationRegistryService.getCurrentId()
    if (tenantId > 0 && facilityId > 0  && registryId > 0){
      return this.http.get<string[]>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/imm-registry/${registryId}/groups/$fetch`,
        httpOptions)
          .pipe(map((array: string[]) => {return array.map((json) => { return (JSON.parse(json) as Group)})}));
    } else {
      return of([])
    }
  }



}
