import { Component, ElementRef, ViewChild } from '@angular/core';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { interval, Observable, timer } from 'rxjs';
import { filter, map, shareReplay } from 'rxjs/operators';
import { MatDialog } from '@angular/material/dialog';
import { AuthenticationDialogComponent } from '../../authentication/_components/authentication-form/authentication-dialog/authentication-dialog.component';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { SettingsService } from 'src/app/core/_services/settings.service';
import { ActivatedRoute, Router, NavigationStart, Event as NavigationEvent  } from '@angular/router';
import { TokenStorageService } from 'src/app/core/authentication/_services/token-storage.service';
import { NotificationCheckService } from '../../_services/notification-check.service';
import { SnackBarService } from '../../_services/snack-bar.service';
import { FeedbackService } from '../../_services/feedback.service';

@Component({
  selector: 'app-navigation',
  templateUrl: './navigation.component.html',
  styleUrls: ['./navigation.component.css']
})
export class NavigationComponent {
  public pathname = window.location.pathname;

  isHandset$: Observable<boolean> = this.breakpointObserver.observe(Breakpoints.Handset)
    .pipe(
      map(result => result.matches),
      shareReplay()
    );

  notification: boolean = false

  constructor(
    private breakpointObserver: BreakpointObserver,
    // private dialog: MatDialog,
    public tenantService: TenantService,
    public facilityService: FacilityService,
    public patientService: PatientService,
    private tokenService: TokenStorageService,
    private notificationCheckService: NotificationCheckService,
    private snackBarService: SnackBarService,
    private feedbackService: FeedbackService,
    public router: Router) {
      this.router.events.subscribe(
        (event: NavigationEvent) => {
          if(event instanceof NavigationStart) {
            this.pathname = event.url
          }
        });

      interval(15000).pipe().subscribe(() => {
        if (!this.pathname.startsWith('/home')) {
          this.notification = !this.notification
          /**
           * checking if current facility was modified since last load ?
           */
          this.notificationCheckService.readNotification(this.patientService.getLastRefreshTime()).pipe(filter((notif) => {return notif})).subscribe((notification) => {
            snackBarService.notification(notification)
          })
        }
      })
    }

  logout() {
    // this.dialog.open(AuthenticationDialogComponent)
    this.tokenService.signOut()
    this.router.navigate(['/home'])
  }

  // tenantDropdown() {
  //   this.dialog.open(AuthenticationDialogComponent)
  // }

  // facilityDropdown() {
  //   this.dialog.open(AuthenticationDialogComponent)
  // }

  triggerRefresh() {
    // this.facilityService.doRefresh()
    this.patientService.doRefresh()
  }


}
