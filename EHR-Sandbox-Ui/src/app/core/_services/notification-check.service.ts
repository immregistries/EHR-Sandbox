import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { FacilityService } from './facility.service';
import { SettingsService } from './settings.service';
import { TenantService } from './tenant.service';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
@Injectable({
  providedIn: 'root'
})
export class NotificationCheckService {


  constructor(private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService) { }

  readNotification(lastRefreshTime: number): Observable<boolean> {
    const tenantId = this.tenantService.getCurrentId()
    const facilityId = this.facilityService.getCurrentId()
    if (tenantId > 0 && facilityId > 0) {
      return this.http.get<boolean>(
        `${this.settings.getApiUrl()}/$notification/${lastRefreshTime}`, httpOptions);
        // `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/notification/${timestamp}`, httpOptions);
    } else {
      return of(false)
    }
  }


}
