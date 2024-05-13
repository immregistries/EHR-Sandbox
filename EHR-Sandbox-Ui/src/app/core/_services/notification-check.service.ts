import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, Subscription, filter, interval, of } from 'rxjs';
import { FacilityService } from './facility.service';
import { SettingsService } from './settings.service';
import { TenantService } from './tenant.service';
import { SnackBarService } from './snack-bar.service';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
@Injectable({
  providedIn: 'root'
})
export class NotificationCheckService {

  private notificationJsSubscription?: Subscription

  constructor(private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService,
    private snackBarService: SnackBarService) { }

  setNotificationJsSubscription(subscription: Subscription): void {
    this.unsubscribe()
    this.notificationJsSubscription = subscription;
  }

  subcribe(): void {
    this.setNotificationJsSubscription(
      interval(40000).pipe().subscribe(() => {
        if (!document.location.hash.startsWith('#/home')) {
          // this.notification = !this.notification
          /**
           * checking if current facility was modified since last load ?
           */
          this.readNotification(this.facilityService.getLastRefreshTime()).pipe(filter((needToRefresh) => {
            //return needToRefresh TODO uncomment
            return false
          })).subscribe((notification) => {
            this.snackBarService.notification()
          })
        }
      })
    )
    this.unsubscribe()
  }

  unsubscribe(): void {
    if (this.notificationJsSubscription) {
      this.notificationJsSubscription.unsubscribe()
    }
  }

  readNotification(lastRefreshTime: number): Observable<boolean> {
    const tenantId = this.tenantService.getCurrentId()
    const facilityId = this.facilityService.getCurrentId()
    if (tenantId > 0 && facilityId > 0) {
      return this.http.get<boolean>(
        `${this.settings.getApiUrl()}/$notification?timestamp=${lastRefreshTime}`, httpOptions);
      // `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/notification/${timestamp}`, httpOptions);
    } else {
      return of(false)
    }
  }

}
