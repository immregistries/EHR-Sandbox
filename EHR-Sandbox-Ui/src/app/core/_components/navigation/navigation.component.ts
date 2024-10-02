import { Component, ElementRef, ViewChild } from '@angular/core';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { interval, Observable, timer } from 'rxjs';
import { filter, map, shareReplay } from 'rxjs/operators';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { Router, NavigationStart, Event as NavigationEvent } from '@angular/router';
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
  public pathname = window.location.href.split('#')[1];

  isHandset$: Observable<boolean> = this.breakpointObserver.observe(Breakpoints.Handset)
    .pipe(
      map(result => result.matches),
      shareReplay()
    );


  constructor(
    private breakpointObserver: BreakpointObserver,
    // private dialog: MatDialog,
    public tenantService: TenantService,
    public facilityService: FacilityService,
    public patientService: PatientService,
    private tokenService: TokenStorageService,
    private notificationCheckService: NotificationCheckService,
    private feedbackService: FeedbackService,
    public router: Router) {
    this.router.events.subscribe(
      (event: NavigationEvent) => {
        if (event instanceof NavigationStart) {
          this.pathname = event.url
        }
      });
  }

  logout() {
    // this.dialog.open(AuthenticationDialogComponent)
    this.tokenService.signOut()
    this.router.navigate(['/home'])
  }

  dontShowTopBar: string[] = ['/home', '/steps', '/vxu-steps', '/first-steps']
  showTopMenus(): boolean {
    return !this.dontShowTopBar.includes(this.pathname.split('?')[0])
  }




}
