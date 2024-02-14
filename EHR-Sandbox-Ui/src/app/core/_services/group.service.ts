import { HttpClient, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Group, GroupMember, Identifier } from 'fhir/r5';
import { BehaviorSubject, map, Observable, of } from 'rxjs';
import { FacilityService } from './facility.service';
import { SettingsService } from './settings.service';
import { TenantService } from './tenant.service';
import { ImmunizationRegistryService } from './immunization-registry.service';
import { CurrentSelectedService } from './current-selected.service';
import { EhrGroup } from '../_model/rest';


const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
@Injectable({
  providedIn: 'root'
})
export class GroupService extends CurrentSelectedService<Group> {

  constructor(private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService,
    private immunizationRegistryService: ImmunizationRegistryService) {
      super(new BehaviorSubject<Group>({resourceType: "Group", type: "person", membership: "definitional"}))
  }

  readRandom(): Observable<EhrGroup> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId = this.facilityService.getCurrentId()
    const registryId: number | undefined = this.immunizationRegistryService.getCurrentId()
    if (facilityId < 0) {
      return of()
    }
    return this.http.get<EhrGroup>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/groups/random`,
      httpOptions);
  }

  postGroup(group: EhrGroup): Observable<HttpResponse<string>>{
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    // const registryId: number | undefined = this.immunizationRegistryService.getCurrentId()
    if (tenantId > 0 && facilityId > 0){
      return this.http.post<HttpResponse<string>>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/groups`,group,
        httpOptions);
    } else {
      return of()
    }
  }

  putGroup(group: EhrGroup): Observable<EhrGroup>{
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    // const registryId: number | undefined = this.immunizationRegistryService.getCurrentId()
    if (tenantId > 0 && facilityId > 0){
      return this.http.put<EhrGroup>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/groups`,group,
        httpOptions);
    } else {
      return of()
    }
  }

  getAllGroups(): Observable<EhrGroup[]>{
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    // const registryId: number | undefined = this.immunizationRegistryService.getCurrentId()
    if (tenantId > 0 && facilityId > 0){
      return this.http.get<EhrGroup[]>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/groups`,
        httpOptions);
    } else {
      return of()
    }
  }

  getGroup(groupId: number): Observable<EhrGroup>{
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    // const registryId: number | undefined = this.immunizationRegistryService.getCurrentId()
    if (tenantId > 0 && facilityId > 0){
      return this.http.get<EhrGroup>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/groups/${groupId}`,
        httpOptions);
    } else {
      return of()
    }
  }

  getGroupFromName(groupName: String): Observable<EhrGroup>{
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    // const registryId: number | undefined = this.immunizationRegistryService.getCurrentId()
    let options = {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
      param: {'name': groupName}
    };
    if (tenantId > 0 && facilityId > 0){
      return this.http.get<EhrGroup>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/groups`,
        options);
    } else {
      return of()
    }
  }

  // triggerFetch(): Observable<Group[]>{
  //   const tenantId: number = this.tenantService.getCurrentId()
  //   const facilityId: number = this.facilityService.getCurrentId()
  //   const registryId: number | undefined = this.immunizationRegistryService.getCurrentId()
  //   if (tenantId > 0 && facilityId > 0  && registryId > 0){
  //     return this.http.get<string[]>(
  //       `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/registry/${registryId}/groups/$fetch`,
  //       httpOptions)
  //         .pipe(map((array: string[]) => {return array.map((json) => { return (JSON.parse(json) as Group)})}));
  //   } else {
  //     return of([])
  //   }
  // }



}
