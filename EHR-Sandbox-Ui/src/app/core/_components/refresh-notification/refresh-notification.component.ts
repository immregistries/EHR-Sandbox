import { Component, OnInit } from '@angular/core';
import { filter, interval } from 'rxjs';
import { TokenStorageService } from '../../authentication/_services/token-storage.service';
import { FacilityService } from '../../_services/facility.service';
import { NotificationCheckService } from '../../_services/notification-check.service';
import { PatientService } from '../../_services/patient.service';
import { SnackBarService } from '../../_services/snack-bar.service';
import { TenantService } from '../../_services/tenant.service';

@Component({
  selector: 'app-refresh-notification',
  templateUrl: './refresh-notification.component.html',
  styleUrls: ['./refresh-notification.component.css']
})
export class RefreshNotificationComponent implements OnInit {

  notification: boolean = false

  constructor(public tenantService: TenantService,
    private facilityService: FacilityService,
    private patientService: PatientService,
    private tokenService: TokenStorageService,
    private notificationCheckService: NotificationCheckService,) { }

  ngOnInit(): void {
    this.notificationCheckService.subcribe()
  }

  ngOnDestroy(): void {
    this.notificationCheckService.unsubscribe()
  }

  triggerRefresh() {
    // this.facilityService.doRefresh()
    this.patientService.doRefresh()
  }


}
