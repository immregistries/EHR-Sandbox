import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Group, GroupMember, Identifier } from 'fhir/r5';
import { BehaviorSubject, map, Observable, of } from 'rxjs';
import { FacilityService } from './facility.service';
import { SettingsService } from './settings.service';
import { TenantService } from './tenant.service';
import { ImmunizationRegistryService } from './immunization-registry.service';
import { CurrentSelectedService } from './_abstract/current-selected.service';
import { SnackBarService } from './snack-bar.service';

/**
 * Deprecated
 */
const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
@Injectable({
  providedIn: 'root'
})
export class RemoteGroupService extends CurrentSelectedService<Group> {

  constructor(private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService,
    private immunizationRegistryService: ImmunizationRegistryService,
    snackBarService: SnackBarService
  ) {
    super(new BehaviorSubject<Group>({ resourceType: "Group", type: "person", membership: "definitional" }), snackBarService)
  }

  readSample(): Observable<string> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId = this.facilityService.getCurrentId()
    const registryId: number = this.immunizationRegistryService.getCurrentId()
    if (this.idsNotValid(tenantId, facilityId, registryId)) {
      return of("")
    }
    return this.http.get<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/registry/${registryId}/groups/sample`,
      httpOptions);
  }

  readGroups(): Observable<Group[]> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    const registryId: number | undefined = this.immunizationRegistryService.getCurrentId()
    if (this.idsNotValid(tenantId, facilityId, registryId)) {
      return of([])
    }
    return this.http.get<string[]>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/registry/${registryId}/groups`,
      httpOptions)
      .pipe(map((array: string[]) => { return array.map((json) => { return (JSON.parse(json) as Group) }) }));

  }

  addMember(groupId: string, patientId: string): Observable<string> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    const registryId: number | undefined = this.immunizationRegistryService.getCurrentId()
    if (this.idsNotValid(tenantId, facilityId, registryId)) {
      return of("")
    }
    return this.http.post<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/registry/${registryId}/groups/${groupId}/$member-add`, null,
      {
        headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
        params: new HttpParams()
          .append("patientId", patientId)
          .append("match", true)
      }
    )
  }

  removeMember(groupId: string, patientId?: string, reference?: string, identifier?: Identifier): Observable<string> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    const registryId: number | undefined = this.immunizationRegistryService.getCurrentId()
    let params: HttpParams = new HttpParams();
    if (patientId) {
      params = params.append("patientId", patientId)
    }
    if (reference) {
      params = params.append("reference", reference)
    }
    if (identifier) {
      params = params.append("identifier", JSON.stringify(identifier))
    }
    if (this.idsNotValid(tenantId, facilityId, registryId)) {
      return of("")
    }
    return this.http.post<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/registry/${registryId}/groups/${groupId}/$member-remove`, null,
      {
        headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
        params: params
      }
    )

  }

  triggerFetch(): Observable<Group[]> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    const registryId: number | undefined = this.immunizationRegistryService.getCurrentId()
    if (this.idsNotValid(tenantId, facilityId, registryId)) {
      return of([])
    }
    return this.http.get<string[]>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/registry/${registryId}/groups/$fetch`,
      httpOptions)
      .pipe(map((array: string[]) => { return array.map((json) => { return (JSON.parse(json) as Group) }) }));

  }



}
