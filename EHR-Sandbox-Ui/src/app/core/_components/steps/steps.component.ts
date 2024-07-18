import { Component, ViewChild } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Facility, Tenant } from '../../_model/rest';
import { TenantService } from '../../_services/tenant.service';
import { FacilityService } from '../../_services/facility.service';
import { MatStepper } from '@angular/material/stepper';
import { Router } from '@angular/router';

@Component({
  selector: 'app-steps',
  templateUrl: './steps.component.html',
  styleUrls: ['./steps.component.css']
})
export class StepsComponent {

  // @ViewChild('tenantStepper', { static: false }) tenantStepper!: MatStepper;
  // @ViewChild('facilityStepper', { static: false }) facilityStepper!: MatStepper;
  @ViewChild('stepper', { static: false }) stepper!: MatStepper;

  constructor(private _formBuilder: FormBuilder,
    private tenantService: TenantService,
    private facilityService: FacilityService,
    private router: Router) {
  }

  nextTenant(tenant: Tenant) {
    this.tenantService.setCurrent(tenant)
    this.stepper.next()
  }
  nextFacility(facility: Facility) {
    this.facilityService.setCurrent(facility)
    this.stepper.next()
  }

  end() {
    this.router.navigate(['/dashboard'])
  }

}
