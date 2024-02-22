import { HttpClient, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Group, GroupMember, Identifier } from 'fhir/r5';
import { BehaviorSubject, map, Observable, of, switchMap } from 'rxjs';
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
export class GroupService extends CurrentSelectedService<EhrGroup> {

  if_valid_parent_ids: Observable<boolean> = new Observable((subscriber) => subscriber.next(this.tenantService.getCurrentId() > 0 && this.facilityService.getCurrentId() > 0))


  constructor(private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService,
    private immunizationRegistryService: ImmunizationRegistryService) {
    super(new BehaviorSubject<EhrGroup>({}))
  }

  /**
   *
   * @returns list of patients associated to the tenant and facility selected in their respected services
   */
  quickReadGroups(): Observable<EhrGroup[]> {
    console.log("jwehbfi2")
    return this.if_valid_parent_ids.pipe(switchMap((value) => {
      console.log("jwehbfi2", value)

      if (value) {
        return this.http.get<EhrGroup[]>(
          `${this.settings.getApiUrl()}/tenants/${this.tenantService.getCurrentId()}/facilities/${this.facilityService.getCurrentId()}/groups`,
          httpOptions)
      } else {
        return of([])
      }
    }))
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

  postGroup(group: EhrGroup): Observable<HttpResponse<string>> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    // const registryId: number | undefined = this.immunizationRegistryService.getCurrentId()
    if (tenantId > 0 && facilityId > 0) {
      return this.http.post<HttpResponse<string>>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/groups`, group,
        httpOptions);
    } else {
      return of()
    }
  }

  putGroup(group: EhrGroup): Observable<EhrGroup> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    // const registryId: number | undefined = this.immunizationRegistryService.getCurrentId()
    if (tenantId > 0 && facilityId > 0) {
      return this.http.put<EhrGroup>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/groups`, group,
        httpOptions);
    } else {
      return of()
    }
  }

  getAllGroups(): Observable<EhrGroup[]> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    // const registryId: number | undefined = this.immunizationRegistryService.getCurrentId()
    if (tenantId > 0 && facilityId > 0) {
      return this.http.get<EhrGroup[]>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/groups`,
        httpOptions);
    } else {
      return of()
    }
  }

  getGroup(groupId: number): Observable<EhrGroup> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    // const registryId: number | undefined = this.immunizationRegistryService.getCurrentId()
    if (tenantId > 0 && facilityId > 0) {
      return this.http.get<EhrGroup>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/groups/${groupId}`,
        httpOptions);
    } else {
      return of()
    }
  }

  getGroupFromName(name: String): Observable<EhrGroup> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    // const registryId: number | undefined = this.immunizationRegistryService.getCurrentId()
    // let options = {
    //   headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
    //   param: {'name': groupName}
    // };
    if (tenantId > 0 && facilityId > 0) {
      return this.http.get<EhrGroup>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/groups?name=${name}`,
        httpOptions);
    } else {
      return of()
    }
  }

  getRandom(): Observable<EhrGroup> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    if (tenantId > 0 && facilityId > 0) {
      return this.http.get<EhrGroup>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/groups/random`,
        httpOptions);
    } else {
      return of()
    }
  }

  addMember(groupId: number, patientId: String): Observable<EhrGroup> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    if (tenantId > 0 && facilityId > 0) {
      return this.http.post<EhrGroup>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/groups/${groupId}/$add?patientId=${patientId}`,
        httpOptions);
    } else {
      return of()
    }
  }

  removeMember(groupId: number, patientId: number): Observable<EhrGroup> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    if (tenantId > 0 && facilityId > 0) {
      return this.http.post<EhrGroup>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/groups/${groupId}/$remove?patientId=${patientId}`,
        httpOptions);
    } else {
      return of()
    }
  }

  refreshGroup(groupId: number | undefined): Observable<EhrGroup> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    // const registryId: number | undefined = this.immunizationRegistryService.getCurrentId()
    if (tenantId > 0 && facilityId > 0) {
      return this.http.get<EhrGroup>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/groups/${groupId}/$refresh`,
        httpOptions);
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
