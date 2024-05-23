import { Component, ViewChild } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { MatStepper } from '@angular/material/stepper';
import { Router } from '@angular/router';
import { Tenant, Facility } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { TenantService } from 'src/app/core/_services/tenant.service';

@Component({
  selector: 'app-group-steps',
  templateUrl: './group-steps.component.html',
  styleUrls: ['./group-steps.component.css']
})
export class GroupStepsComponent {
    // @ViewChild('tenantStepper', { static: false }) tenantStepper!: MatStepper;
  // @ViewChild('facilityStepper', { static: false }) facilityStepper!: MatStepper;
  @ViewChild('stepper', { static: false }) stepper!: MatStepper;

  constructor(private _formBuilder: FormBuilder,
    private tenantService: TenantService,
    public facilityService: FacilityService,
    public patientService: PatientService,
    public registryService: ImmunizationRegistryService,
    private router: Router) {
    }

  nextTenant(tenant: Tenant) {
    // console.log("NEXT", tenant)
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

  openFhirAll() {

  }

}
