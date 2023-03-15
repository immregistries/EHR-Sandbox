import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';

import { Facility } from '../_model/rest';
import { BehaviorSubject, Observable, of, share } from 'rxjs';
import { SettingsService } from './settings.service';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
/**
 * Facility Service interacting with the API, and providing the global selected facility as an observable
 */
export class FacilityService {

  private facility: BehaviorSubject<Facility>;
  /**
   * Global observable used to trigger a refresh for all the facility lists
   */
  private refresh: BehaviorSubject<boolean>;

  private lastRefreshTime: number;

  public getLastRefreshTime(): number {
    return this.lastRefreshTime;
  }

  public updateLastRefreshtime(): number {
    this.lastRefreshTime = new Date().getTime()
    return this.lastRefreshTime;
  }

  public getRefresh(): Observable<boolean> {
    return this.refresh.asObservable();
  }

  public doRefresh(): void{
    this.refresh.next(!this.refresh.value)
    this.updateLastRefreshtime()
  }

  public getObservableFacility(): Observable<Facility> {
    return this.facility.asObservable();
  }

  public getFacility(): Facility {
    return this.facility.value
  }

  public getFacilityId(): number {
    return this.facility.value.id
  }

  public setFacility(facility: Facility) {
    this.facility.next(facility)
    this.updateLastRefreshtime()
  }

  constructor(private http: HttpClient,
    private settings: SettingsService,
    ) {
    this.facility = new BehaviorSubject<Facility>({id:-1})
    this.refresh = new BehaviorSubject<boolean>(false)
    this.lastRefreshTime = new Date().getTime()
   }

  readAllFacilities(): Observable<Facility[]> {
    return this.http.get<Facility[]>(
      `${this.settings.getApiUrl()}/facilities`,
      httpOptions);
  }

  readFacilities(tenantId: number): Observable<Facility[]> {
    if (tenantId > 0){
      return this.http.get<Facility[]>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities`,
        httpOptions).pipe(share());
    }
    return of([])
  }

  readFacility(tenantId: number, facilityId: number): Observable<Facility> {
    return this.http.get<Facility>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}`,
      httpOptions);
  }

  postFacility(tenantId: number, facility: Facility): Observable<HttpResponse<Facility>> {
    return this.http.post<Facility>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities`,
      facility, {observe: 'response'})
  }
}
