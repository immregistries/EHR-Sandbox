import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';
import { EhrSubscription } from '../../core/_model/rest';
import { FacilityService } from '../../core/_services/facility.service';
import { SettingsService } from '../../core/_services/settings.service';
import { TenantService } from '../../core/_services/tenant.service';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {
  private refresh: BehaviorSubject<boolean>;

  public getRefresh(): Observable<boolean> {
    return this.refresh.asObservable();
  }

  public doRefresh(): void{
    this.refresh.next(!this.refresh.value)
  }

  constructor(private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService,
    private immRegistriesService: ImmunizationRegistryService) {
      this.refresh = new BehaviorSubject<boolean>(false)
  }

  createSubscription(): Observable<boolean | null> {
    const tenantId: number = this.tenantService.getTenantId()
    const facilityId: number = this.facilityService.getFacilityId()
    const immRegistryId: number | undefined = this.immRegistriesService.getImmRegistryId()
    return this.http.post<any>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/imm-registry/${immRegistryId}/subscription`,
      httpOptions);
  }

  readSubscription(): Observable<EhrSubscription> {
    const tenantId: number = this.tenantService.getTenantId()
    const facilityId = this.facilityService.getFacilityId()
    if (facilityId < 0) {
      return of()
    }
    return this.http.get<EhrSubscription>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/subscription`,
      httpOptions);
  }

}
