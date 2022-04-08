import { Component } from '@angular/core';
import { map } from 'rxjs/operators';
import { Breakpoints, BreakpointObserver } from '@angular/cdk/layout';
import { PatientService } from 'src/app/_services/patient.service';
import { TenantService } from 'src/app/_services/tenant.service';
import { FacilityService } from 'src/app/_services/facility.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent {

  constructor(private breakpointObserver: BreakpointObserver,
    // public tenantService: TenantService,
    // public facilityService: FacilityService,
    public patientService: PatientService) {}
}
