import { Component } from '@angular/core';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { Observable } from 'rxjs';
import { map, shareReplay } from 'rxjs/operators';
import { MatDialog } from '@angular/material/dialog';
import { AuthenticationDialogComponent } from '../_dialogs/authentication-dialog/authentication-dialog.component';
import { TenantService } from 'src/app/_services/tenant.service';
import { PatientService } from 'src/app/_services/patient.service';
import { FacilityService } from 'src/app/_services/facility.service';
import { SettingsService } from 'src/app/_services/settings.service';

@Component({
  selector: 'app-navigation',
  templateUrl: './navigation.component.html',
  styleUrls: ['./navigation.component.css']
})
export class NavigationComponent {

  isHandset$: Observable<boolean> = this.breakpointObserver.observe(Breakpoints.Handset)
    .pipe(
      map(result => result.matches),
      shareReplay()
    );

  constructor(private breakpointObserver: BreakpointObserver,
    private dialog: MatDialog,
    public tenantService: TenantService,
    public facilityService: FacilityService,
    public patientService: PatientService) {}

  login() {
    this.dialog.open(AuthenticationDialogComponent)
  }

  tenantDropdown() {
    this.dialog.open(AuthenticationDialogComponent)
  }

  facilityDropdown() {
    this.dialog.open(AuthenticationDialogComponent)
  }


}
