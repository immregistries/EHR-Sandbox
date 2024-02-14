import { Component, ElementRef, ViewChild } from '@angular/core';
import { map } from 'rxjs/operators';
import { Breakpoints, BreakpointObserver } from '@angular/cdk/layout';
import { PatientService } from 'src/app/core/_services/patient.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { Observable, firstValueFrom, merge } from 'rxjs';
import { EhrPatient } from '../../_model/rest';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent {

  constructor(public tenantService: TenantService,
    public facilityService: FacilityService,
    public patientService: PatientService) {}

  rowHeight(): string {
    return (window.innerHeight/2 - 35) + 'px'
  }

  patientLoading: boolean = false

  patientListRefreshObservable(): Observable<any> {
    return merge(
      this.facilityService.getCurrentObservable(),
      this.patientService.getRefresh(),
      this.facilityService.getRefresh(),
    )
  }

  patientListObservable(tenantId: number, facilityId: number): Observable<EhrPatient[]> {
    return this.patientService.quickReadPatients()
  }
}
