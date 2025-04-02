import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, combineLatest, map, Observable, of, share, shareReplay, startWith, switchMap, tap } from 'rxjs';
import { FacilityService } from './facility.service';
import { SettingsService } from './settings.service';
import { TenantService } from './tenant.service';
import { ImmunizationRegistryService } from './immunization-registry.service';
import { CurrentSelectedService } from './_abstract/current-selected.service';
import { EhrGroup } from '../_model/rest';
import { SnackBarService } from './snack-bar.service';


const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
@Injectable({
  providedIn: 'root'
})
export class GroupService extends CurrentSelectedService<EhrGroup> {

  private readonly if_valid_parent_ids: Observable<boolean> = new Observable((subscriber) => subscriber.next(this.tenantService.getCurrentId() > 0 && this.facilityService.getCurrentId() > 0))

  public readonly quickReadObservable: Observable<EhrGroup[]> = combineLatest([
    this.getRefresh().pipe(startWith(false)), // Start with null to trigger initially
    this.tenantService.getCurrentObservable(), // Start with the initial ID
    this.facilityService.getCurrentObservable() // Start with the initial ID
  ]).pipe(tap(() => this.loading = true))
    .pipe(switchMap(([_, tenant, facility]) => tenant?.id > 0 && facility?.id && facility.id > 0 ? this.readGroups(tenant.id, facility.id) : of([])))
    .pipe(shareReplay({ bufferSize: 1, refCount: true }))
    .pipe(tap(() => this.loading = false))


  constructor(private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService,
    private immunizationRegistryService: ImmunizationRegistryService,
    snackBarService: SnackBarService
  ) {
    super(new BehaviorSubject<EhrGroup>({}), snackBarService)
  }

  /**
   *
   * @returns list of patients associated to the tenant and facility selected in their respected services
   */
  quickReadGroups(): Observable<EhrGroup[]> {
    return this.quickReadObservable
  }

  /**
   *
   * @returns list of patients associated to the tenant and facility selected in their respected services
   */
  readGroups(tenantId: number, facilityId: number): Observable<EhrGroup[]> {
    return this.http.get<EhrGroup[]>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/groups`,
      httpOptions).pipe(share())
  }

  getRandom(): Observable<EhrGroup> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    if (tenantId > 0 && facilityId > 0) {
      return this.http.get<EhrGroup>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/groups/$random`,
        httpOptions);
    } else {
      return of()
    }
  }

  postGroup(group: EhrGroup): Observable<HttpResponse<string>> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    // const registryId: number | undefined = this.immunizationRegistryService.getCurrentId()
    if (tenantId > 0 && facilityId > 0) {
      return this.http.post<string>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/groups`, group,
        { ...httpOptions, observe: 'response' });
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

  getGroupBulkImportStatus(groupId: number): Observable<{}> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    // const registryId: number | undefined = this.immunizationRegistryService.getCurrentId()
    if (tenantId > 0 && facilityId > 0 && groupId > 0) {
      return this.http.get<{}>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/groups/${groupId}/$import-status`,
        httpOptions);
    } else {
      return of()
    }
  }
  groupBulkImportStatusForceRefresh(groupId: number): Observable<{}> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    // const registryId: number | undefined = this.immunizationRegistryService.getCurrentId()
    if (tenantId > 0 && facilityId > 0 && groupId > 0) {
      return this.http.get<{}>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/groups/${groupId}/$import-status-refresh`,
        httpOptions);
    } else {
      return of()
    }
  }
  getGroupBulkViewResult(groupId: number, body: string): Observable<[]> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    if (tenantId > 0 && facilityId > 0) {
      return this.http.post<[]>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/groups/${groupId}/$import-view-result`, body,
        httpOptions
      );
    } else {
      return of()
    }
  }



  groupBulkImportKickoff(groupId: number): Observable<{}> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    if (tenantId > 0 && facilityId > 0) {
      return this.http.get<{}>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/groups/${groupId}/$import`,
        httpOptions);
    } else {
      return of()
    }
  }

  getGroupFromName(name: string): Observable<EhrGroup> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    // const registryId: number | undefined = this.immunizationRegistryService.getCurrentId()
    // let options = {
    //   headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
    //   param: {'name': groupName}
    // };
    if (tenantId > 0 && facilityId > 0) {
      return this.http.get<EhrGroup>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/groups`,
        { ...httpOptions, params: { 'name': name } });
    } else {
      return of()
    }
  }



  addMember(groupId: number, patientId: string): Observable<EhrGroup> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    if (tenantId > 0 && facilityId > 0) {
      return this.http.post<EhrGroup>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/groups/${groupId}/$add`, null,
        { ...httpOptions, params: { 'patientId': patientId } });
    } else {
      return of()
    }
  }

  removeMember(groupId: number, patientId: number): Observable<EhrGroup> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    if (tenantId > 0 && facilityId > 0) {
      return this.http.post<EhrGroup>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/groups/${groupId}/$remove`, null,
        { ...httpOptions, params: { 'patientId': patientId } });
    } else {
      return of()
    }
  }

  refreshGroup(groupId: number | undefined): Observable<EhrGroup> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    // const registryId: number | undefined = this.immunizationRegistryService.getCurrentId()
    if (tenantId > 0 && facilityId > 0 && groupId && groupId > -1) {
      return this.http.get<EhrGroup>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/groups/${groupId}/$refresh`,
        httpOptions).pipe(share());
    } else {
      return of()
    }
  }

}
